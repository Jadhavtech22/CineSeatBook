package in.cineseat;

import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
class Cinema {
    private final JdbcTemplate db;
    Cinema(JdbcTemplate db) { this.db = db; }
    static final String DISPLAY = """
        select s.id, m.title, m.genre, m.language, s.auditorium, s.price,
        to_char(s.starts_at at time zone 'Asia/Kolkata','Dy, DD Mon · HH24:MI') as showtime
        from screenings s join movies m on m.id=s.movie_id
        """;
    List<Map<String,Object>> shows(int movie) {
        return db.queryForList(DISPLAY+" where s.movie_id=? and s.starts_at>now() order by s.starts_at",movie);
    }
    Map<String,Object> show(long id) {
        return db.queryForList(DISPLAY+" where s.id=? and s.starts_at>now()",id).stream().findFirst()
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND,"Show unavailable"));
    }
    List<String> occupied(long id) {
        return db.queryForList("select seat from tickets where screening_id=?",String.class,id);
    }
    List<String> validate(long id, List<String> seats) {
        if (seats == null || seats.isEmpty() || seats.size()>8 ||
            seats.stream().anyMatch(s -> !s.matches("[A-F][1-8]")) ||
            new HashSet<>(seats).size()!=seats.size()) throw new IllegalArgumentException("Choose 1 to 8 different seats.");
        if (!Collections.disjoint(occupied(id),seats)) throw new IllegalArgumentException("A selected seat has just been booked. Please choose again.");
        return seats.stream().sorted().toList();
    }
    @Transactional
    public void pay(String email,long id,List<String> requested,UUID reference) {
        // Serialize checkouts for one screening. A retry of a completed checkout returns its ticket.
        db.queryForList("select id from screenings where id=? for update",id);
        var existing=db.queryForList("select email,screening_id from bookings where id=?",reference);
        if (!existing.isEmpty()) {
            var previous=existing.getFirst();
            if (email.equals(previous.get("email")) && Long.valueOf(id).equals(previous.get("screening_id")) &&
                db.queryForList("select seat from tickets where booking_id=? order by seat",String.class,reference)
                    .equals(requested.stream().sorted().toList())) return;
            throw new IllegalArgumentException("Checkout reference expired. Please choose your seats again.");
        }
        int price=(int)show(id).get("price");
        var seats=validate(id,requested);
        db.update("insert into bookings(id,email,screening_id,total) values(?,?,?,?)",reference,email,id,price*seats.size());
        for(String seat:seats) db.update("insert into tickets(booking_id,screening_id,seat) values(?,?,?)",reference,id,seat);
    }
    List<Map<String,Object>> history(String email) {
        return db.queryForList("""
            select b.id,m.title,s.auditorium,b.total,string_agg(t.seat, ', ' order by t.seat) as seats,
            to_char(s.starts_at at time zone 'Asia/Kolkata','Dy, DD Mon YYYY · HH24:MI') as showtime
            from bookings b join tickets t on t.booking_id=b.id join screenings s on s.id=b.screening_id
            join movies m on m.id=s.movie_id where b.email=?
            group by b.id,m.title,s.id order by b.paid_at desc
            """,email);
    }
}

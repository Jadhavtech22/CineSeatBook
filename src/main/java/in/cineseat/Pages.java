package in.cineseat;

import java.security.Principal;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
class Pages {
    private final Cinema cinema;
    private final JdbcTemplate db;

    Pages(Cinema cinema, JdbcTemplate db) {
        this.cinema = cinema;
        this.db = db;
    }

    @GetMapping("/login")
    String login() {
        return "login";
    }

    @GetMapping("/") String movies(Model model){
        model.addAttribute("movies",db.queryForList("select * from movies order by id"));
        return "movies";
    }
    @GetMapping("/movies/{id}") String shows(@PathVariable int id,Model model){
        model.addAttribute("shows",cinema.shows(id));
        return "shows";
    }
    @GetMapping("/shows/{id}") String seats(@PathVariable long id,Model model){
        model.addAttribute("show",cinema.show(id));
        model.addAttribute("taken",cinema.occupied(id));
        model.addAttribute("rows",List.of("A","B","C","D","E","F"));
        return "seats";
    }
    @PostMapping("/shows/{id}/review") String review(@PathVariable long id,
            @RequestParam(required=false) List<String> seat,Model model,RedirectAttributes flash){
        var show=cinema.show(id);
        try {
            var selected=cinema.validate(id,seat);
            model.addAttribute("show",show);
            model.addAttribute("selected",selected);
            model.addAttribute("total",(int)show.get("price")*selected.size());
            model.addAttribute("reference",UUID.randomUUID());
            return "review";
        } catch(IllegalArgumentException ex){
            flash.addFlashAttribute("error",ex.getMessage());
            return "redirect:/shows/"+id;
        }
    }
    @PostMapping("/shows/{id}/pay") String pay(@PathVariable long id,@RequestParam List<String> seat,
            @RequestParam UUID reference, Principal user,RedirectAttributes flash){
        try {
            cinema.pay(user.getName(),id,seat,reference);
            flash.addFlashAttribute("message","Demo payment successful. Your booking is confirmed.");
            return "redirect:/bookings";
        } catch(IllegalArgumentException ex){
            flash.addFlashAttribute("error",ex.getMessage());
            return "redirect:/shows/"+id;
        }
    }
    @GetMapping("/bookings") String history(Principal user,Model model){
        model.addAttribute("bookings",cinema.history(user.getName()));
        return "bookings";
    }
}

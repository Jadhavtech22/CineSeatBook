package in.cineseat;

import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import java.util.Locale;

@Configuration
class Security {
    @Bean BCryptPasswordEncoder passwords() { return new BCryptPasswordEncoder(); }

    @Bean UserDetailsService users(JdbcTemplate db) {
        return input -> {
            String email = input.trim().toLowerCase(Locale.ROOT);
            var hashes = db.queryForList("select password_hash from members where email=?", String.class, email);
            if (hashes.isEmpty()) throw new UsernameNotFoundException("Unknown account");
            return User.withUsername(email).password(hashes.getFirst()).roles("CUSTOMER").build();
        };
    }

    @Bean SecurityFilterChain filter(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(a -> a.requestMatchers("/register","/login","/style.css","/error").permitAll()
                .anyRequest().authenticated())
            .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/",true).permitAll())
            .logout(l -> l.logoutSuccessUrl("/login?logout")).build();
    }
}

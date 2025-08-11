package com.haydenhurst.bankingapp.config;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfiguration {
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
    @Bean
    protected void configure(HttpSecurity http) throws Exception {
        http
                .anyMatchers("/api/users/register", "/api/users/login").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin().disable(); // Disable default login form if using JWT
    }
    */
}


package com.javaproject.security;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JdbcUserDetailsManager jdbcUserDetailsManager(
            DataSource dataSource,
            BCryptPasswordEncoder passwordEncoder) {

        JdbcUserDetailsManager manager =
                new JdbcUserDetailsManager(dataSource);

        manager.setEnableAuthorities(true);

        if (!manager.userExists("bugs")) {
            UserDetails bugs = User.withUsername("bugs")
                    .password(passwordEncoder.encode("bunny"))
                    .roles("USER")
                    .build();

            manager.createUser(bugs);
        }

        if (!manager.userExists("daffy")) {
            UserDetails daffy = User.withUsername("daffy")
                    .password(passwordEncoder.encode("duck"))
                    .roles("USER", "MANAGER")
                    .build();

            manager.createUser(daffy);
        }

        return manager;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            LoggingAccessDeniedHandler accessDeniedHandler)
            throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/h2-console/**"
                ).permitAll()
                .requestMatchers(
                    "/user/**"
                ).hasAnyRole("USER", "MANAGER")
                .requestMatchers(
                    "/secured/**"
                ).hasAnyRole("USER", "MANAGER")
                .requestMatchers(
                    "/manager/**"
                ).hasRole("MANAGER")
                .requestMatchers(
                    "/", "/login", "/css/**", "/js/**",
                    "/images/**", "/webjars/**"
                ).permitAll()
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/secured", true)
                .permitAll()
            )
            .logout(logout -> logout
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            .exceptionHandling(exception -> exception
                .accessDeniedHandler(accessDeniedHandler)
            )
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .frameOptions(frame -> frame.disable())
            );

        return http.build();
    }
}

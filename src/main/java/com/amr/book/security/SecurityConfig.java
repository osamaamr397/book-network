package com.amr.book.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationProvider;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration //to mark a class as configuration
@EnableWebSecurity
@RequiredArgsConstructor //in order to create constructor will all private fields
@EnableMethodSecurity(securedEnabled = true)

public class SecurityConfig {

    private final JwtFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    //when we make @Configuration the scan of classes begain so spring will configure that we have a
    //bench or beans we have to configure and to put in context so we use @Bean
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)throws Exception{
        http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)//with this way we disable the csrf
                .authorizeHttpRequests(req->
                        req.requestMatchers(//that the urls that i want to permit
                                        "/auth/**", //this belong to our controller
                                        // so i want to authorized all the methods that are inside it
                                        "/v2/api-docs",
                                        "/v3/api-docs",
                                        "/v3/api-docs/**",
                                        "/swagger-resources",
                                        "/swagger-resources/**",
                                        "/configuration/ui",
                                        "/configuration/security",
                                        "/swagger-ui/**",
                                        "/webjars/**",
                                        "/swagger-ui.html"
                                ).permitAll()
                                .anyRequest()
                                .authenticated() //else it should be authenticated
                )
                //session creation policy statles mean that spring should not store the session state in its context
                //so this means every time user send or every time receive a request we will act on it as
                //we don't know anything about this request
                .sessionManagement(session->session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                //to add or our own filter which customized by me or which support from spring
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

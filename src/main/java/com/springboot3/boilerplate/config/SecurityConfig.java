package com.springboot3.boilerplate.config;

import com.springboot3.boilerplate.security.CustomAuthenticationFailureHandler;
import com.springboot3.boilerplate.security.CustomAuthenticationSuccessHandler;
import com.springboot3.boilerplate.security.CustomUsernamePasswordAuthenticationFilter;
import com.springboot3.boilerplate.security.RestAuthenticationEntryPoint;
import com.springboot3.boilerplate.security.TokenAuthenticationFilter;
import com.springboot3.boilerplate.security.oauth2.CustomOAuth2UserService;
import com.springboot3.boilerplate.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.springboot3.boilerplate.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.springboot3.boilerplate.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter();
    }

    /*
      By default, Spring OAuth2 uses HttpSessionOAuth2AuthorizationRequestRepository to save
      the authorization request. But, since our service is stateless, we can't save it in
      the session. We'll save the request in a Base64 encoded cookie instead.
    */
    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CustomUsernamePasswordAuthenticationFilter authenticationFilter(
            HttpSecurity http,
            CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler,
            CustomAuthenticationFailureHandler customAuthenticationFailureHandler) throws Exception {
        CustomUsernamePasswordAuthenticationFilter authenticationFilter = new CustomUsernamePasswordAuthenticationFilter();
        AuthenticationManager authenticationManager = authenticationManager(
                http.getSharedObject(AuthenticationConfiguration.class));
        AntPathRequestMatcher antPathRequestMatcher = new AntPathRequestMatcher("/v1/auth/sign-in", "POST");

        authenticationFilter.setAuthenticationManager(authenticationManager);
        authenticationFilter.setRequiresAuthenticationRequestMatcher(antPathRequestMatcher);
        authenticationFilter.setAuthenticationFailureHandler(customAuthenticationFailureHandler);
        authenticationFilter.setAuthenticationSuccessHandler(customAuthenticationSuccessHandler);

        return authenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService,
                                           OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
                                           OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler,
                                           RestAuthenticationEntryPoint restAuthenticationEntryPoint)
            throws Exception {
        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .cors().disable()
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .exceptionHandling().authenticationEntryPoint(restAuthenticationEntryPoint)
                .and()
                .authorizeHttpRequests()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/v1/**").permitAll()
                .requestMatchers("/v1/auth/**").permitAll()
                .requestMatchers("/v1/oauth2/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .oauth2Login()
                .authorizationEndpoint()
                .baseUri("/v1/oauth2/authorize")
                .authorizationRequestRepository(cookieAuthorizationRequestRepository())
                .and()
                .redirectionEndpoint()
                .baseUri("/v1/oauth2/callback/*")
                .and()
                .userInfoEndpoint()
                .userService(customOAuth2UserService)
                .and()
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler)
                .and()
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
package com.project.date.config;

import com.project.date.exception.AccessDeniedHandlerException;
import com.project.date.exception.AuthenticationEntryPointException;
import com.project.date.impl.UserDetailsServiceImpl;
import com.project.date.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@ConditionalOnDefaultWebSecurity
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfiguration {

    @Value("${jwt.secret}")
    String SECRET_KEY;
    private final TokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationEntryPointException authenticationEntryPointException;
    private final AccessDeniedHandlerException accessDeniedHandlerException;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(SecurityProperties.BASIC_AUTH_ORDER)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(); // cors??????
        //h2 console??? SoketJs??? ???????????? ?????? Header ??????
        http.headers().frameOptions().sameOrigin();
        // SockJS??? ??????????????? HTML iframe ????????? ?????? ????????? ???????????? ????????? ??????????????? ?????? ????????? ????????????.
        http.csrf().disable()// ???????????? on??? csrf ????????? ????????? ????????????. on?????? ???????????? ?????? ??????????????? ?????????????????? ??????????????? ?????????.
                .cors()
                .and()
//                .formLogin() // ???????????? ????????? ???????????? ????????? ???????????? ????????????.
//                .and()
                //Exception ???????????? ????????? ????????? ??????
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPointException)
                .accessDeniedHandler(accessDeniedHandlerException)

                //????????? ???????????? ?????? ????????? STATELESS??? ??????
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
//            ???????????? (=????????????) ??????????????? api??????
                .authorizeRequests()
                .antMatchers("/user/**").permitAll()
                .antMatchers("/post/**").permitAll()
                .antMatchers("/comment/**").permitAll()
                .antMatchers("/reComment/**").permitAll()
                .antMatchers("/main").permitAll()
                .antMatchers("/profile/**").permitAll()
                .antMatchers("/chat/**").permitAll()
                .antMatchers("/chat").permitAll()
                .antMatchers("/h2-console/**").permitAll() // h2-console ????????? ?????? ??????
                .antMatchers("/report/**").permitAll()
                .antMatchers("/stomp/**").permitAll()
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll() // preflight ????????? ?????? ??????
                .anyRequest().authenticated()
//                .anyRequest().permitAll()

                .and()
                .apply(new JwtSecurityConfiguration(SECRET_KEY, tokenProvider, userDetailsService));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true) ;
        configuration.addAllowedOriginPattern("*");
//        configuration.addAllowedOrigin("http://localhost:3000");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.addExposedHeader("*");
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

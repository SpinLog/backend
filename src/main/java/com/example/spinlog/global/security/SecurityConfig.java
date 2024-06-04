package com.example.spinlog.global.security;

import com.example.spinlog.global.security.customFilter.TemporaryAuthFilter;
import com.example.spinlog.global.security.oauth2.client.CustomClientRegistrationRepository;
import com.example.spinlog.global.security.oauth2.client.CustomOAuth2AuthorizedClientService;
import com.example.spinlog.global.security.oauth2.handler.authentication.CustomAuthenticationEntryPoint;
import com.example.spinlog.global.security.oauth2.handler.login.OAuth2LoginSuccessHandler;
import com.example.spinlog.global.security.oauth2.handler.logout.OAuth2LogoutHandler;
import com.example.spinlog.global.security.oauth2.handler.logout.OAuth2LogoutSuccessHandler;
import com.example.spinlog.global.security.oauth2.service.CustomOAuth2UserService;
import com.example.spinlog.global.security.session.CustomSessionManager;
import com.example.spinlog.global.security.session.SessionAuthenticationFilter;
import com.example.spinlog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomClientRegistrationRepository customClientRegistrationRepository;
    private final CustomOAuth2AuthorizedClientService customOAuth2AuthorizedClientService;
    private final JdbcTemplate jdbcTemplate;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    private final OAuth2LogoutHandler oAuth2LogoutHandler;
    private final OAuth2LogoutSuccessHandler oAuth2LogoutSuccessHandler;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    private final TemporaryAuthFilter temporaryAuthFilter;
    private final UserRepository userRepository;
    private final CustomSessionManager customSessionManager;

//    private final CorsConfig corsConfig;

    /*
    TODO oauth2Login() 관련
        3. 로그아웃을 한 뒤 다시 로그인을 하면 자동 로그인이 되지 않게
            - 참고: https://docs.spring.io/spring-security/reference/servlet/authentication/logout.html
        5. cors 설정
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource())
                )
                .csrf(csrf -> csrf
                        .disable()
                )
                .formLogin(form -> form
                        .disable()
                )
                .httpBasic(basic -> basic
                        .disable()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(url -> url
                                .baseUri("/api/users/login")
                        )
                        .clientRegistrationRepository(customClientRegistrationRepository.clientRegistrationRepository())
                        .authorizedClientService(customOAuth2AuthorizedClientService.oAuth2AuthorizedClientService(
                                jdbcTemplate, customClientRegistrationRepository.clientRegistrationRepository()
                        ))
                        .userInfoEndpoint(userInfoEndpointConfig ->
                                userInfoEndpointConfig.userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/api/users/logout")
                        //.addLogoutHandler(oAuth2LogoutHandler)
                        //.invalidateHttpSession(true)
                        //.clearAuthentication(true)
                        .logoutSuccessHandler(oAuth2LogoutSuccessHandler)
                        .permitAll()
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/oauth2/**", "/favicon.ico", "/error",
                                "/actuator/**",
                                "/api/authentication/logout-result",
                                "/api/authentication/not-authenticated").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                .addFilterBefore(new SessionAuthenticationFilter(userRepository, customSessionManager), LogoutFilter.class)
                .addFilterAfter(temporaryAuthFilter, ExceptionTranslationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowCredentials(true);
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "https://frontend-chi-sage-83.vercel.app",
                "http://localhost:5173",
                "https://spinlog.swygbro.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization", "Set-Cookie", "TemporaryAuth"));
        //configuration.setExposedHeaders(Arrays.asList(AUTHORIZATION, SET_COOKIE, "TemporaryAuth"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}

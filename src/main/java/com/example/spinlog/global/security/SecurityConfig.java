package com.example.spinlog.global.security;

import com.example.spinlog.global.security.oauth2.client.CustomClientRegistrationRepository;
import com.example.spinlog.global.security.oauth2.client.CustomOAuth2AuthorizedClientService;
import com.example.spinlog.global.security.oauth2.handler.authentication.CustomAuthenticationEntryPoint;
import com.example.spinlog.global.security.oauth2.handler.login.OAuth2LoginSuccessHandler;
import com.example.spinlog.global.security.oauth2.handler.logout.OAuth2LogoutHandler;
import com.example.spinlog.global.security.oauth2.handler.logout.OAuth2LogoutSuccessHandler;
import com.example.spinlog.global.security.oauth2.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;
import java.util.List;

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
                .cors(cors -> cors
                        .configurationSource(new CorsConfigurationSource() {
                            @Override
                            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                                CorsConfiguration config = new CorsConfiguration();

                                config.setAllowedOrigins(Collections.singletonList("https://frontend-chi-sage-83.vercel.app/"));
                                config.setAllowedOrigins(Collections.singletonList("http://localhost:5173/"));

                                config.setAllowedMethods(Collections.singletonList("*"));
                                config.setAllowCredentials(true);
                                config.setAllowedHeaders(Collections.singletonList("*"));
                                config.setMaxAge(3600L);

                                config.setExposedHeaders(Collections.singletonList("Authorization"));
                                config.setExposedHeaders(Collections.singletonList("Set-Cookie"));

                                return config;
                            }
                        }))
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
                        .addLogoutHandler(oAuth2LogoutHandler)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .logoutSuccessHandler(oAuth2LogoutSuccessHandler)
                        .permitAll()
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/oauth2/**", "/favicon.ico",
                                "/api/authentication/logout-result",
                                "/api/authentication/not-authenticated").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                );
//                .addFilterBefore(corsConfig.corsFilter(), LogoutFilter.class);

        return http.build();
    }

}

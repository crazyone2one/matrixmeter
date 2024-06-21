package cn.master.matrix.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author Created by 11's papa on 06/21/2024
 **/
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final CustomUserDetailsService customUserDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final JwtProvider jwtProvider;
    private final CustomLogoutHandler logoutHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity security) throws Exception {
        security.csrf(AbstractHttpConfigurer::disable);
        security.exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler));
        security.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        security.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/auth/signin").permitAll()
                .requestMatchers("/v3/api-docs").permitAll()
                .anyRequest().authenticated());
        security.authenticationProvider(authenticationProvider());
        security.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        security.logout(l -> l.logoutUrl("/logout")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler(
                        (request, response, authentication) -> SecurityContextHolder.clearContext()
                ));
        return security.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter(jwtProvider, customUserDetailsService);
    }
}

package com.zosh.configrations;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Configuration
public class SecurityConfig {
	
	@Autowired
	private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

	@Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173,https://zosh-pos.vercel.app,https://pos-sytem-bcs6.vercel.app}")
	private String allowedOrigins;
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		
		return http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(Authorize -> Authorize
						.requestMatchers("/api/**").authenticated()
						.requestMatchers("/api/super-admin/**").hasRole("ADMIN")
						.anyRequest().permitAll())
			.addFilterBefore(new JwtValidator(), BasicAuthenticationFilter.class)
			.csrf(AbstractHttpConfigurer::disable)
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.exceptionHandling(
					exceptionHandler -> exceptionHandler
							.authenticationEntryPoint(customAuthenticationEntryPoint))
			.build();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	private CorsConfigurationSource corsConfigurationSource() {
		return new CorsConfigurationSource() {
			@Override
			public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
				CorsConfiguration cfg = new CorsConfiguration();
				if (allowedOrigins != null && !allowedOrigins.trim().isEmpty()) {
					List<String> origins = Arrays.stream(allowedOrigins.split(","))
							.map(String::trim)
							.filter(s -> !s.isEmpty())
							.toList();
					cfg.setAllowedOriginPatterns(origins);
				} else {
					cfg.setAllowedOriginPatterns(Collections.singletonList("*"));
				}
				cfg.setAllowedMethods(Collections.singletonList("*"));
				cfg.setAllowCredentials(true);
				cfg.setAllowedHeaders(Collections.singletonList("*"));
				cfg.setExposedHeaders(Arrays.asList("Authorization"));
				cfg.setMaxAge(3600L);
				return cfg;
			}
		};
	}

}

package ca.sheridancollege.shar1440.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http // Disable CSRF protection (Cross-Site Request Forgery)
            // Needed because the frontend (JS fetch calls) sends POST requests without CSRF tokens
	        .csrf(csrf -> csrf.disable())
	        .authorizeHttpRequests(auth -> auth
	        	// Allow anyone (no login required) to access:
                // "/api/paypal/**" → all PayPal endpoints (create-order, capture-order)
	        	.requestMatchers("/", "/api/paypal/**").permitAll()
	            .anyRequest().authenticated()   // Any other request MUST be authenticated 
	        );

	    return http.build();
	}
	}
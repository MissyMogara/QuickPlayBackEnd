package com.example.quickplay.security;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import com.example.quickplay.services.JWTService;
import com.example.quickplay.services.MyReactiveUserDetailsService;
import com.mongodb.lang.NonNull;

import reactor.core.publisher.Mono;

@Component
public class JwtFilter implements WebFilter {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private MyReactiveUserDetailsService userDetailsService;

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String jwt = authHeader.substring(7);
        String username = jwtService.extractUserName(jwt);

        if (username != null) {
            return userDetailsService.findByUsername(username)
                    .flatMap(userDetails -> {
                        if (jwtService.validateToken(jwt, userDetails)) {
                            Authentication auth = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                            );
                            return exchange.getPrincipal()
                                .then(Mono.fromRunnable(() -> 
                                    SecurityContextHolder.getContext().setAuthentication(auth)
                                ))
                                .then(chain.filter(exchange));
                        }
                        return chain.filter(exchange);
                    });
        }
        return chain.filter(exchange);
    }
}

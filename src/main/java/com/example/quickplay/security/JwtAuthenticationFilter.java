package com.example.quickplay.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ReactiveUserDetailsService userDetailsService;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        //Obtener cabeceras del exchange
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // Si no hay cabecera de autorización, sigue el flujo normal
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        // Se obtiene el token
        String token = authHeader.substring(7);

        // Si el token no es válido, sigue el flujo normal y devuelve un error no autorizado
        if (!jwtUtil.isValidToken(token)) {
            return chain.filter(exchange);
        }

        // Obtener el usuario desde el token
        return Mono.justOrEmpty(jwtUtil.getUsernameFromToken(token))
                .flatMap(userDetailsService::findByUsername)
                .flatMap(userDetails -> {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    // Propagar la autenticación sin alterar la request original
                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authToken));
                });
    }


}

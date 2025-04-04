package com.example.quickplay.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.quickplay.handlers.CommentHandler;
import com.example.quickplay.handlers.PostHandler;
import com.example.quickplay.handlers.ProfileHandler;
import com.example.quickplay.handlers.UserHandler;

@Configuration
@EnableWebFluxSecurity
public class RouterConfig implements WebFluxConfigurer {

    @Bean
    public RouterFunction<ServerResponse> postRoutes(PostHandler handler) {
        return RouterFunctions.route()
                .POST("/api/posts", handler::createPost)
                .GET("/api/posts/{postId}", handler::getPostById)
                .GET("/api/posts/user/{userId}", handler::getPostsByUserId)
                .GET("/api/posts/title/search", handler::getPostByTitle)
                .PUT("/api/posts/{postId}/like", handler::likePost)
                .PUT("/api/posts/{postId}/unlike", handler::unlikePost)
                .PUT("/api/posts/{postId}/view", handler::incrementViews)
                .PUT("/api/posts/{postId}/update", handler::updatePost)
                .DELETE("/api/posts/{postId}", handler::deletePost)
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> commentRoutes(CommentHandler handler) {
        return RouterFunctions.route()
        .POST("/api/comments", handler::createComment)
        .GET("/api/comments/{commentId}", handler::getCommentById)
        .GET("/api/comments/user/{userId}", handler::getCommentsByUserId)
        .GET("/api/comments/user/{userId}/order", handler::getCommentsByUserIdOrderByLikes)
        .PUT("/api/comments/{commentId}/like", handler::likeComment)
        .PUT("/api/comments/{commentId}/unlike", handler::unlikeComment)
        .PUT("/api/comments/{commentId}/update", handler::updateComment)
        .DELETE("/api/comments/{commentId}", handler::deleteComment)
        .build();
    }

    @Bean
    public RouterFunction<ServerResponse> profileRoutes(ProfileHandler handler) {
        return RouterFunctions.route()
        .GET("/api/profiles/{userId}", handler::getProfileByUserId)
        .PUT("/api/profiles/{userId}/update", handler::updateProfile)
        .build();
    }

    @Bean
    public RouterFunction<ServerResponse> loginRoutes(UserHandler handler) {
        return RouterFunctions.route()
                .GET("/api/users/{userId}", handler::getUserById)
                .POST("/api/users/login", handler::login)
                .POST("/api/users/register", handler::register)
                .PUT("/api/users/{userId}/update", handler::updateUser)
                .PUT("/api/users/{userId}/follow/{followerId}", handler::followUser)
                .DELETE("/api/users/{userId}", handler::deleteUser)
                .build();
    }


    /**
     * Configuración de seguridad
     * Añadimos la seguridad a las rutas que empiecen por /api/**
     * Permitimos acceso a las rutas de productos sin autenticación --> Cambiar cuando generemos tokens
     * Permitimos acceso a las rutas de autenticación/registro sin autenticación
     * @param http
     * @return
     */
    @Bean
    SecurityWebFilterChain webHttpSecurity(ServerHttpSecurity http, JwtAuthenticationFilter jwtFilter) {
        http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/api/**"))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/users/login", "/api/users/register").permitAll()  // Rutas públicas para autenticación/registro
                        .anyExchange().authenticated()
                )
                .addFilterBefore(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(withDefaults());
        return http.build();
    }

    /**
     * Configuración Cors, viene de implementar el interfaz WebFluxConfigurer y sobreescribir el método addCorsMappings
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://quickplay.com")   //Aquí ponemos el dominio desde el que aceptamos peticiones
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true).maxAge(3600);

        // Add more mappings...
    }


}
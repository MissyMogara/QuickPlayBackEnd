package com.example.quickplay.security;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.example.quickplay.entities.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtUtil {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    @Value("${app.security.jwt.expiration}")
    private Long jwtDurationSeconds;

    public String generateToken(User user) {

        return Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()), Jwts.SIG.HS256)
                .header()
                .add("typ", "JWT").and()
                .subject(user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + (jwtDurationSeconds * 1000)))
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .compact();

    }

    public boolean isValidToken(String token) {
        if (!StringUtils.hasLength(token))
            return false; //No hay token

        try {
            JwtParser validator = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes())).build();
            validator.parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            log.info("Error en la firma del token", e);
        } catch (MalformedJwtException | UnsupportedJwtException e) {
            log.info("Token incorrecto", e);
        } catch (ExpiredJwtException e) {
            log.info("Token expirado", e);
        }
        return false;

    }

    public String getUsernameFromToken(String token) {
        JwtParser parser = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes())).build();
        Jws<Claims> claims = parser.parseSignedClaims(token);
        return claims.getPayload().get("username").toString();
    }
}
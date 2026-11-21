package com.vitalpet.mspets.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
public class JwtService {

    private final SecretKey signingKey;

    public JwtService(@Value("${JWT_SECRET}") String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // Verifica la firma y la fecha de expiracion.
    // Si el token es invalido, JJWT lanza una excepcion.
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Comprueba si el token es valido.
    public boolean isTokenValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Email del usuario autenticado.
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    // ID del usuario incluido en el JWT.
    public Long extractUserId(String token) {
        Object userId = extractClaims(token).get("userId");

        if (userId instanceof Number number) {
            return number.longValue();
        }

        throw new IllegalArgumentException(
                "El token no contiene un userId valido"
        );
    }

    // Rol incluido en el JWT: CLIENT o ADMIN.
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }
}


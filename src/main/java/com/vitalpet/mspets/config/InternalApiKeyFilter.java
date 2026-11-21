package com.vitalpet.mspets.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

public class InternalApiKeyFilter extends OncePerRequestFilter {

    private final byte[] expectedKey;

    public InternalApiKeyFilter(String internalApiKey) {
        if (internalApiKey == null || internalApiKey.isBlank()) {
            throw new IllegalArgumentException(
                "INTERNAL_API_KEY no puede estar vacia"
            );
        }

        this.expectedKey = internalApiKey.getBytes(
            StandardCharsets.UTF_8
        );
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"GET".equals(request.getMethod())) {
            return true;
        }

        String path = request.getServletPath();

        return !(
            path.matches("/api/pets/[0-9]+") ||
            path.matches("/api/pets/[0-9]+/exists")
        );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String receivedKey =
                request.getHeader("X-Internal-Api-Key");

        if (receivedKey != null) {
            boolean valid = MessageDigest.isEqual(
                expectedKey,
                receivedKey.getBytes(StandardCharsets.UTF_8)
            );

            if (valid) {
                var authentication =
                    new UsernamePasswordAuthenticationToken(
                        "ms-appointments",
                        null,
                        List.of(
                            new SimpleGrantedAuthority("ROLE_SERVICE")
                        )
                    );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}

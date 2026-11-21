package com.vitalpet.mspets.config;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${INTERNAL_API_KEY}") String internalApiKey
    ) throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(withDefaults())
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)

            .sessionManagement(session -> session
                .sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .exceptionHandling(exceptions -> exceptions

                .authenticationEntryPoint((request, response, ex) ->
                    response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                    )
                )

                .accessDeniedHandler((request, response, ex) ->
                    response.setStatus(
                        HttpServletResponse.SC_FORBIDDEN
                    )
                )
            )

            .authorizeHttpRequests(auth -> auth

                // Catalogo publico.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/pets/species"
                ).permitAll()

                // Preflight CORS.
                .requestMatchers(
                    HttpMethod.OPTIONS,
                    "/**"
                ).permitAll()

                // Documentacion.
                .requestMatchers(
                    "/api/pets/v3/api-docs",
                    "/api/pets/v3/api-docs/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()

                // Crear: CLIENT o ADMIN.
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/pets"
                ).hasAnyRole("CLIENT", "ADMIN")

                // Reasignar propietario: solo ADMIN.
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/pets/*/owner/*"
                ).hasRole("ADMIN")

                // Modificar: autenticacion requerida.
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/pets/*"
                ).hasAnyRole("CLIENT", "ADMIN")

                // Desactivar: autenticacion requerida.
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/pets/*"
                ).hasAnyRole("CLIENT", "ADMIN")

                // Listado general: solo ADMIN.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/pets"
                ).hasRole("ADMIN")

                // Mascotas disponibles: usuario autenticado.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/pets/available"
                ).hasAnyRole("CLIENT", "ADMIN")

                // Consultar mascotas por propietario.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/pets/*/owner"
                ).hasAnyRole("CLIENT", "ADMIN")

                // Verificar existencia: ADMIN o servicio interno.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/pets/*/exists"
                ).hasAnyRole("ADMIN", "SERVICE")

                // Consultar mascota individual.
                // La propiedad se valida en PetController.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/pets/*"
                ).hasAnyRole("CLIENT", "ADMIN", "SERVICE")

                // Todo lo demas queda bloqueado.
                .anyRequest().denyAll()
            )

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            )

            .addFilterBefore(
                new InternalApiKeyFilter(internalApiKey),
                JwtAuthenticationFilter.class
            );

        return http.build();
    }
}


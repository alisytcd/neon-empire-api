package com.neonempire.neon_empire_api.config

import com.nimbusds.jose.jwk.source.ImmutableSecret
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import java.util.Base64
import javax.crypto.spec.SecretKeySpec

@Configuration
class SecurityConfig(@Value("\${JWT_SECRET}") private val jwtSecret: String,
                     @Value("\${JWT_ISSUER") private val jwtIssuer : String) {

    private val decodedBytes = Base64.getDecoder().decode(jwtSecret)

    private val jwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()

    private val jwtAuthenticationConverter = JwtAuthenticationConverter()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {

        setupJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter,jwtAuthenticationConverter)
        //if a user is making GET requests to /shows and /content shows, they don't need authorization
        //if a user is trying any other requests to other endpoints (except POST to the login endpoint)
        //then they need authorization

        http
            .csrf { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests {
                it
                    .requestMatchers(
                        HttpMethod.GET,
                        "/shows",
                        "/content/shows"
                    ).permitAll()
                    .requestMatchers(
                        HttpMethod.POST,
                        "/auth/login"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .oauth2ResourceServer { it.jwt {} }

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {

        return BCryptPasswordEncoder()

    }

    @Bean
    fun jwtEncoder(): JwtEncoder {

        val secretKeySpec = SecretKeySpec(decodedBytes,"HmacSHA256")
        val secret = ImmutableSecret<SecurityContext>(secretKeySpec)

        return NimbusJwtEncoder(secret)
    }

    @Bean
    fun jwtDecoder() : JwtDecoder {

        val secretKeySpec = SecretKeySpec(decodedBytes,"HmacSHA256")
        val decoder =  NimbusJwtDecoder.withSecretKey(secretKeySpec).macAlgorithm(MacAlgorithm.HS256).build()
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(jwtIssuer))

        return decoder
    }

    fun setupJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter : JwtGrantedAuthoritiesConverter, jwtAuthenticationConverter : JwtAuthenticationConverter){
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("role")
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_")
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter)
    }
}
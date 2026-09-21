package com.github.dersolopes.eventmanagement.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    // Carrega a chave secreta do seu application.properties ou application.yml
    // Garanta que esta chave tenha pelo menos 256 bits (32 caracteres) de comprimento
    @Value("${api.security.token.secret}")
    private String secretKey;

    @Value("${api.security.token.expiration-ms:86400000}") // Padrão: 24 horas
    private long expirationTime;

    /**
     * Gera um token JWT com o username do usuário.
     */
    public String generateToken(UserDetails userDetails) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);

        return JWT.create()
                .withIssuer("event-management-api") // Identifica quem gerou o token
                .withSubject(userDetails.getUsername()) // O dono do token
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .sign(algorithm);
    }

    /**
     * Extrai o 'subject' (username) de dentro de um token.
     */
    public String extractUsername(String token) {
        return decodeToken(token).getSubject();
    }

    /**
     * Valida se o token pertence ao usuário e se não está expirado.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            // Compara a String do username/email diretamente com a String do UserDetails
            return username != null && username.equalsIgnoreCase(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * Verifica se o token já expirou.
     */
    private boolean isTokenExpired(String token) {
        return decodeToken(token).getExpiresAt().before(new Date());
    }

    /**
     * Método auxiliar interno para decodificar e verificar a assinatura do token.
     */
    private DecodedJWT decodeToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);

        return JWT.require(algorithm)
                .withIssuer("event-management-api")
                .build()
                .verify(token); // Dispara JWTVerificationException se for inválido/expirado
    }
}

package com.gomezcapital.trading_journal.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(String email) {

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        return JWT.create()
                .withSubject(email)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpiration))
                .withIssuer("GomezCapital")
                .sign(algorithm);
    }

    public String extractUsername(String token) {
        DecodedJWT decodedJWT = JWT. require(Algorithm.HMAC256(secretKey))
                .withIssuer("GomezCapital")
                .build()
                .verify(token);
        return decodedJWT.getSubject();           
    }
}
package ru.abramov.FinFlow.FinFlow.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.time.ZonedDateTime;
import java.util.Date;

@Component
public class JWTUtil {

    @Value("${jwt_secret}")
    private String secret;

    public JWTUtil(String s) {
        this.secret = s;
    }

    public JWTUtil() {
    }

    public String generateAccessToken(String username){
        Date expirationDate = Date.from(ZonedDateTime.now().plusMinutes(60).toInstant());
        return JWT.create()
                .withSubject(username)
                .withClaim("type", "access")
                .withIssuedAt(new Date())
                .withExpiresAt(expirationDate)
                .withIssuer("FinFlow")
                .sign(Algorithm.HMAC256(secret));
    }

    public String generateRefreshToken(String username){
        Date expirationDate = Date.from(ZonedDateTime.now().plusDays(10).toInstant());
        return JWT.create()
                .withSubject(username)
                .withClaim("type", "refresh")
                .withIssuedAt(new Date())
                .withExpiresAt(expirationDate)
                .withIssuer("FinFlow")
                .sign(Algorithm.HMAC256(secret));

    }


    public String verifyAccessToken(String token) throws JWTVerificationException{
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withClaim("type", "access")
                .withIssuer("FinFlow")
                .build();
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getSubject();
    }

    public String verifyRefreshToken(String token) throws JWTVerificationException{
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withClaim("type", "refresh")
                .withIssuer("FinFlow")
                .build();
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getSubject();
    }

}

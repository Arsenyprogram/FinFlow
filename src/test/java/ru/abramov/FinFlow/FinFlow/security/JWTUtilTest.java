package ru.abramov.FinFlow.FinFlow.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JWTUtilUnitTest {

    private JWTUtil jwtUtil;
    private final String secret = "testsecret";
    private final String username = "Arseny";

    @BeforeEach
    void setUp() {
        jwtUtil = new JWTUtil(secret);
    }


    @Test
    void testGenerateAndVerifyAccessToken() {
        String token = jwtUtil.generateAccessToken(username);
        assertNotNull(token);

        String subject = jwtUtil.verifyAccessToken(token);
        assertEquals(username, subject);

        // Проверка claims
        DecodedJWT jwt = JWT.decode(token);
        assertEquals("access", jwt.getClaim("type").asString());
        assertEquals("FinFlow", jwt.getIssuer());

        Date now = new Date();
        assertTrue(jwt.getExpiresAt().after(now));
    }

    @Test
    void testAccessTokenCannotVerifyAsRefreshToken() {
        String accessToken = jwtUtil.generateAccessToken(username);
        assertThrows(JWTVerificationException.class, () -> jwtUtil.verifyRefreshToken(accessToken));
    }


    @Test
    void testGenerateAndVerifyRefreshToken() {
        String token = jwtUtil.generateRefreshToken(username);
        assertNotNull(token);

        String subject = jwtUtil.verifyRefreshToken(token);
        assertEquals(username, subject);

        DecodedJWT jwt = JWT.decode(token);
        assertEquals("refresh", jwt.getClaim("type").asString());
        assertEquals("FinFlow", jwt.getIssuer());

        Date now = new Date();
        assertTrue(jwt.getExpiresAt().after(now));
    }

    @Test
    void testRefreshTokenCannotVerifyAsAccessToken() {
        String refreshToken = jwtUtil.generateRefreshToken(username);
        assertThrows(JWTVerificationException.class, () -> jwtUtil.verifyAccessToken(refreshToken));
    }


    @Test
    void testInvalidTokenThrowsException() {
        String invalidToken = "invalid.token.string";
        assertThrows(JWTVerificationException.class, () -> jwtUtil.verifyAccessToken(invalidToken));
        assertThrows(JWTVerificationException.class, () -> jwtUtil.verifyRefreshToken(invalidToken));
    }

}


package cn.master.matrix.security;

import cn.master.matrix.entity.UserKey;
import cn.master.matrix.service.UserKeyService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @author Created by 11's papa on 06/21/2024
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    @Value("${matrix.meter.jwtExpirationMs}")
    private int jwtExpirationMs;

    private final UserKeyService userKeyService;

    SecretKey key = Keys.hmacShaKeyFor("a4a95385c5ed79118b720e6a7538c0af106905954235bb0aeb75a7ff89a05ef5".getBytes(StandardCharsets.UTF_8));

    public String generateJwtToken(Authentication authentication) {

        UserPrinciple userPrincipal = (UserPrinciple) authentication.getPrincipal();

        return Jwts.builder()
                .subject("Matrix-Meter")
                .claim("name", userPrincipal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(key).build().parse(authToken);
            return true;
        } catch (JwtException e) {
            log.error(e.getMessage());
        }
        return false;
    }

    public Jws<Claims> parseClaim(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }

    public String getUserNameFromJwtToken(String authToken) {
        val claimsJws = parseClaim(authToken);
        return claimsJws.getPayload().get("name", String.class);
    }

    public boolean isValid(String authToken, UserDetails userDetails) {
        String username = getUserNameFromJwtToken(authToken);
        boolean isValidToken = userKeyService.findByToken(authToken)
                .map(UserKey::getEnable).orElse(false);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(authToken) && isValidToken;
    }

    public boolean isTokenExpired(String authToken) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(authToken).getPayload().getExpiration().before(new Date());
    }
}

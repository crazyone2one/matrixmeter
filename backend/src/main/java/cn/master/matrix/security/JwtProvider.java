package cn.master.matrix.security;

import cn.master.matrix.entity.UserKey;
import cn.master.matrix.service.UserKeyService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Created by 11's papa on 06/21/2024
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    @Value("${matrix.meter.jwt.expiration}")
    private int jwtExpirationMs;
    @Value("${matrix.meter.jwt.refresh-expiration}")
    private int refreshExpiration;
    @Value("${matrix.meter.jwt.secret-key}")
    private String secretKey;

    private final UserKeyService userKeyService;

    //SecretKey key = Keys.hmacShaKeyFor("a4a95385c5ed79118b720e6a7538c0af106905954235bb0aeb75a7ff89a05ef5".getBytes(StandardCharsets.UTF_8));
    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Authentication authentication) {
        return generateJwtToken(authentication, jwtExpirationMs);
    }

    public String generateRefreshToken(Authentication authentication) {
        return generateJwtToken(authentication, refreshExpiration);
    }

    public String generateJwtToken(Authentication authentication, int expiration) {
        UserPrinciple userPrincipal = (UserPrinciple) authentication.getPrincipal();
        val authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", userPrincipal.getUsername());
        claims.put("auth", authorities);
        return Jwts.builder()
                .subject("MatrixMeter")
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + expiration))
                .signWith(getSecretKey())
                .compact();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException e) {
            log.error(e.getMessage());
        }
        return false;
    }

    public Jws<Claims> parseClaim(String token) {
        return Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token);
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
        return Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(authToken).getPayload().getExpiration().before(new Date());
    }
}

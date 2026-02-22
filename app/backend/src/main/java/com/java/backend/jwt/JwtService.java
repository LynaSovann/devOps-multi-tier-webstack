package com.java.backend.jwt;

import com.java.backend.model.User;
import com.java.backend.repository.AccountRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtService {
    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;
    public static final String SECRET = "3a4b43a1d54a24831b8bd3a004599a0057a2d45e091ae319f5c6a7a8322a773975f1436b1f2934cf6919be48f5701748cc8b821115c44865425b538ea089355f";

    private final AccountRepository accountRepository;

    public JwtService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    private String createToken(Map<String, Object> claim, String subject){
        return Jwts.builder()
                .setClaims(claim)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = Base64.getDecoder().decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        User appuser = (User) userDetails;

        User user = accountRepository.findByEmail(appuser.getEmail());

        if(user != null) {
            claims.put("userId", appuser.getUserId());
            return createToken(claims, userDetails.getUsername());
        } else {
            System.out.println("User not found");
            return null;
        }

    }

    private Claims extractAllClaim(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaim(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Boolean isTokenExpired(String token) {
        return extractExpirationDate(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

}

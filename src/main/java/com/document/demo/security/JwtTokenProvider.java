package com.document.demo.security;

import com.document.demo.models.User;
import com.document.demo.service.UserService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final UserService userService;
    private final StringRedisTemplate redisTemplate;
    
    private static final String BLACKLIST_PREFIX = "BLACKLIST:";
    private static final String REFRESH_BLACKLIST_PREFIX = "REFRESH_BLACKLIST:";
    
    @Value("${jwt.expiration}")
    private int jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private int jwtRefreshExpiration;

    private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof User user) {
            claims.put("userId", user.getUserId());
            claims.put("email", user.getEmail());
            claims.put("role", user.getRole().name());
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)  // Sử dụng key đã tạo
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateRefreshToken(userDetails);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof User user) {
            claims.put("userId", user.getUserId());
            claims.put("email", user.getEmail());
            claims.put("role", user.getRole().name());
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtRefreshExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)  // Sử dụng key đã tạo
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)  // Sử dụng key đã tạo
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String token, boolean isRefreshToken) {
        try {
            // Check if token is blacklisted
            String prefix = isRefreshToken ? REFRESH_BLACKLIST_PREFIX : BLACKLIST_PREFIX;
            String blacklistKey = prefix + token;
            Boolean isBlacklisted = redisTemplate.hasKey(blacklistKey);
            if (Boolean.TRUE.equals(isBlacklisted)) {
                log.error("Token is blacklisted");
                return false;
            }

            // Validate token signature and expiration
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }

    // Overloaded method for backward compatibility
    public boolean validateToken(String token) {
        return validateToken(token, false);
    }

    public String refreshToken(String refreshToken) {
        if (!validateToken(refreshToken, true)) {
            throw new RuntimeException("Invalid refresh token");
        }

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        String username = claims.getSubject();
        UserDetails userDetails = userService.loadUserByUsername(username);
        return generateToken(userDetails);
    }
    
    public void invalidateTokens(String accessToken, String refreshToken) {
        try {
            // Blacklist access token
            Claims accessClaims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(accessToken)
                .getBody();
            
            long accessTtl = (accessClaims.getExpiration().getTime() - System.currentTimeMillis()) / 1000;
            if (accessTtl > 0) {
                String blacklistKey = BLACKLIST_PREFIX + accessToken;
                redisTemplate.opsForValue().set(blacklistKey, "true", accessTtl, TimeUnit.SECONDS);
                log.info("Access token blacklisted successfully");
            }

            // Blacklist refresh token
            Claims refreshClaims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();
            
            long refreshTtl = (refreshClaims.getExpiration().getTime() - System.currentTimeMillis()) / 1000;
            if (refreshTtl > 0) {
                String refreshBlacklistKey = REFRESH_BLACKLIST_PREFIX + refreshToken;
                redisTemplate.opsForValue().set(refreshBlacklistKey, "true", refreshTtl, TimeUnit.SECONDS);
                log.info("Refresh token blacklisted successfully");
            }
        } catch (Exception e) {
            log.error("Error invalidating tokens: ", e);
        }
    }
}
package dbp.projectbackend.security;

import dbp.projectbackend.models.UserModel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    public String generateToken(UserModel user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .claim("role", user.getRole().name())
                .claim("empresaId", user.getEmpresa().getId())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(UserModel user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return isValid(token, userDetails, TYPE_ACCESS);
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        return isValid(token, userDetails, TYPE_REFRESH);
    }

    private boolean isValid(String token, UserDetails userDetails, String expectedType) {
        Claims claims = extractClaims(token);
        return expectedType.equals(claims.get(CLAIM_TYPE, String.class))
                && claims.getSubject().equals(userDetails.getUsername())
                && claims.getExpiration().after(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}

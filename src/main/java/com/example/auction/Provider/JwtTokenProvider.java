package com.example.auction.Provider;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenProvider {
    private static final String SECRET_KEY = "your-secret-key"; // 이 줄은 삭제하십시오.

    private static final byte[] SECRET_KEY_BYTES = Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded();

    public String createToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY_BYTES), SignatureAlgorithm.HS256)
                .compact();
    }
}

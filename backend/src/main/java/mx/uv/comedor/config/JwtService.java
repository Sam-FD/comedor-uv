package mx.uv.comedor.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import mx.uv.comedor.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiracionMs;

    public JwtService(
            @Value("${comedor.jwt.secret}") String secret,
            @Value("${comedor.jwt.expiracion-ms}") long expiracionMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date vencimiento = new Date(ahora.getTime() + expiracionMs);

        return Jwts.builder()
                .subject(usuario.getCredencialInstitucional())
                .claim("userId", usuario.getId())
                .claim("rol", usuario.getRol())
                .issuedAt(ahora)
                .expiration(vencimiento)
                .signWith(secretKey)
                .compact();
    }

    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Optional<Claims> validarYExtraerClaims(String token) {
        try {
            return Optional.of(extraerClaims(token));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}

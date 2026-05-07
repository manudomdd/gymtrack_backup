package app.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // Esta es tu firma digital. Está en Base64 y tiene el tamaño adecuado para ser segura.
    // (En un entorno de producción real, esto se pone en el application.properties)
    private static final String SECRET_KEY = "VGhpcy1pcy1hLXZlcnktc2VjdXJlLWtleS1mb3Itand0LW11c3QtYmUtYXQtbGVhc3QtMjU2LWJpdHM=";

    // 1. Método para generar el token (solo pasándole el usuario)
    public String generarToken(UserDetails userDetails) {
        return generarToken(new HashMap<>(), userDetails);
    }

    // 2. Método interno que construye el token con la librería Jjwt
    public String generarToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername()) // Usamos el email que definimos en getUsername() de la entidad User
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // El token durará 24 horas
                .signWith(getSignInKey())
                .compact();
    }

    // 3. Método para leer a quién pertenece el token (extrae el email)
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 4. Método para comprobar si el token es válido y no ha caducado
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // --- MÉTODOS AUXILIARES ---

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Transforma el String de la clave secreta en un objeto SecretKey que entiende la librería
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
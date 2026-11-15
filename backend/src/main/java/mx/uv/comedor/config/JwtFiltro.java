package mx.uv.comedor.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFiltro extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtFiltro(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            jwtService.validarYExtraerClaims(token).ifPresent(claims -> {
                Integer userId = claims.get("userId", Integer.class);
                String credencial = claims.getSubject();
                String rol = claims.get("rol", String.class);

                if (userId != null && credencial != null && rol != null && !rol.isBlank()) {
                    UsuarioAutenticado principal = new UsuarioAutenticado(userId, credencial, rol);
                    var autoridad = new SimpleGrantedAuthority("ROLE_" + rol.trim().toUpperCase());
                    var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of(autoridad));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            });
        }

        filterChain.doFilter(request, response);
    }
}

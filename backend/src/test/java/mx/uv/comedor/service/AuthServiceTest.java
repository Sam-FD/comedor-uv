package mx.uv.comedor.service;

import mx.uv.comedor.config.JwtService;
import mx.uv.comedor.config.ReglasComedorProperties;
import mx.uv.comedor.dto.AuthDtos.AuthResponse;
import mx.uv.comedor.dto.AuthDtos.LoginRequest;
import mx.uv.comedor.dto.AuthDtos.RegistroRequest;
import mx.uv.comedor.exception.ReglaNegocioException;
import mx.uv.comedor.model.ConstantesComedor;
import mx.uv.comedor.model.Usuario;
import mx.uv.comedor.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void configurar() {
        usuarioRepository = mock(UsuarioRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        jwtService = new JwtService(
                "ClaveDePruebaParaFirmaJWTComedorUV2026ConLongitudSuficiente12345",
                43200000L
        );
        RestriccionService restriccionService = new RestriccionService(new ReglasComedorProperties());
        authService = new AuthService(usuarioRepository, passwordEncoder, jwtService, restriccionService);
    }

    @Test
    void registrarEstudianteNormalizaCamposYGeneraJwt() {
        when(usuarioRepository.existsByCredencialInstitucionalIgnoreCase("S24099999")).thenReturn(false);
        when(usuarioRepository.existsByCorreoIgnoreCase("alumno@estudiantes.uv.mx")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(99);
            return u;
        });

        AuthResponse res = authService.registrarEstudiante(new RegistroRequest(
                " s24099999 ",
                " Alumno Prueba ",
                " ALUMNO@ESTUDIANTES.UV.MX ",
                "clave123"
        ));

        assertEquals("S24099999", res.usuario().credencialInstitucional());
        assertEquals("alumno@estudiantes.uv.mx", res.usuario().correo());
        assertEquals(ConstantesComedor.ROL_ESTUDIANTE, res.usuario().rol());
        assertFalse(res.token().isBlank());
        assertEquals(99, jwtService.extraerClaims(res.token()).get("userId", Integer.class));
    }

    @Test
    void loginConContrasenaIncorrectaLanzaUnauthorized() {
        Usuario u = new Usuario();
        u.setId(1);
        u.setCredencialInstitucional("S22014510");
        u.setPasswordHash(passwordEncoder.encode("uv2026"));
        u.setRol(ConstantesComedor.ROL_ESTUDIANTE);
        when(usuarioRepository.findByCredencialInstitucionalIgnoreCase("S22014510")).thenReturn(Optional.of(u));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                authService.iniciarSesion(new LoginRequest("S22014510", "otra_clave"))
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }
}

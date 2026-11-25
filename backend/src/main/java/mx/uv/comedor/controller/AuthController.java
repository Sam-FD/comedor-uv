package mx.uv.comedor.controller;

import jakarta.validation.Valid;
import mx.uv.comedor.config.UsuarioAutenticado;
import mx.uv.comedor.dto.AuthDtos.AuthResponse;
import mx.uv.comedor.dto.AuthDtos.LoginRequest;
import mx.uv.comedor.dto.AuthDtos.RegistroRequest;
import mx.uv.comedor.dto.AuthDtos.UsuarioDto;
import mx.uv.comedor.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.iniciarSesion(request));
    }

    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registro(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrarEstudiante(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioDto> perfilActual(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(authService.obtenerPerfil(usuario.id()));
    }
}

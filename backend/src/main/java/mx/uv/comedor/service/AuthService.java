package mx.uv.comedor.service;

import mx.uv.comedor.config.JwtService;
import mx.uv.comedor.dto.AuthDtos.AuthResponse;
import mx.uv.comedor.dto.AuthDtos.LoginRequest;
import mx.uv.comedor.dto.AuthDtos.RegistroRequest;
import mx.uv.comedor.dto.AuthDtos.UsuarioDto;
import mx.uv.comedor.exception.RecursoNoEncontradoException;
import mx.uv.comedor.exception.ReglaNegocioException;
import mx.uv.comedor.model.ConstantesComedor;
import mx.uv.comedor.model.Usuario;
import mx.uv.comedor.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RestriccionService restriccionService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RestriccionService restriccionService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.restriccionService = restriccionService;
    }

    @Transactional
    public AuthResponse iniciarSesion(LoginRequest request) {
        String entrada = request.credencialOCorreo().trim();

        Usuario usuario = usuarioRepository.findByCredencialInstitucionalIgnoreCase(entrada)
                .or(() -> usuarioRepository.findByCorreoIgnoreCase(entrada))
                .orElseThrow(() -> new ReglaNegocioException(
                        "Credenciales incorrectas. Verifica tu matrícula y contraseña.",
                        HttpStatus.UNAUTHORIZED
                ));

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new ReglaNegocioException(
                    "Credenciales incorrectas. Verifica tu matrícula y contraseña.",
                    HttpStatus.UNAUTHORIZED
            );
        }

        if (restriccionService.evaluarEstadoRestricciones(usuario, LocalDateTime.now())) {
            usuario = usuarioRepository.save(usuario);
        }

        String token = jwtService.generarToken(usuario);
        return new AuthResponse(token, UsuarioDto.desdeEntidad(usuario));
    }

    @Transactional
    public AuthResponse registrarEstudiante(RegistroRequest request) {
        String credencial = request.credencialInstitucional().trim().toUpperCase();
        String correo = request.correo().trim().toLowerCase();

        if (usuarioRepository.existsByCredencialInstitucionalIgnoreCase(credencial)) {
            throw new ReglaNegocioException("Ya existe una cuenta registrada con esa matrícula", HttpStatus.CONFLICT);
        }
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ReglaNegocioException("Ya existe una cuenta registrada con ese correo", HttpStatus.CONFLICT);
        }

        Usuario nuevo = new Usuario();
        nuevo.setCredencialInstitucional(credencial);
        nuevo.setNombre(request.nombre().trim());
        nuevo.setCorreo(correo);
        nuevo.setPasswordHash(passwordEncoder.encode(request.password()));
        nuevo.setRol(ConstantesComedor.ROL_ESTUDIANTE);
        nuevo.setFaltasAbandono(0);
        nuevo.setNivelRestriccion(ConstantesComedor.RESTRICCION_NINGUNA);

        Usuario guardado = usuarioRepository.save(nuevo);
        String token = jwtService.generarToken(guardado);
        return new AuthResponse(token, UsuarioDto.desdeEntidad(guardado));
    }

    @Transactional
    public UsuarioDto obtenerPerfil(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (restriccionService.evaluarEstadoRestricciones(usuario, LocalDateTime.now())) {
            usuario = usuarioRepository.save(usuario);
        }
        return UsuarioDto.desdeEntidad(usuario);
    }
}

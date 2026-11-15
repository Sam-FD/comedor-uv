package mx.uv.comedor.dto;

public record AuthResponse(
        String token,
        UsuarioDto usuario
) {
}

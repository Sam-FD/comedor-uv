package mx.uv.comedor.config;

public record UsuarioAutenticado(
        Integer id,
        String credencial,
        String rol
) {
}

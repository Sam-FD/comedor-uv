package mx.uv.comedor.dto;

import mx.uv.comedor.model.Usuario;

import java.time.LocalDateTime;

public record UsuarioDto(
        Integer id,
        String credencialInstitucional,
        String nombre,
        String correo,
        String rol,
        Integer faltasAbandono,
        LocalDateTime fechaUltimaFalta,
        String nivelRestriccion,
        LocalDateTime restriccionHasta
) {
    public static UsuarioDto desdeEntidad(Usuario usuario) {
        return new UsuarioDto(
                usuario.getId(),
                usuario.getCredencialInstitucional(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.getFaltasAbandono(),
                usuario.getFechaUltimaFalta(),
                usuario.getNivelRestriccion(),
                usuario.getRestriccionHasta()
        );
    }
}

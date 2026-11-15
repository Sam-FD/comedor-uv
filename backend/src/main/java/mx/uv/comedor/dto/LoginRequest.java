package mx.uv.comedor.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Ingresa tu matricula, credencial o correo institucional")
        String credencialOCorreo,
        @NotBlank(message = "Ingresa tu contraseña")
        String password
) {
}

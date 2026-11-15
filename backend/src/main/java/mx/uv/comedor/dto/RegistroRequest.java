package mx.uv.comedor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = "La matrícula institucional es obligatoria")
        @Size(max = 30, message = "La matrícula no puede exceder 30 caracteres")
        String credencialInstitucional,
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombre,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Ingresa un correo válido")
        @Size(max = 100, message = "El correo no puede exceder 100 caracteres")
        String correo,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password
) {
}

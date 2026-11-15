package mx.uv.comedor.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ComidaDelDiaRequest(
        @NotBlank(message = "El nombre de la comida del día es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombre,
        String descripcion,
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "1.00", message = "El precio debe ser mayor a 0")
        BigDecimal precio,
        @NotBlank(message = "El turno es obligatorio")
        String turno
) {
}

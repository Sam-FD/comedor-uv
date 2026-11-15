package mx.uv.comedor.dto;

import jakarta.validation.constraints.NotNull;

public record DisponibilidadRequest(
        @NotNull(message = "El estado de disponibilidad es obligatorio")
        Boolean disponible
) {
}

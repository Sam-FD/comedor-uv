package mx.uv.comedor.dto;

import jakarta.validation.constraints.NotNull;

public record RecepcionRequest(
        @NotNull(message = "El estado de recepcion es obligatorio")
        Boolean abierta
) {
}

package mx.uv.comedor.dto;

import jakarta.validation.constraints.NotNull;

public record RecepcionRequest(
        @NotNull(message = "El estado de recepción es obligatorio")
        Boolean abierta
) {
}

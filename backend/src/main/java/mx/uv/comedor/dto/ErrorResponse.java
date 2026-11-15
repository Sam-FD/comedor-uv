package mx.uv.comedor.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        String mensaje,
        LocalDateTime fecha
) {
}

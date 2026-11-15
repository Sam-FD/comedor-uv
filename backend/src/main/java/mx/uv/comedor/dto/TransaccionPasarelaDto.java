package mx.uv.comedor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransaccionPasarelaDto(
        String transaccionId,
        String modo,
        String estadoPasarela,
        String estadoComedor,
        String ultimosDigitos,
        BigDecimal monto,
        String moneda,
        LocalDateTime fechaActualizacion
) {
}

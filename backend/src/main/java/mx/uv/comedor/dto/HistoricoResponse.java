package mx.uv.comedor.dto;

import java.math.BigDecimal;
import java.util.List;

public record HistoricoResponse(
        List<PedidoDto> pedidos,
        long totalEntregados,
        long totalNoReclamados,
        BigDecimal montoCobradoApp,
        BigDecimal montoCobradoMostrador,
        BigDecimal montoTotalCobrado
) {
}

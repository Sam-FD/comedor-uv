package mx.uv.comedor.dto;

import mx.uv.comedor.model.Pedido;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoDto(
        Integer id,
        String referencia,
        Integer usuarioId,
        String nombreEstudiante,
        String credencialEstudiante,
        String turno,
        LocalDate fechaOperativa,
        BigDecimal total,
        String modalidadPago,
        String estadoPago,
        String estadoPedido,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaListo,
        LocalDateTime fechaLimiteRecoleccion,
        LocalDateTime fechaCierre,
        List<DetallePedidoDto> detalles
) {
    public static PedidoDto desdeEntidad(Pedido pedido) {
        List<DetallePedidoDto> detallesDto = pedido.getDetalles().stream()
                .map(DetallePedidoDto::desdeEntidad)
                .toList();
        return new PedidoDto(
                pedido.getId(),
                pedido.getReferencia(),
                pedido.getUsuario().getId(),
                pedido.getUsuario().getNombre(),
                pedido.getUsuario().getCredencialInstitucional(),
                pedido.getTurno(),
                pedido.getFechaOperativa(),
                pedido.getTotal(),
                pedido.getModalidadPago(),
                pedido.getEstadoPago(),
                pedido.getEstadoPedido(),
                pedido.getFechaCreacion(),
                pedido.getFechaListo(),
                pedido.getFechaLimiteRecoleccion(),
                pedido.getFechaCierre(),
                detallesDto
        );
    }
}

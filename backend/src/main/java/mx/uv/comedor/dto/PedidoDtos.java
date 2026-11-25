package mx.uv.comedor.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import mx.uv.comedor.model.DetallePedido;
import mx.uv.comedor.model.Pedido;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class PedidoDtos {

    private PedidoDtos() {
    }

    public record ItemPedidoRequest(
            @NotNull(message = "Falta el identificador del platillo")
            Integer platilloId,
            @NotNull(message = "La cantidad es obligatoria")
            @Min(value = 1, message = "La cantidad mínima es 1")
            Integer cantidad
    ) {
    }

    public record CrearPedidoRequest(
            @NotBlank(message = "El turno del pedido es obligatorio")
            String turno,
            @NotBlank(message = "La modalidad de pago es obligatoria")
            String modalidadPago,
            @NotEmpty(message = "Debes agregar al menos un platillo al carrito")
            @Valid
            List<ItemPedidoRequest> items,
            String numeroTarjeta
    ) {
    }

    public record DetallePedidoDto(
            Integer id,
            Integer platilloId,
            String nombrePlatillo,
            String tipoMenu,
            Integer cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal
    ) {
        public static DetallePedidoDto desdeEntidad(DetallePedido detalle) {
            return new DetallePedidoDto(
                    detalle.getId(),
                    detalle.getPlatillo().getId(),
                    detalle.getPlatillo().getNombre(),
                    detalle.getPlatillo().getTipoMenu(),
                    detalle.getCantidad(),
                    detalle.getPrecioUnitario(),
                    detalle.getSubtotal()
            );
        }
    }

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

    public record CambiarEstadoPedidoRequest(
            @NotBlank(message = "El nuevo estado del pedido es obligatorio")
            String estadoPedido
    ) {
    }

    public record HistoricoResponse(
            List<PedidoDto> pedidos,
            long totalEntregados,
            long totalNoReclamados,
            BigDecimal montoCobradoApp,
            BigDecimal montoCobradoMostrador,
            BigDecimal montoTotalCobrado
    ) {
    }
}

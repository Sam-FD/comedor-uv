package mx.uv.comedor.dto;

import mx.uv.comedor.model.DetallePedido;

import java.math.BigDecimal;

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

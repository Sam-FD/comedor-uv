package mx.uv.comedor.dto;

import jakarta.validation.constraints.NotBlank;

public record CambiarEstadoPedidoRequest(
        @NotBlank(message = "El nuevo estado del pedido es obligatorio")
        String estadoPedido
) {
}

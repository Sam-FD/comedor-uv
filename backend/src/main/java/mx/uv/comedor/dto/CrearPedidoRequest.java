package mx.uv.comedor.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CrearPedidoRequest(
        @NotBlank(message = "El turno del pedido es obligatorio")
        String turno,
        @NotBlank(message = "La modalidad de pago es obligatoria")
        String modalidadPago,
        @NotEmpty(message = "Debes agregar al menos un platillo al carrito")
        @Valid
        List<ItemPedidoRequest> items,
        String tarjetaPrueba
) {
}

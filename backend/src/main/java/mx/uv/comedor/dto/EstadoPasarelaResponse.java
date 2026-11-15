package mx.uv.comedor.dto;

import java.util.List;

public record EstadoPasarelaResponse(
        String modo,
        String nombreModo,
        String moneda,
        List<TarjetaPruebaInfo> tarjetasPrueba
) {
    public record TarjetaPruebaInfo(
            String etiqueta,
            String numero,
            String resultado
    ) {
    }
}

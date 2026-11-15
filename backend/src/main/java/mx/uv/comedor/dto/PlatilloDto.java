package mx.uv.comedor.dto;

import mx.uv.comedor.model.Platillo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PlatilloDto(
        Integer id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        String tipoMenu,
        String turno,
        LocalDate fechaOperativa,
        Boolean disponible
) {
    public static PlatilloDto desdeEntidad(Platillo platillo) {
        return new PlatilloDto(
                platillo.getId(),
                platillo.getNombre(),
                platillo.getDescripcion(),
                platillo.getPrecio(),
                platillo.getTipoMenu(),
                platillo.getTurno(),
                platillo.getFechaOperativa(),
                platillo.getDisponible()
        );
    }
}

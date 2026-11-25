package mx.uv.comedor.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mx.uv.comedor.model.Platillo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class PlatilloDtos {

    private PlatilloDtos() {
    }

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

    public record CatalogoResponse(
            LocalDate fechaOperativa,
            String turnoSugerido,
            boolean recepcionAbierta,
            List<PlatilloDto> comidasDelDia,
            List<PlatilloDto> menuRegular
    ) {
    }

    public record ComidaDelDiaRequest(
            @NotBlank(message = "El nombre de la comida del día es obligatorio")
            @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
            String nombre,
            String descripcion,
            @NotNull(message = "El precio es obligatorio")
            @DecimalMin(value = "1.00", message = "El precio debe ser mayor a 0")
            BigDecimal precio,
            @NotBlank(message = "El turno es obligatorio")
            String turno
    ) {
    }

    public record PlatilloRegularRequest(
            @NotBlank(message = "El nombre del platillo es obligatorio")
            @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
            String nombre,
            String descripcion,
            @NotNull(message = "El precio es obligatorio")
            @DecimalMin(value = "1.00", message = "El precio debe ser mayor a 0")
            BigDecimal precio,
            @NotBlank(message = "El turno es obligatorio")
            String turno
    ) {
    }

    public record DisponibilidadRequest(
            @NotNull(message = "El estado de disponibilidad es obligatorio")
            Boolean disponible
    ) {
    }

    public record RecepcionRequest(
            @NotNull(message = "El estado de recepción es obligatorio")
            Boolean abierta
    ) {
    }
}

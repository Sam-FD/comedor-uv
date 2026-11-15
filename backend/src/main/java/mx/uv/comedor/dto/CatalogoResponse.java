package mx.uv.comedor.dto;

import java.time.LocalDate;
import java.util.List;

public record CatalogoResponse(
        LocalDate fechaOperativa,
        String turnoSugerido,
        boolean recepcionAbierta,
        List<PlatilloDto> comidasDelDia,
        List<PlatilloDto> menuRegular
) {
}

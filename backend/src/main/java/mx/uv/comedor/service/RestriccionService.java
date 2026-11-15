package mx.uv.comedor.service;

import mx.uv.comedor.config.ReglasComedorProperties;
import mx.uv.comedor.exception.ReglaNegocioException;
import mx.uv.comedor.model.ConstantesComedor;
import mx.uv.comedor.model.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class RestriccionService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ReglasComedorProperties reglas;

    public RestriccionService(ReglasComedorProperties reglas) {
        this.reglas = reglas;
    }

    public boolean evaluarEstadoRestricciones(Usuario usuario, LocalDateTime ahora) {
        boolean huboCambio = false;

        // Si ya vencio el plazo de restriccion, se restablece la cuenta a estado normal
        if (!ConstantesComedor.RESTRICCION_NINGUNA.equals(usuario.getNivelRestriccion())
                && usuario.getRestriccionHasta() != null
                && !usuario.getRestriccionHasta().isAfter(ahora)) {
            usuario.setNivelRestriccion(ConstantesComedor.RESTRICCION_NINGUNA);
            usuario.setRestriccionHasta(null);
            usuario.setFaltasAbandono(0);
            usuario.setFechaUltimaFalta(null);
            huboCambio = true;
        }

        // Si tiene 1 o 2 faltas sin restriccion y pasaron 60 dias sin reincidir, se reinicia el contador
        if (ConstantesComedor.RESTRICCION_NINGUNA.equals(usuario.getNivelRestriccion())
                && usuario.getFaltasAbandono() > 0
                && usuario.getFechaUltimaFalta() != null
                && !usuario.getFechaUltimaFalta().plusDays(reglas.getDiasCorteLimpio()).isAfter(ahora)) {
            usuario.setFaltasAbandono(0);
            usuario.setFechaUltimaFalta(null);
            huboCambio = true;
        }

        return huboCambio;
    }

    public Usuario registrarFaltaYActualizarRestriccion(Usuario usuario, LocalDateTime ahora) {
        evaluarEstadoRestricciones(usuario, ahora);

        int nuevasFaltas = usuario.getFaltasAbandono() + 1;
        usuario.setFaltasAbandono(nuevasFaltas);
        usuario.setFechaUltimaFalta(ahora);

        if (ConstantesComedor.RESTRICCION_NINGUNA.equals(usuario.getNivelRestriccion())
                && nuevasFaltas >= reglas.getUmbralNivel1()) {
            usuario.setNivelRestriccion(ConstantesComedor.RESTRICCION_SIN_MOSTRADOR);
            usuario.setRestriccionHasta(ahora.plusMonths(reglas.getDuracionNivel1Meses()));
        } else if (ConstantesComedor.RESTRICCION_SIN_MOSTRADOR.equals(usuario.getNivelRestriccion())
                && nuevasFaltas >= reglas.getUmbralNivel2()) {
            LocalDateTime base = usuario.getRestriccionHasta() != null
                    ? usuario.getRestriccionHasta()
                    : ahora.plusMonths(reglas.getDuracionNivel1Meses());
            usuario.setNivelRestriccion(ConstantesComedor.RESTRICCION_SIN_PEDIDOS);
            usuario.setRestriccionHasta(base.plusDays(reglas.getExtensionNivel2Dias()));
        }

        return usuario;
    }

    public void validarPermisoDePedido(Usuario usuario, String modalidadPago, LocalDateTime ahora) {
        evaluarEstadoRestricciones(usuario, ahora);

        if (ConstantesComedor.RESTRICCION_SIN_PEDIDOS.equals(usuario.getNivelRestriccion())) {
            String fechaTexto = formatearFecha(usuario.getRestriccionHasta());
            throw new ReglaNegocioException(
                    "Tu cuenta tiene suspendidos los pedidos en línea por acumular 4 faltas de recolección. "
                            + "Solo puedes consultar el menú hasta el " + fechaTexto + ".",
                    HttpStatus.FORBIDDEN
            );
        }

        if (ConstantesComedor.RESTRICCION_SIN_MOSTRADOR.equals(usuario.getNivelRestriccion())
                && ConstantesComedor.PAGO_MOSTRADOR.equals(modalidadPago)) {
            String fechaTexto = formatearFecha(usuario.getRestriccionHasta());
            throw new ReglaNegocioException(
                    "Por acumular 3 faltas de recolección, no puedes usar pago en mostrador hasta el "
                            + fechaTexto + ". Solo puedes ordenar pagando con tarjeta desde la aplicación.",
                    HttpStatus.FORBIDDEN
            );
        }
    }

    private String formatearFecha(LocalDateTime fecha) {
        return fecha != null ? fecha.format(FORMATO_FECHA) : "próximo corte";
    }
}

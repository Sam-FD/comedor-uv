package mx.uv.comedor.service;

import mx.uv.comedor.config.ReglasComedorProperties;
import mx.uv.comedor.exception.ReglaNegocioException;
import mx.uv.comedor.model.ConstantesComedor;
import mx.uv.comedor.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestriccionServiceTest {

    private RestriccionService restriccionService;

    @BeforeEach
    void configurar() {
        restriccionService = new RestriccionService(new ReglasComedorProperties());
    }

    @Test
    void alLlegarATresFaltasAplicaRestriccionSinMostradorPorUnMes() {
        LocalDateTime ahora = LocalDateTime.of(2026, 11, 11, 12, 0);

        Usuario u = new Usuario();
        u.setFaltasAbandono(2);
        u.setFechaUltimaFalta(ahora.minusDays(3));
        u.setNivelRestriccion(ConstantesComedor.RESTRICCION_NINGUNA);

        Usuario actualizado = restriccionService.registrarFaltaYActualizarRestriccion(u, ahora);

        assertEquals(3, actualizado.getFaltasAbandono());
        assertEquals(ConstantesComedor.RESTRICCION_SIN_MOSTRADOR, actualizado.getNivelRestriccion());
        assertEquals(ahora.plusMonths(1), actualizado.getRestriccionHasta());

        assertThrows(ReglaNegocioException.class, () ->
                restriccionService.validarPermisoDePedido(actualizado, ConstantesComedor.PAGO_MOSTRADOR, ahora)
        );
        restriccionService.validarPermisoDePedido(actualizado, ConstantesComedor.PAGO_APP_TARJETA, ahora);
    }

    @Test
    void cuartaFaltaEscalaASinPedidosYExtiendeQuinceDias() {
        LocalDateTime ahora = LocalDateTime.of(2026, 11, 11, 12, 0);
        LocalDateTime vencimientoPrevio = ahora.plusDays(20);

        Usuario u = new Usuario();
        u.setFaltasAbandono(3);
        u.setFechaUltimaFalta(ahora.minusDays(2));
        u.setNivelRestriccion(ConstantesComedor.RESTRICCION_SIN_MOSTRADOR);
        u.setRestriccionHasta(vencimientoPrevio);

        Usuario actualizado = restriccionService.registrarFaltaYActualizarRestriccion(u, ahora);

        assertEquals(4, actualizado.getFaltasAbandono());
        assertEquals(ConstantesComedor.RESTRICCION_SIN_PEDIDOS, actualizado.getNivelRestriccion());
        assertEquals(vencimientoPrevio.plusDays(15), actualizado.getRestriccionHasta());

        assertThrows(ReglaNegocioException.class, () ->
                restriccionService.validarPermisoDePedido(actualizado, ConstantesComedor.PAGO_APP_TARJETA, ahora)
        );
    }

    @Test
    void alVencerRestriccionSeReinicianFaltasYNivel() {
        LocalDateTime ahora = LocalDateTime.of(2026, 12, 15, 10, 0);

        Usuario u = new Usuario();
        u.setFaltasAbandono(3);
        u.setFechaUltimaFalta(ahora.minusMonths(1).minusDays(2));
        u.setNivelRestriccion(ConstantesComedor.RESTRICCION_SIN_MOSTRADOR);
        u.setRestriccionHasta(ahora.minusHours(1));

        boolean cambio = restriccionService.evaluarEstadoRestricciones(u, ahora);

        assertTrue(cambio);
        assertEquals(0, u.getFaltasAbandono());
        assertEquals(ConstantesComedor.RESTRICCION_NINGUNA, u.getNivelRestriccion());
        assertNull(u.getRestriccionHasta());
        assertNull(u.getFechaUltimaFalta());
    }

    @Test
    void alPasarSesentaDiasSinReincidirSeReinicianUnaODosFaltas() {
        LocalDateTime ahora = LocalDateTime.of(2026, 11, 11, 12, 0);

        Usuario u = new Usuario();
        u.setFaltasAbandono(2);
        u.setFechaUltimaFalta(ahora.minusDays(60));
        u.setNivelRestriccion(ConstantesComedor.RESTRICCION_NINGUNA);

        boolean cambio = restriccionService.evaluarEstadoRestricciones(u, ahora);

        assertTrue(cambio);
        assertEquals(0, u.getFaltasAbandono());
        assertNull(u.getFechaUltimaFalta());
    }
}

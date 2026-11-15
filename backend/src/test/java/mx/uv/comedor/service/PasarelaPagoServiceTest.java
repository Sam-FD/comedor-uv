package mx.uv.comedor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.uv.comedor.config.PasarelaPagoProperties;
import mx.uv.comedor.dto.TransaccionPasarelaDto;
import mx.uv.comedor.exception.ReglaNegocioException;
import mx.uv.comedor.model.ConstantesComedor;
import mx.uv.comedor.model.TransaccionPasarela;
import mx.uv.comedor.repository.TransaccionPasarelaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PasarelaPagoServiceTest {

    private TransaccionPasarelaRepository transaccionRepository;
    private PasarelaPagoService pasarelaPagoService;

    @BeforeEach
    void configurar() {
        PasarelaPagoProperties props = new PasarelaPagoProperties();
        props.setStripeSecretKey("");
        props.setMoneda("mxn");

        Map<Integer, TransaccionPasarela> almacenamiento = new ConcurrentHashMap<>();
        transaccionRepository = mock(TransaccionPasarelaRepository.class);
        when(transaccionRepository.findByPedidoId(anyInt()))
                .thenAnswer(inv -> Optional.ofNullable(almacenamiento.get(inv.getArgument(0))));
        when(transaccionRepository.save(any(TransaccionPasarela.class)))
                .thenAnswer(inv -> {
                    TransaccionPasarela t = inv.getArgument(0);
                    if (t.getId() == null) {
                        t.setId(almacenamiento.size() + 1);
                    }
                    almacenamiento.put(t.getPedidoId(), t);
                    return t;
                });

        pasarelaPagoService = new PasarelaPagoService(props, new ObjectMapper(), transaccionRepository);
    }

    @Test
    void tarjetaPruebaAprobadaGeneraRetencionYcobraAlEntregar() {
        assertFalse(pasarelaPagoService.usaStripeReal());

        TransaccionPasarelaDto tx = pasarelaPagoService.autorizarRetencion(
                "A010",
                new BigDecimal("35.00"),
                "4242 4242 4242 4242"
        );
        assertEquals("requires_capture", tx.estadoPasarela());
        assertEquals(ConstantesComedor.ESTADO_PAGO_RETENIDO, tx.estadoComedor());
        assertEquals("4242", tx.ultimosDigitos());

        pasarelaPagoService.asociarPedido(101, tx);
        pasarelaPagoService.confirmarCobro(101);

        TransaccionPasarelaDto cobrada = pasarelaPagoService.obtenerPorPedido(101).orElseThrow();
        assertEquals("succeeded", cobrada.estadoPasarela());
        assertEquals(ConstantesComedor.ESTADO_PAGO_COBRADO, cobrada.estadoComedor());
    }

    @Test
    void tarjetaPruebaDeclinadaLanzaErrorPagoRequerido() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                pasarelaPagoService.autorizarRetencion("A011", new BigDecimal("50.00"), "4000 0000 0000 0002")
        );
        assertEquals(HttpStatus.PAYMENT_REQUIRED, ex.getStatus());
    }

    @Test
    void liberarRetencionMarcaTransaccionComoCancelada() {
        TransaccionPasarelaDto tx = pasarelaPagoService.autorizarRetencion(
                "A012",
                new BigDecimal("40.00"),
                "4242424242424242"
        );
        pasarelaPagoService.asociarPedido(102, tx);
        pasarelaPagoService.liberarRetencion(102);

        TransaccionPasarelaDto liberada = pasarelaPagoService.obtenerPorPedido(102).orElseThrow();
        assertEquals("canceled", liberada.estadoPasarela());
        assertEquals(ConstantesComedor.ESTADO_PAGO_LIBERADO, liberada.estadoComedor());
    }

    @Test
    void detectaStripeRealCuandoHayClaveSkTest() {
        PasarelaPagoProperties props = new PasarelaPagoProperties();
        props.setStripeSecretKey("sk_test_demo_123456");
        PasarelaPagoService servicioStripe = new PasarelaPagoService(props, new ObjectMapper(), transaccionRepository);

        assertTrue(servicioStripe.usaStripeReal());
        assertEquals(PasarelaPagoService.MODO_STRIPE_TEST, servicioStripe.obtenerEstadoPasarela().modo());
    }
}

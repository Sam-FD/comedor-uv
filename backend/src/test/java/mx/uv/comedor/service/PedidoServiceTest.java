package mx.uv.comedor.service;

import mx.uv.comedor.config.ReglasComedorProperties;
import mx.uv.comedor.dto.CrearPedidoRequest;
import mx.uv.comedor.dto.ItemPedidoRequest;
import mx.uv.comedor.dto.PedidoDto;
import mx.uv.comedor.dto.TransaccionPasarelaDto;
import mx.uv.comedor.exception.ReglaNegocioException;
import mx.uv.comedor.model.ConstantesComedor;
import mx.uv.comedor.model.Pedido;
import mx.uv.comedor.model.Platillo;
import mx.uv.comedor.model.Usuario;
import mx.uv.comedor.repository.PedidoRepository;
import mx.uv.comedor.repository.PlatilloRepository;
import mx.uv.comedor.repository.UsuarioRepository;
import mx.uv.comedor.socket.SocketNotificador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PedidoServiceTest {

    private PedidoRepository pedidoRepository;
    private PlatilloRepository platilloRepository;
    private UsuarioRepository usuarioRepository;
    private RestriccionService restriccionService;
    private EstadoOperacionService estadoOperacionService;
    private PasarelaPagoService pasarelaPagoService;
    private SocketNotificador socketNotificador;
    private PedidoService pedidoService;

    private Usuario estudiante;
    private Platillo platilloRegular;

    @BeforeEach
    void configurar() {
        pedidoRepository = mock(PedidoRepository.class);
        platilloRepository = mock(PlatilloRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        restriccionService = new RestriccionService(new ReglasComedorProperties());
        estadoOperacionService = mock(EstadoOperacionService.class);
        pasarelaPagoService = mock(PasarelaPagoService.class);
        socketNotificador = mock(SocketNotificador.class);

        pedidoService = new PedidoService(
                pedidoRepository,
                platilloRepository,
                usuarioRepository,
                restriccionService,
                estadoOperacionService,
                pasarelaPagoService,
                new ReglasComedorProperties(),
                socketNotificador
        );

        estudiante = new Usuario();
        estudiante.setId(1);
        estudiante.setCredencialInstitucional("S22014510");
        estudiante.setNombre("Laura Mendoza");
        estudiante.setCorreo("l.mendoza@estudiantes.uv.mx");
        estudiante.setRol(ConstantesComedor.ROL_ESTUDIANTE);
        estudiante.setFaltasAbandono(0);
        estudiante.setNivelRestriccion(ConstantesComedor.RESTRICCION_NINGUNA);

        platilloRegular = new Platillo();
        platilloRegular.setId(10);
        platilloRegular.setNombre("Molletes gratinados");
        platilloRegular.setPrecio(new BigDecimal("38.00"));
        platilloRegular.setTipoMenu(ConstantesComedor.MENU_REGULAR);
        platilloRegular.setTurno(ConstantesComedor.TURNO_AMBOS);
        platilloRegular.setDisponible(true);

        when(estadoOperacionService.isRecepcionAbierta()).thenReturn(true);
        when(pedidoRepository.findPedidosListosVencidos(eq(ConstantesComedor.ESTADO_LISTO), any()))
                .thenReturn(List.of());
    }

    @Test
    void crearPedidoMostradorGeneraReferenciaYTotalCorrecto() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(estudiante));
        when(pedidoRepository.findActivosPorUsuario(eq(1), anyList())).thenReturn(List.of());
        when(pedidoRepository.findReferenciasDelDiaDesc(any())).thenReturn(List.of("A001"));
        when(platilloRepository.findById(10)).thenReturn(Optional.of(platilloRegular));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setId(77);
            return p;
        });

        PedidoDto creado = pedidoService.crearPedido(1, new CrearPedidoRequest(
                "matutino",
                "mostrador",
                List.of(new ItemPedidoRequest(10, 2)),
                null
        ));

        assertEquals("A002", creado.referencia());
        assertEquals(new BigDecimal("76.00"), creado.total());
        assertEquals(ConstantesComedor.ESTADO_RECIBIDO, creado.estadoPedido());
        assertEquals(ConstantesComedor.ESTADO_PAGO_PENDIENTE_MOSTRADOR, creado.estadoPago());
        verify(socketNotificador).notificarNuevoPedido(creado);
    }

    @Test
    void crearPedidoRechazaCuandoRecepcionEstaCerrada() {
        when(estadoOperacionService.isRecepcionAbierta()).thenReturn(false);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                pedidoService.crearPedido(1, new CrearPedidoRequest(
                        "matutino",
                        "mostrador",
                        List.of(new ItemPedidoRequest(10, 1)),
                        null
                ))
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void crearPedidoCompensaRetencionSiFallaGuardadoEnBd() {
        TransaccionPasarelaDto tx = new TransaccionPasarelaDto(
                "pi_test_huerfana",
                PasarelaPagoService.MODO_STRIPE_TEST,
                "requires_capture",
                ConstantesComedor.ESTADO_PAGO_RETENIDO,
                "4242",
                new BigDecimal("38.00"),
                "MXN",
                LocalDateTime.now()
        );

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(estudiante));
        when(pedidoRepository.findActivosPorUsuario(eq(1), anyList())).thenReturn(List.of());
        when(pedidoRepository.findReferenciasDelDiaDesc(any())).thenReturn(List.of());
        when(platilloRepository.findById(10)).thenReturn(Optional.of(platilloRegular));
        when(pasarelaPagoService.autorizarRetencion(eq("A001"), any(), any())).thenReturn(tx);
        when(pedidoRepository.save(any(Pedido.class))).thenThrow(new RuntimeException("Fallo simulado de BD"));

        assertThrows(RuntimeException.class, () ->
                pedidoService.crearPedido(1, new CrearPedidoRequest(
                        "matutino",
                        "app_tarjeta",
                        List.of(new ItemPedidoRequest(10, 1)),
                        "4242424242424242"
                ))
        );

        verify(pasarelaPagoService).liberarRetencionHuerfana(tx);
    }

    @Test
    void cancelarPedidoEnPreparacionDevuelveConflicto() {
        Pedido p = new Pedido();
        p.setId(50);
        p.setReferencia("A005");
        p.setUsuario(estudiante);
        p.setEstadoPedido(ConstantesComedor.ESTADO_EN_PREPARACION);
        when(pedidoRepository.findByIdConDetalle(50)).thenReturn(Optional.of(p));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                pedidoService.cancelarPedidoEstudiante(50, 1)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void procesarVencidosToleraFalloExternoDePasarelaSinAbortarCierre() {
        Pedido vencido = new Pedido();
        vencido.setId(60);
        vencido.setReferencia("A009");
        vencido.setUsuario(estudiante);
        vencido.setTurno(ConstantesComedor.TURNO_MATUTINO);
        vencido.setFechaOperativa(LocalDate.now());
        vencido.setTotal(new BigDecimal("35.00"));
        vencido.setModalidadPago(ConstantesComedor.PAGO_APP_TARJETA);
        vencido.setEstadoPago(ConstantesComedor.ESTADO_PAGO_RETENIDO);
        vencido.setEstadoPedido(ConstantesComedor.ESTADO_LISTO);
        vencido.setFechaCreacion(LocalDateTime.now().minusMinutes(25));
        vencido.setFechaLimiteRecoleccion(LocalDateTime.now().minusMinutes(2));

        when(pedidoRepository.findPedidosListosVencidos(eq(ConstantesComedor.ESTADO_LISTO), any()))
                .thenReturn(List.of(vencido));
        doThrow(new ReglaNegocioException("Error de red Stripe", HttpStatus.BAD_GATEWAY))
                .when(pasarelaPagoService).liberarRetencion(anyInt());
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        int cerrados = pedidoService.procesarPedidosVencidos();

        assertEquals(1, cerrados);
        assertEquals(ConstantesComedor.ESTADO_NO_RECLAMADO, vencido.getEstadoPedido());
        assertEquals(ConstantesComedor.ESTADO_PAGO_RETENIDO, vencido.getEstadoPago());
        assertEquals(1, estudiante.getFaltasAbandono());
        assertNotNull(vencido.getFechaCierre());
    }
}

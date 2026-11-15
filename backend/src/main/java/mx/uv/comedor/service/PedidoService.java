package mx.uv.comedor.service;

import mx.uv.comedor.config.ReglasComedorProperties;
import mx.uv.comedor.dto.CrearPedidoRequest;
import mx.uv.comedor.dto.HistoricoResponse;
import mx.uv.comedor.dto.ItemPedidoRequest;
import mx.uv.comedor.dto.PedidoDto;
import mx.uv.comedor.dto.TransaccionPasarelaDto;
import mx.uv.comedor.dto.UsuarioDto;
import mx.uv.comedor.exception.RecursoNoEncontradoException;
import mx.uv.comedor.exception.ReglaNegocioException;
import mx.uv.comedor.model.ConstantesComedor;
import mx.uv.comedor.model.DetallePedido;
import mx.uv.comedor.model.Pedido;
import mx.uv.comedor.model.Platillo;
import mx.uv.comedor.model.Usuario;
import mx.uv.comedor.repository.PedidoRepository;
import mx.uv.comedor.repository.PlatilloRepository;
import mx.uv.comedor.repository.UsuarioRepository;
import mx.uv.comedor.socket.SocketNotificador;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);
    private static final Pattern PATRON_REFERENCIA = Pattern.compile("^[A-Z][0-9]{3}$");

    private final PedidoRepository pedidoRepository;
    private final PlatilloRepository platilloRepository;
    private final UsuarioRepository usuarioRepository;
    private final RestriccionService restriccionService;
    private final EstadoOperacionService estadoOperacionService;
    private final PasarelaPagoService pasarelaPagoService;
    private final ReglasComedorProperties reglas;
    private final SocketNotificador socketNotificador;

    public PedidoService(
            PedidoRepository pedidoRepository,
            PlatilloRepository platilloRepository,
            UsuarioRepository usuarioRepository,
            RestriccionService restriccionService,
            EstadoOperacionService estadoOperacionService,
            PasarelaPagoService pasarelaPagoService,
            ReglasComedorProperties reglas,
            SocketNotificador socketNotificador
    ) {
        this.pedidoRepository = pedidoRepository;
        this.platilloRepository = platilloRepository;
        this.usuarioRepository = usuarioRepository;
        this.restriccionService = restriccionService;
        this.estadoOperacionService = estadoOperacionService;
        this.pasarelaPagoService = pasarelaPagoService;
        this.reglas = reglas;
        this.socketNotificador = socketNotificador;
    }

    @Transactional
    public int procesarPedidosVencidos() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Pedido> vencidos = pedidoRepository.findPedidosListosVencidos(ConstantesComedor.ESTADO_LISTO, ahora);

        if (vencidos.isEmpty()) {
            return 0;
        }

        for (Pedido pedido : vencidos) {
            cerrarPedidoPorVencimiento(pedido, ahora, true);
        }

        return vencidos.size();
    }

    @Transactional
    public PedidoDto forzarVencimientoPedido(Integer pedidoId) {
        Pedido pedido = pedidoRepository.findByIdConDetalle(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado"));

        if (!ConstantesComedor.ESTADO_LISTO.equals(pedido.getEstadoPedido())) {
            throw new ReglaNegocioException(
                    "Solo se puede marcar como no reclamado un pedido que está listo para recoger",
                    HttpStatus.CONFLICT
            );
        }

        return cerrarPedidoPorVencimiento(pedido, LocalDateTime.now(), false);
    }

    @Transactional
    public PedidoDto crearPedido(Integer usuarioId, CrearPedidoRequest request) {
        procesarPedidosVencidos();

        if (!estadoOperacionService.isRecepcionAbierta()) {
            throw new ReglaNegocioException(
                    "El comedor cerró temporalmente la recepción de pedidos en línea",
                    HttpStatus.CONFLICT
            );
        }

        String turno = request.turno().trim().toLowerCase();
        String modalidadPago = request.modalidadPago().trim().toLowerCase();

        if (!ConstantesComedor.TURNO_MATUTINO.equals(turno)
                && !ConstantesComedor.TURNO_VESPERTINO.equals(turno)) {
            throw new ReglaNegocioException("El turno del pedido debe ser matutino o vespertino");
        }
        if (!ConstantesComedor.PAGO_APP_TARJETA.equals(modalidadPago)
                && !ConstantesComedor.PAGO_MOSTRADOR.equals(modalidadPago)) {
            throw new ReglaNegocioException("Modalidad de pago no válida");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        LocalDateTime ahora = LocalDateTime.now();
        LocalDate hoy = ahora.toLocalDate();

        if (restriccionService.evaluarEstadoRestricciones(usuario, ahora)) {
            usuarioRepository.save(usuario);
        }
        restriccionService.validarPermisoDePedido(usuario, modalidadPago, ahora);

        List<Pedido> activos = pedidoRepository.findActivosPorUsuario(usuarioId, ConstantesComedor.ESTADOS_ACTIVOS);
        if (!activos.isEmpty()) {
            String refActiva = activos.get(0).getReferencia();
            throw new ReglaNegocioException(
                    "Ya tienes un pedido activo en curso (" + refActiva + "). Debes recogerlo antes de enviar otro.",
                    HttpStatus.CONFLICT
            );
        }

        Map<Integer, Integer> cantidadesPorPlatillo = new LinkedHashMap<>();
        for (ItemPedidoRequest item : request.items()) {
            cantidadesPorPlatillo.merge(item.platilloId(), item.cantidad(), Integer::sum);
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setTurno(turno);
        pedido.setFechaOperativa(hoy);
        pedido.setModalidadPago(modalidadPago);
        pedido.setEstadoPedido(ConstantesComedor.ESTADO_RECIBIDO);
        pedido.setEstadoPago(
                ConstantesComedor.PAGO_APP_TARJETA.equals(modalidadPago)
                        ? ConstantesComedor.ESTADO_PAGO_RETENIDO
                        : ConstantesComedor.ESTADO_PAGO_PENDIENTE_MOSTRADOR
        );
        pedido.setFechaCreacion(ahora);
        pedido.setReferencia(generarSiguienteReferencia(hoy));

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Integer, Integer> entrada : cantidadesPorPlatillo.entrySet()) {
            Platillo platillo = platilloRepository.findById(entrada.getKey())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Uno de los platillos seleccionados no existe"));

            if (!Boolean.TRUE.equals(platillo.getDisponible())) {
                throw new ReglaNegocioException(
                        "El platillo '" + platillo.getNombre() + "' acaba de agotarse",
                        HttpStatus.CONFLICT
                );
            }

            validarCoherenciaDeTurnoYFecha(platillo, turno, hoy);

            int cantidad = entrada.getValue();
            BigDecimal precioUnitario = platillo.getPrecio();
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));

            DetallePedido detalle = new DetallePedido();
            detalle.setPlatillo(platillo);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotal);

            pedido.agregarDetalle(detalle);
            total = total.add(subtotal);
        }

        pedido.setTotal(total);

        TransaccionPasarelaDto transaccion = null;
        if (ConstantesComedor.PAGO_APP_TARJETA.equals(modalidadPago)) {
            transaccion = pasarelaPagoService.autorizarRetencion(
                    pedido.getReferencia(),
                    total,
                    request.tarjetaPrueba()
            );
        }

        Pedido guardado;
        try {
            guardado = pedidoRepository.save(pedido);
            if (transaccion != null) {
                pasarelaPagoService.asociarPedido(guardado.getId(), transaccion);
            }
        } catch (RuntimeException ex) {
            if (transaccion != null) {
                pasarelaPagoService.liberarRetencionHuerfana(transaccion);
            }
            throw ex;
        }

        PedidoDto dto = PedidoDto.desdeEntidad(guardado);
        socketNotificador.notificarNuevoPedido(dto);
        return dto;
    }

    @Transactional
    public PedidoDto cancelarPedidoEstudiante(Integer pedidoId, Integer usuarioId) {
        procesarPedidosVencidos();

        Pedido pedido = pedidoRepository.findByIdConDetalle(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado"));

        if (!pedido.getUsuario().getId().equals(usuarioId)) {
            throw new ReglaNegocioException("No tienes permiso para cancelar este pedido", HttpStatus.FORBIDDEN);
        }

        if (!ConstantesComedor.ESTADO_RECIBIDO.equals(pedido.getEstadoPedido())) {
            throw new ReglaNegocioException(
                    "Tu pedido ya pasó a preparación o fue cerrado y ya no puede cancelarse",
                    HttpStatus.CONFLICT
            );
        }

        pedido.setEstadoPedido(ConstantesComedor.ESTADO_CANCELADO);
        pedido.setFechaCierre(LocalDateTime.now());
        if (ConstantesComedor.PAGO_APP_TARJETA.equals(pedido.getModalidadPago())) {
            pasarelaPagoService.liberarRetencion(pedido.getId());
            pedido.setEstadoPago(ConstantesComedor.ESTADO_PAGO_LIBERADO);
        } else {
            pedido.setEstadoPago(ConstantesComedor.ESTADO_PAGO_CANCELADO);
        }

        Pedido guardado = pedidoRepository.save(pedido);
        PedidoDto dto = PedidoDto.desdeEntidad(guardado);
        socketNotificador.notificarPedidoActualizado(dto);
        return dto;
    }

    @Transactional
    public List<PedidoDto> obtenerMisPedidos(Integer usuarioId) {
        procesarPedidosVencidos();
        return pedidoRepository.findHistorialPorUsuario(usuarioId)
                .stream()
                .map(PedidoDto::desdeEntidad)
                .toList();
    }

    @Transactional
    public List<PedidoDto> obtenerColaOperativa() {
        procesarPedidosVencidos();
        return pedidoRepository.findColaOperativa(ConstantesComedor.ESTADOS_ACTIVOS)
                .stream()
                .map(PedidoDto::desdeEntidad)
                .toList();
    }

    @Transactional
    public PedidoDto buscarPorReferenciaHoy(String referencia) {
        procesarPedidosVencidos();
        String normalizada = referencia == null ? "" : referencia.trim().toUpperCase();
        if (!PATRON_REFERENCIA.matcher(normalizada).matches()) {
            throw new ReglaNegocioException("La referencia debe tener formato de una letra y tres dígitos (ej. A001)");
        }
        Pedido pedido = pedidoRepository.findByFechaOperativaAndReferenciaConDetalle(LocalDate.now(), normalizada)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe ningún pedido hoy con la referencia " + normalizada
                ));
        return PedidoDto.desdeEntidad(pedido);
    }

    @Transactional
    public PedidoDto cambiarEstadoPedido(Integer pedidoId, String nuevoEstadoRaw) {
        procesarPedidosVencidos();

        String nuevoEstado = nuevoEstadoRaw.trim().toLowerCase();
        Pedido pedido = pedidoRepository.findByIdConDetalle(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado"));

        String actual = pedido.getEstadoPedido();
        LocalDateTime ahora = LocalDateTime.now();

        if (ConstantesComedor.ESTADO_NO_RECLAMADO.equals(actual)) {
            throw new ReglaNegocioException(
                    "El pedido " + pedido.getReferencia() + " ya venció y fue marcado como no reclamado",
                    HttpStatus.CONFLICT
            );
        }
        if (ConstantesComedor.ESTADO_CANCELADO.equals(actual)) {
            throw new ReglaNegocioException(
                    "El pedido " + pedido.getReferencia() + " fue cancelado por el estudiante",
                    HttpStatus.CONFLICT
            );
        }
        if (ConstantesComedor.ESTADO_ENTREGADO.equals(actual)) {
            throw new ReglaNegocioException(
                    "El pedido " + pedido.getReferencia() + " ya fue entregado",
                    HttpStatus.CONFLICT
            );
        }

        if (ConstantesComedor.ESTADO_RECIBIDO.equals(actual)
                && ConstantesComedor.ESTADO_EN_PREPARACION.equals(nuevoEstado)) {
            pedido.setEstadoPedido(ConstantesComedor.ESTADO_EN_PREPARACION);
        } else if (ConstantesComedor.ESTADO_EN_PREPARACION.equals(actual)
                && ConstantesComedor.ESTADO_LISTO.equals(nuevoEstado)) {
            pedido.setEstadoPedido(ConstantesComedor.ESTADO_LISTO);
            pedido.setFechaListo(ahora);
            pedido.setFechaLimiteRecoleccion(ahora.plusMinutes(reglas.getMinutosVigenciaRecoleccion()));
        } else if (ConstantesComedor.ESTADO_LISTO.equals(actual)
                && ConstantesComedor.ESTADO_ENTREGADO.equals(nuevoEstado)) {
            if (ConstantesComedor.PAGO_APP_TARJETA.equals(pedido.getModalidadPago())) {
                pasarelaPagoService.confirmarCobro(pedido.getId());
            }
            pedido.setEstadoPedido(ConstantesComedor.ESTADO_ENTREGADO);
            pedido.setFechaCierre(ahora);
            pedido.setEstadoPago(ConstantesComedor.ESTADO_PAGO_COBRADO);
        } else {
            throw new ReglaNegocioException(
                    "Transición no permitida de '" + actual + "' a '" + nuevoEstado + "'"
            );
        }

        Pedido actualizado = pedidoRepository.save(pedido);
        PedidoDto dto = PedidoDto.desdeEntidad(actualizado);
        socketNotificador.notificarPedidoActualizado(dto);
        return dto;
    }

    @Transactional
    public HistoricoResponse obtenerHistorico(LocalDate fecha, String turno, String modalidadPago, String estadoPedido) {
        procesarPedidosVencidos();

        String filtroTurno = normalizarFiltro(turno);
        String filtroModalidad = normalizarFiltro(modalidadPago);
        String filtroEstado = normalizarFiltro(estadoPedido);

        List<Pedido> pedidos = pedidoRepository.findHistorico(
                ConstantesComedor.ESTADOS_CERRADOS,
                fecha,
                filtroTurno,
                filtroModalidad,
                filtroEstado
        );

        long entregados = 0;
        long noReclamados = 0;
        BigDecimal cobradoApp = BigDecimal.ZERO;
        BigDecimal cobradoMostrador = BigDecimal.ZERO;

        for (Pedido p : pedidos) {
            if (ConstantesComedor.ESTADO_ENTREGADO.equals(p.getEstadoPedido())) {
                entregados++;
                if (ConstantesComedor.ESTADO_PAGO_COBRADO.equals(p.getEstadoPago())) {
                    if (ConstantesComedor.PAGO_APP_TARJETA.equals(p.getModalidadPago())) {
                        cobradoApp = cobradoApp.add(p.getTotal());
                    } else {
                        cobradoMostrador = cobradoMostrador.add(p.getTotal());
                    }
                }
            } else if (ConstantesComedor.ESTADO_NO_RECLAMADO.equals(p.getEstadoPedido())) {
                noReclamados++;
            }
        }

        List<PedidoDto> dtos = pedidos.stream().map(PedidoDto::desdeEntidad).toList();
        return new HistoricoResponse(
                dtos,
                entregados,
                noReclamados,
                cobradoApp,
                cobradoMostrador,
                cobradoApp.add(cobradoMostrador)
        );
    }

    private PedidoDto cerrarPedidoPorVencimiento(Pedido pedido, LocalDateTime ahora, boolean tolerarFalloPasarela) {
        pedido.setEstadoPedido(ConstantesComedor.ESTADO_NO_RECLAMADO);
        pedido.setFechaCierre(ahora);

        if (ConstantesComedor.PAGO_APP_TARJETA.equals(pedido.getModalidadPago())) {
            try {
                pasarelaPagoService.liberarRetencion(pedido.getId());
                pedido.setEstadoPago(ConstantesComedor.ESTADO_PAGO_LIBERADO);
            } catch (ReglaNegocioException ex) {
                if (!tolerarFalloPasarela) {
                    throw ex;
                }
                log.warn(
                        "No se pudo liberar en pasarela la retención del pedido {} al vencer automáticamente: {}",
                        pedido.getReferencia(),
                        ex.getMessage()
                );
            }
        } else {
            pedido.setEstadoPago(ConstantesComedor.ESTADO_PAGO_CANCELADO);
        }

        Pedido guardado = pedidoRepository.save(pedido);
        Usuario usuarioActualizado = restriccionService.registrarFaltaYActualizarRestriccion(
                guardado.getUsuario(), ahora
        );
        usuarioActualizado = usuarioRepository.save(usuarioActualizado);

        PedidoDto dto = PedidoDto.desdeEntidad(guardado);
        socketNotificador.notificarPedidoActualizado(dto);
        socketNotificador.notificarUsuarioActualizado(UsuarioDto.desdeEntidad(usuarioActualizado));
        return dto;
    }

    private void validarCoherenciaDeTurnoYFecha(Platillo platillo, String turnoPedido, LocalDate hoy) {
        if (ConstantesComedor.MENU_COMIDA_DEL_DIA.equals(platillo.getTipoMenu())) {
            if (!hoy.equals(platillo.getFechaOperativa())) {
                throw new ReglaNegocioException(
                        "La comida del día '" + platillo.getNombre() + "' no pertenece a la jornada actual"
                );
            }
            if (!turnoPedido.equals(platillo.getTurno())) {
                throw new ReglaNegocioException(
                        "La comida del día '" + platillo.getNombre() + "' es exclusiva del turno " + platillo.getTurno()
                );
            }
        } else if (ConstantesComedor.MENU_REGULAR.equals(platillo.getTipoMenu())) {
            if (!ConstantesComedor.TURNO_AMBOS.equals(platillo.getTurno())
                    && !turnoPedido.equals(platillo.getTurno())) {
                throw new ReglaNegocioException(
                        "El platillo '" + platillo.getNombre() + "' solo está disponible en turno " + platillo.getTurno()
                );
            }
        }
    }

    private String generarSiguienteReferencia(LocalDate fechaOperativa) {
        List<String> referencias = pedidoRepository.findReferenciasDelDiaDesc(fechaOperativa);
        if (referencias.isEmpty()) {
            return "A001";
        }
        String ultima = referencias.get(0);
        char letra = ultima.charAt(0);
        int numero = Integer.parseInt(ultima.substring(1));

        if (numero < 999) {
            numero++;
        } else {
            if (letra == 'Z') {
                throw new ReglaNegocioException("Se alcanzó el límite diario de referencias");
            }
            letra = (char) (letra + 1);
            numero = 1;
        }

        String siguiente = String.format("%c%03d", letra, numero);
        if (!PATRON_REFERENCIA.matcher(siguiente).matches()) {
            throw new IllegalStateException("Referencia generada con formato inválido: " + siguiente);
        }
        return siguiente;
    }

    private String normalizarFiltro(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim().toLowerCase();
        return limpio.isEmpty() || "todos".equals(limpio) ? null : limpio;
    }
}

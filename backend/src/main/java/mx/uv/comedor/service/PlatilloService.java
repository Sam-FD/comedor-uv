package mx.uv.comedor.service;

import mx.uv.comedor.config.ReglasComedorProperties;
import mx.uv.comedor.dto.CatalogoResponse;
import mx.uv.comedor.dto.ComidaDelDiaRequest;
import mx.uv.comedor.dto.PlatilloDto;
import mx.uv.comedor.dto.PlatilloRegularRequest;
import mx.uv.comedor.exception.RecursoNoEncontradoException;
import mx.uv.comedor.exception.ReglaNegocioException;
import mx.uv.comedor.model.ConstantesComedor;
import mx.uv.comedor.model.Platillo;
import mx.uv.comedor.repository.PlatilloRepository;
import mx.uv.comedor.socket.SocketNotificador;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class PlatilloService {

    private final PlatilloRepository platilloRepository;
    private final ReglasComedorProperties reglas;
    private final EstadoOperacionService estadoOperacionService;
    private final SocketNotificador socketNotificador;

    public PlatilloService(
            PlatilloRepository platilloRepository,
            ReglasComedorProperties reglas,
            EstadoOperacionService estadoOperacionService,
            SocketNotificador socketNotificador
    ) {
        this.platilloRepository = platilloRepository;
        this.reglas = reglas;
        this.estadoOperacionService = estadoOperacionService;
        this.socketNotificador = socketNotificador;
    }

    @Transactional(readOnly = true)
    public CatalogoResponse obtenerCatalogoVigente() {
        LocalDate hoy = LocalDate.now();
        String turnoSugerido = calcularTurnoSugerido(LocalTime.now());

        List<PlatilloDto> comidasDelDia = platilloRepository
                .findByTipoMenuAndFechaOperativaOrderByIdAsc(ConstantesComedor.MENU_COMIDA_DEL_DIA, hoy)
                .stream()
                .map(PlatilloDto::desdeEntidad)
                .toList();

        List<PlatilloDto> menuRegular = platilloRepository
                .findByTipoMenuOrderByIdAsc(ConstantesComedor.MENU_REGULAR)
                .stream()
                .map(PlatilloDto::desdeEntidad)
                .toList();

        return new CatalogoResponse(
                hoy,
                turnoSugerido,
                estadoOperacionService.isRecepcionAbierta(),
                comidasDelDia,
                menuRegular
        );
    }

    @Transactional
    public PlatilloDto publicarComidaDelDia(ComidaDelDiaRequest request) {
        String turno = request.turno().trim().toLowerCase();
        if (!ConstantesComedor.TURNO_MATUTINO.equals(turno)
                && !ConstantesComedor.TURNO_VESPERTINO.equals(turno)) {
            throw new ReglaNegocioException("La comida del día solo puede ser matutina o vespertina");
        }

        LocalDate hoy = LocalDate.now();

        List<Platillo> previasHoy = platilloRepository.findByTipoMenuAndFechaOperativaAndTurnoOrderByIdAsc(
                ConstantesComedor.MENU_COMIDA_DEL_DIA, hoy, turno
        );
        for (Platillo previa : previasHoy) {
            if (Boolean.TRUE.equals(previa.getDisponible())) {
                previa.setDisponible(false);
                platilloRepository.save(previa);
            }
        }

        Platillo nueva = new Platillo();
        nueva.setNombre(request.nombre().trim());
        nueva.setDescripcion(limpiarTexto(request.descripcion()));
        nueva.setPrecio(request.precio());
        nueva.setTipoMenu(ConstantesComedor.MENU_COMIDA_DEL_DIA);
        nueva.setTurno(turno);
        nueva.setFechaOperativa(hoy);
        nueva.setDisponible(true);

        Platillo guardada = platilloRepository.save(nueva);
        socketNotificador.notificarCatalogoActualizado();
        return PlatilloDto.desdeEntidad(guardada);
    }

    @Transactional
    public PlatilloDto crearPlatilloRegular(PlatilloRegularRequest request) {
        String turno = validarTurnoRegular(request.turno());

        Platillo nuevo = new Platillo();
        nuevo.setNombre(request.nombre().trim());
        nuevo.setDescripcion(limpiarTexto(request.descripcion()));
        nuevo.setPrecio(request.precio());
        nuevo.setTipoMenu(ConstantesComedor.MENU_REGULAR);
        nuevo.setTurno(turno);
        nuevo.setFechaOperativa(null);
        nuevo.setDisponible(true);

        Platillo guardado = platilloRepository.save(nuevo);
        socketNotificador.notificarCatalogoActualizado();
        return PlatilloDto.desdeEntidad(guardado);
    }

    @Transactional
    public PlatilloDto editarPlatilloRegular(Integer platilloId, PlatilloRegularRequest request) {
        Platillo platillo = platilloRepository.findById(platilloId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Platillo no encontrado"));

        if (!ConstantesComedor.MENU_REGULAR.equals(platillo.getTipoMenu())) {
            throw new ReglaNegocioException(
                    "La comida del día no se edita; se debe publicar una nueva para conservar el histórico"
            );
        }

        platillo.setNombre(request.nombre().trim());
        platillo.setDescripcion(limpiarTexto(request.descripcion()));
        platillo.setPrecio(request.precio());
        platillo.setTurno(validarTurnoRegular(request.turno()));

        Platillo actualizado = platilloRepository.save(platillo);
        socketNotificador.notificarCatalogoActualizado();
        return PlatilloDto.desdeEntidad(actualizado);
    }

    @Transactional
    public PlatilloDto cambiarDisponibilidad(Integer platilloId, boolean disponible) {
        Platillo platillo = platilloRepository.findById(platilloId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Platillo no encontrado"));

        platillo.setDisponible(disponible);
        Platillo actualizado = platilloRepository.save(platillo);
        socketNotificador.notificarCatalogoActualizado();
        return PlatilloDto.desdeEntidad(actualizado);
    }

    private String validarTurnoRegular(String turnoRaw) {
        String turno = turnoRaw.trim().toLowerCase();
        if (!ConstantesComedor.TURNO_MATUTINO.equals(turno)
                && !ConstantesComedor.TURNO_VESPERTINO.equals(turno)
                && !ConstantesComedor.TURNO_AMBOS.equals(turno)) {
            throw new ReglaNegocioException("El turno debe ser matutino, vespertino o ambos");
        }
        return turno;
    }

    private String calcularTurnoSugerido(LocalTime horaActual) {
        LocalTime horaCorte = LocalTime.parse(reglas.getHoraCorteMatutino());
        return horaActual.isBefore(horaCorte)
                ? ConstantesComedor.TURNO_MATUTINO
                : ConstantesComedor.TURNO_VESPERTINO;
    }

    private String limpiarTexto(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}

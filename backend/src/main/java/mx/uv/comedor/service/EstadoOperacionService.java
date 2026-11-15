package mx.uv.comedor.service;

import mx.uv.comedor.model.ConfiguracionOperacion;
import mx.uv.comedor.repository.ConfiguracionOperacionRepository;
import mx.uv.comedor.socket.SocketNotificador;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EstadoOperacionService {

    private static final String CLAVE_RECEPCION = "recepcion_abierta";

    private final ConfiguracionOperacionRepository configuracionRepository;
    private final SocketNotificador socketNotificador;

    public EstadoOperacionService(
            ConfiguracionOperacionRepository configuracionRepository,
            SocketNotificador socketNotificador
    ) {
        this.configuracionRepository = configuracionRepository;
        this.socketNotificador = socketNotificador;
    }

    @Transactional(readOnly = true)
    public boolean isRecepcionAbierta() {
        return configuracionRepository.findById(CLAVE_RECEPCION)
                .map(cfg -> Boolean.parseBoolean(cfg.getValor()))
                .orElse(true);
    }

    @Transactional
    public boolean cambiarRecepcion(boolean abierta) {
        ConfiguracionOperacion cfg = configuracionRepository.findById(CLAVE_RECEPCION)
                .orElseGet(() -> new ConfiguracionOperacion(CLAVE_RECEPCION, String.valueOf(abierta), LocalDateTime.now()));
        cfg.setValor(String.valueOf(abierta));
        cfg.setFechaActualizacion(LocalDateTime.now());
        configuracionRepository.save(cfg);

        socketNotificador.notificarCatalogoActualizado();
        return abierta;
    }
}

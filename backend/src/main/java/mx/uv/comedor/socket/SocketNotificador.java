package mx.uv.comedor.socket;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import mx.uv.comedor.config.JwtService;
import mx.uv.comedor.dto.PedidoDto;
import mx.uv.comedor.dto.UsuarioDto;
import mx.uv.comedor.model.ConstantesComedor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SocketNotificador implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SocketNotificador.class);
    private static final String SALA_CATALOGO = "sala:catalogo";
    private static final String SALA_PERSONAL = "sala:personal";

    @org.springframework.beans.factory.annotation.Value("${comedor.socket.habilitado:true}")
    private boolean habilitado;

    private final SocketIOServer server;
    private final JwtService jwtService;

    public SocketNotificador(SocketIOServer server, JwtService jwtService) {
        this.server = server;
        this.jwtService = jwtService;
        configurarEventos();
    }

    private void configurarEventos() {
        server.addConnectListener(client -> {
            client.joinRoom(SALA_CATALOGO);
            log.debug("Cliente conectado a Socket.IO: {}", client.getSessionId());
        });

        server.addDisconnectListener(client ->
                log.debug("Cliente desconectado de Socket.IO: {}", client.getSessionId())
        );

        server.addEventListener("unirse_sala", UnirseSalaPayload.class, (client, data, ack) -> {
            if (data == null || data.getToken() == null || !jwtService.esValido(data.getToken())) {
                return;
            }
            Integer userId = jwtService.obtenerUserId(data.getToken());
            String rol = jwtService.obtenerRol(data.getToken());

            dejarSalasUsuario(client);
            client.joinRoom(SALA_CATALOGO);
            client.joinRoom(salaUsuario(userId));

            if (ConstantesComedor.ROL_PERSONAL.equals(rol)) {
                client.joinRoom(SALA_PERSONAL);
            }
        });
    }

    private void dejarSalasUsuario(SocketIOClient client) {
        for (String sala : client.getAllRooms()) {
            if (sala.startsWith("sala:usuario:") || SALA_PERSONAL.equals(sala)) {
                client.leaveRoom(sala);
            }
        }
    }

    @Override
    public void run(String... args) {
        if (!habilitado) {
            log.info("Servidor Socket.IO deshabilitado (modo un solo puerto); se usara consulta periodica en el cliente.");
            return;
        }
        server.start();
        log.info("Servidor Socket.IO escuchando en el puerto {}", server.getConfiguration().getPort());
    }

    public void notificarCatalogoActualizado() {
        server.getRoomOperations(SALA_CATALOGO).sendEvent("catalogo_actualizado", Map.of("actualizado", true));
    }

    public void notificarNuevoPedido(PedidoDto pedido) {
        server.getRoomOperations(SALA_PERSONAL).sendEvent("nuevo_pedido", pedido);
    }

    public void notificarPedidoActualizado(PedidoDto pedido) {
        server.getRoomOperations(SALA_PERSONAL).sendEvent("pedido_actualizado", pedido);
        server.getRoomOperations(salaUsuario(pedido.usuarioId())).sendEvent("pedido_actualizado", pedido);
    }

    public void notificarUsuarioActualizado(UsuarioDto usuario) {
        server.getRoomOperations(salaUsuario(usuario.id())).sendEvent("usuario_actualizado", usuario);
    }

    private String salaUsuario(Integer usuarioId) {
        return "sala:usuario:" + usuarioId;
    }

    public static class UnirseSalaPayload {
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}

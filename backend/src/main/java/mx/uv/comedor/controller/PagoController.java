package mx.uv.comedor.controller;

import mx.uv.comedor.config.UsuarioAutenticado;
import mx.uv.comedor.dto.EstadoPasarelaResponse;
import mx.uv.comedor.dto.TransaccionPasarelaDto;
import mx.uv.comedor.exception.RecursoNoEncontradoException;
import mx.uv.comedor.exception.ReglaNegocioException;
import mx.uv.comedor.model.ConstantesComedor;
import mx.uv.comedor.model.Pedido;
import mx.uv.comedor.repository.PedidoRepository;
import mx.uv.comedor.service.PasarelaPagoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PasarelaPagoService pasarelaPagoService;
    private final PedidoRepository pedidoRepository;

    public PagoController(PasarelaPagoService pasarelaPagoService, PedidoRepository pedidoRepository) {
        this.pasarelaPagoService = pasarelaPagoService;
        this.pedidoRepository = pedidoRepository;
    }

    @GetMapping("/estado")
    public ResponseEntity<EstadoPasarelaResponse> estadoPasarela() {
        return ResponseEntity.ok(pasarelaPagoService.obtenerEstadoPasarela());
    }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<TransaccionPasarelaDto> transaccionPorPedido(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Integer pedidoId
    ) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado"));

        boolean esDueno = pedido.getUsuario().getId().equals(usuario.id());
        boolean esPersonal = ConstantesComedor.ROL_PERSONAL.equals(usuario.rol());
        if (!esDueno && !esPersonal) {
            throw new ReglaNegocioException("No tienes permiso para consultar este comprobante", HttpStatus.FORBIDDEN);
        }

        return pasarelaPagoService.obtenerPorPedido(pedidoId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}

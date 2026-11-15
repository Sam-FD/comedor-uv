package mx.uv.comedor.controller;

import jakarta.validation.Valid;
import mx.uv.comedor.config.UsuarioAutenticado;
import mx.uv.comedor.dto.CrearPedidoRequest;
import mx.uv.comedor.dto.PedidoDto;
import mx.uv.comedor.service.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final Object lockCreacion = new Object();

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<PedidoDto> crearPedido(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody CrearPedidoRequest request
    ) {
        PedidoDto creado;
        synchronized (lockCreacion) {
            creado = pedidoService.crearPedido(usuario.id(), request);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PedidoDto> cancelarPedido(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(pedidoService.cancelarPedidoEstudiante(id, usuario.id()));
    }

    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<PedidoDto>> misPedidos(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(pedidoService.obtenerMisPedidos(usuario.id()));
    }
}

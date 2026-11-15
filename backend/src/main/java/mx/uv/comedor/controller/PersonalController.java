package mx.uv.comedor.controller;

import jakarta.validation.Valid;
import mx.uv.comedor.dto.CambiarEstadoPedidoRequest;
import mx.uv.comedor.dto.ComidaDelDiaRequest;
import mx.uv.comedor.dto.DisponibilidadRequest;
import mx.uv.comedor.dto.HistoricoResponse;
import mx.uv.comedor.dto.PedidoDto;
import mx.uv.comedor.dto.PlatilloDto;
import mx.uv.comedor.dto.PlatilloRegularRequest;
import mx.uv.comedor.dto.RecepcionRequest;
import mx.uv.comedor.service.EstadoOperacionService;
import mx.uv.comedor.service.PedidoService;
import mx.uv.comedor.service.PlatilloService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/personal")
public class PersonalController {

    private final PedidoService pedidoService;
    private final PlatilloService platilloService;
    private final EstadoOperacionService estadoOperacionService;

    public PersonalController(
            PedidoService pedidoService,
            PlatilloService platilloService,
            EstadoOperacionService estadoOperacionService
    ) {
        this.pedidoService = pedidoService;
        this.platilloService = platilloService;
        this.estadoOperacionService = estadoOperacionService;
    }

    @GetMapping("/pedidos/cola")
    public ResponseEntity<List<PedidoDto>> obtenerCola() {
        return ResponseEntity.ok(pedidoService.obtenerColaOperativa());
    }

    @GetMapping("/pedidos/buscar")
    public ResponseEntity<PedidoDto> buscarPorReferencia(@RequestParam String referencia) {
        return ResponseEntity.ok(pedidoService.buscarPorReferenciaHoy(referencia));
    }

    @PatchMapping("/pedidos/{id}/estado")
    public ResponseEntity<PedidoDto> cambiarEstado(
            @PathVariable Integer id,
            @Valid @RequestBody CambiarEstadoPedidoRequest request
    ) {
        return ResponseEntity.ok(pedidoService.cambiarEstadoPedido(id, request.estadoPedido()));
    }

    @PostMapping("/pedidos/{id}/vencer")
    public ResponseEntity<PedidoDto> forzarVencimiento(@PathVariable Integer id) {
        return ResponseEntity.ok(pedidoService.forzarVencimientoPedido(id));
    }

    @GetMapping("/pedidos/historico")
    public ResponseEntity<HistoricoResponse> obtenerHistorico(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) String turno,
            @RequestParam(required = false) String modalidadPago,
            @RequestParam(required = false) String estado
    ) {
        return ResponseEntity.ok(pedidoService.obtenerHistorico(fecha, turno, modalidadPago, estado));
    }

    @PostMapping("/platillos/comida-del-dia")
    public ResponseEntity<PlatilloDto> publicarComidaDelDia(@Valid @RequestBody ComidaDelDiaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(platilloService.publicarComidaDelDia(request));
    }

    @PostMapping("/platillos/regular")
    public ResponseEntity<PlatilloDto> crearPlatilloRegular(@Valid @RequestBody PlatilloRegularRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(platilloService.crearPlatilloRegular(request));
    }

    @PutMapping("/platillos/regular/{id}")
    public ResponseEntity<PlatilloDto> editarPlatilloRegular(
            @PathVariable Integer id,
            @Valid @RequestBody PlatilloRegularRequest request
    ) {
        return ResponseEntity.ok(platilloService.editarPlatilloRegular(id, request));
    }

    @PatchMapping("/platillos/{id}/disponibilidad")
    public ResponseEntity<PlatilloDto> cambiarDisponibilidad(
            @PathVariable Integer id,
            @Valid @RequestBody DisponibilidadRequest request
    ) {
        return ResponseEntity.ok(platilloService.cambiarDisponibilidad(id, request.disponible()));
    }

    @PatchMapping("/operacion/recepcion")
    public ResponseEntity<Map<String, Boolean>> cambiarRecepcion(@Valid @RequestBody RecepcionRequest request) {
        boolean abierta = estadoOperacionService.cambiarRecepcion(request.abierta());
        return ResponseEntity.ok(Map.of("recepcionAbierta", abierta));
    }
}

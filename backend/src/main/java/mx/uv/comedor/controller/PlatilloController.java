package mx.uv.comedor.controller;

import mx.uv.comedor.dto.CatalogoResponse;
import mx.uv.comedor.service.PlatilloService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platillos")
public class PlatilloController {

    private final PlatilloService platilloService;

    public PlatilloController(PlatilloService platilloService) {
        this.platilloService = platilloService;
    }

    @GetMapping
    public ResponseEntity<CatalogoResponse> obtenerCatalogo() {
        return ResponseEntity.ok(platilloService.obtenerCatalogoVigente());
    }
}

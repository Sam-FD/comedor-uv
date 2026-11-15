package mx.uv.comedor.scheduler;

import mx.uv.comedor.service.PedidoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class VencimientoPedidosTask {

    private static final Logger log = LoggerFactory.getLogger(VencimientoPedidosTask.class);

    private final PedidoService pedidoService;

    public VencimientoPedidosTask(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @Scheduled(fixedDelay = 30000)
    public void revisarPedidosVencidos() {
        int cerrados = pedidoService.procesarPedidosVencidos();
        if (cerrados > 0) {
            log.info("Se marcaron {} pedidos listos como no reclamados por vencimiento", cerrados);
        }
    }
}

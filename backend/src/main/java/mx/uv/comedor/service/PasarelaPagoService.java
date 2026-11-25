package mx.uv.comedor.service;

import mx.uv.comedor.exception.ReglaNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PasarelaPagoService {

    private static final Logger log = LoggerFactory.getLogger(PasarelaPagoService.class);

    public void autorizarRetencion(String referenciaPedido, BigDecimal total, String numeroTarjeta) {
        String digitos = numeroTarjeta == null ? "" : numeroTarjeta.replaceAll("\\D", "");
        if (digitos.length() != 16) {
            throw new ReglaNegocioException("El número de tarjeta debe tener 16 dígitos");
        }
        if (digitos.endsWith("0002")) {
            throw new ReglaNegocioException(
                    "Pago rechazado: la tarjeta terminación 0002 fue declinada.",
                    HttpStatus.PAYMENT_REQUIRED
            );
        }
        log.info("Retención simulada autorizada para pedido {} por ${}", referenciaPedido, total);
    }

    public void confirmarCobro(String referenciaPedido, BigDecimal total) {
        log.info("Cobro simulado confirmado para pedido {} por ${}", referenciaPedido, total);
    }

    public void liberarRetencion(String referenciaPedido) {
        log.info("Retención simulada liberada para pedido {}", referenciaPedido);
    }
}

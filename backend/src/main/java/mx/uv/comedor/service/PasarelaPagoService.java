package mx.uv.comedor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mx.uv.comedor.config.PasarelaPagoProperties;
import mx.uv.comedor.dto.EstadoPasarelaResponse;
import mx.uv.comedor.dto.TransaccionPasarelaDto;
import mx.uv.comedor.exception.ReglaNegocioException;
import mx.uv.comedor.model.ConstantesComedor;
import mx.uv.comedor.model.TransaccionPasarela;
import mx.uv.comedor.repository.TransaccionPasarelaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasarelaPagoService {

    private static final Logger log = LoggerFactory.getLogger(PasarelaPagoService.class);
    private static final String STRIPE_API_BASE = "https://api.stripe.com/v1/payment_intents";

    public static final String MODO_STRIPE_TEST = "stripe_test";
    public static final String MODO_SANDBOX_LOCAL = "sandbox_local";

    private final PasarelaPagoProperties propiedades;
    private final ObjectMapper objectMapper;
    private final TransaccionPasarelaRepository transaccionRepository;
    private final HttpClient httpClient;

    public PasarelaPagoService(
            PasarelaPagoProperties propiedades,
            ObjectMapper objectMapper,
            TransaccionPasarelaRepository transaccionRepository
    ) {
        this.propiedades = propiedades;
        this.objectMapper = objectMapper;
        this.transaccionRepository = transaccionRepository;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public boolean usaStripeReal() {
        String key = propiedades.getStripeSecretKey();
        return key != null && key.trim().startsWith("sk_test_");
    }

    public EstadoPasarelaResponse obtenerEstadoPasarela() {
        String modo = usaStripeReal() ? MODO_STRIPE_TEST : MODO_SANDBOX_LOCAL;
        String nombre = usaStripeReal()
                ? "Stripe Sandbox (modo prueba)"
                : "Pasarela Sandbox local (emulacion Stripe)";

        List<EstadoPasarelaResponse.TarjetaPruebaInfo> tarjetas = List.of(
                new EstadoPasarelaResponse.TarjetaPruebaInfo(
                        "Visa prueba aprobada",
                        "4242 4242 4242 4242",
                        "Autoriza retencion de fondos"
                ),
                new EstadoPasarelaResponse.TarjetaPruebaInfo(
                        "Visa prueba declinada",
                        "4000 0000 0000 0002",
                        "Simula rechazo por tarjeta declinada"
                )
        );

        return new EstadoPasarelaResponse(
                modo,
                nombre,
                propiedades.getMoneda().toUpperCase(),
                tarjetas
        );
    }

    @Transactional(readOnly = true)
    public Optional<TransaccionPasarelaDto> obtenerPorPedido(Integer pedidoId) {
        return transaccionRepository.findByPedidoId(pedidoId)
                .map(TransaccionPasarela::aDto);
    }

    public TransaccionPasarelaDto autorizarRetencion(String referenciaPedido, BigDecimal total, String tarjetaEntrada) {
        String digitos = limpiarDigitos(tarjetaEntrada);
        boolean esTarjetaDeclinada = "4000000000000002".equals(digitos)
                || "0002".equals(digitos)
                || "pm_card_chargeDeclined".equalsIgnoreCase(tarjetaEntrada == null ? "" : tarjetaEntrada.trim());

        String ultimosCuatro = digitos.length() >= 4
                ? digitos.substring(digitos.length() - 4)
                : "4242";

        if (!usaStripeReal()) {
            if (esTarjetaDeclinada) {
                throw new ReglaNegocioException(
                        "Pago rechazado en pasarela de prueba: la tarjeta terminacion 0002 fue declinada.",
                        HttpStatus.PAYMENT_REQUIRED
                );
            }
            String fakeId = "pi_sandbox_" + referenciaPedido + "_" + UUID.randomUUID().toString().substring(0, 8);
            return new TransaccionPasarelaDto(
                    fakeId,
                    MODO_SANDBOX_LOCAL,
                    "requires_capture",
                    ConstantesComedor.ESTADO_PAGO_RETENIDO,
                    ultimosCuatro,
                    total,
                    propiedades.getMoneda().toUpperCase(),
                    LocalDateTime.now()
            );
        }

        String paymentMethod = resolverPaymentMethodStripe(tarjetaEntrada, esTarjetaDeclinada);
        long centavos = total.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("amount", String.valueOf(centavos));
        params.put("currency", propiedades.getMoneda().toLowerCase());
        params.put("capture_method", "manual");
        params.put("confirm", "true");
        params.put("payment_method", paymentMethod);
        params.put("automatic_payment_methods[enabled]", "true");
        params.put("automatic_payment_methods[allow_redirects]", "never");
        params.put("description", "Comedor Universitario UV - Pedido " + referenciaPedido);

        JsonNode json = enviarPostStripe(STRIPE_API_BASE, params);
        String paymentIntentId = json.path("id").asText();
        String estadoStripe = json.path("status").asText();

        if (!"requires_capture".equals(estadoStripe)) {
            throw new ReglaNegocioException(
                    "Stripe no dejo el pago listo para retencion (estado recibido: " + estadoStripe + ")",
                    HttpStatus.PAYMENT_REQUIRED
            );
        }

        return new TransaccionPasarelaDto(
                paymentIntentId,
                MODO_STRIPE_TEST,
                estadoStripe,
                ConstantesComedor.ESTADO_PAGO_RETENIDO,
                ultimosCuatro,
                total,
                propiedades.getMoneda().toUpperCase(),
                LocalDateTime.now()
        );
    }

    @Transactional
    public void asociarPedido(Integer pedidoId, TransaccionPasarelaDto dto) {
        if (pedidoId == null || dto == null) {
            return;
        }
        TransaccionPasarela entidad = transaccionRepository.findByPedidoId(pedidoId)
                .orElseGet(TransaccionPasarela::new);
        entidad.setPedidoId(pedidoId);
        entidad.setTransaccionId(dto.transaccionId());
        entidad.setModo(dto.modo());
        entidad.setEstadoPasarela(dto.estadoPasarela());
        entidad.setEstadoComedor(dto.estadoComedor());
        entidad.setUltimosDigitos(dto.ultimosDigitos());
        entidad.setMonto(dto.monto());
        entidad.setMoneda(dto.moneda());
        entidad.setFechaActualizacion(dto.fechaActualizacion() != null ? dto.fechaActualizacion() : LocalDateTime.now());
        transaccionRepository.save(entidad);
    }

    @Transactional
    public void confirmarCobro(Integer pedidoId) {
        TransaccionPasarela actual = transaccionRepository.findByPedidoId(pedidoId).orElse(null);
        if (actual == null) {
            return;
        }

        String estadoStripe = "succeeded";
        if (MODO_STRIPE_TEST.equals(actual.getModo()) && usaStripeReal()) {
            JsonNode json = enviarPostStripe(STRIPE_API_BASE + "/" + actual.getTransaccionId() + "/capture", Map.of());
            estadoStripe = json.path("status").asText("succeeded");
        }

        actual.setEstadoPasarela(estadoStripe);
        actual.setEstadoComedor(ConstantesComedor.ESTADO_PAGO_COBRADO);
        actual.setFechaActualizacion(LocalDateTime.now());
        transaccionRepository.save(actual);
    }

    @Transactional
    public void liberarRetencion(Integer pedidoId) {
        TransaccionPasarela actual = transaccionRepository.findByPedidoId(pedidoId).orElse(null);
        if (actual == null) {
            return;
        }

        String estadoStripe = "canceled";
        if (MODO_STRIPE_TEST.equals(actual.getModo()) && usaStripeReal()) {
            JsonNode json = enviarPostStripe(STRIPE_API_BASE + "/" + actual.getTransaccionId() + "/cancel", Map.of());
            estadoStripe = json.path("status").asText("canceled");
        }

        actual.setEstadoPasarela(estadoStripe);
        actual.setEstadoComedor(ConstantesComedor.ESTADO_PAGO_LIBERADO);
        actual.setFechaActualizacion(LocalDateTime.now());
        transaccionRepository.save(actual);
    }

    public void liberarRetencionHuerfana(TransaccionPasarelaDto transaccion) {
        if (transaccion == null || !MODO_STRIPE_TEST.equals(transaccion.modo()) || !usaStripeReal()) {
            return;
        }
        try {
            enviarPostStripe(STRIPE_API_BASE + "/" + transaccion.transaccionId() + "/cancel", Map.of());
        } catch (Exception ex) {
            log.warn("No se pudo liberar la retencion huerfana {}: {}", transaccion.transaccionId(), ex.getMessage());
        }
    }

    private String resolverPaymentMethodStripe(String entrada, boolean esDeclinada) {
        if (entrada != null && entrada.trim().startsWith("pm_")) {
            return entrada.trim();
        }
        return esDeclinada ? "pm_card_chargeDeclined" : "pm_card_visa";
    }

    private String limpiarDigitos(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replaceAll("\\D", "");
    }

    private JsonNode enviarPostStripe(String url, Map<String, String> params) {
        String body = codificarFormulario(params);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + propiedades.getStripeSecretKey().trim())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());

            if (response.statusCode() >= 400) {
                String mensajeStripe = root.path("error").path("message").asText("La tarjeta fue rechazada por Stripe");
                HttpStatus status = response.statusCode() == 402 ? HttpStatus.PAYMENT_REQUIRED : HttpStatus.BAD_GATEWAY;
                throw new ReglaNegocioException("Pasarela Stripe: " + mensajeStripe, status);
            }

            return root;
        } catch (ReglaNegocioException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ReglaNegocioException("Se interrumpio la comunicacion con Stripe Sandbox", HttpStatus.BAD_GATEWAY);
        } catch (IOException ex) {
            log.warn("Error de red comunicando con Stripe Sandbox: {}", ex.getMessage());
            throw new ReglaNegocioException(
                    "No se pudo conectar con Stripe Sandbox. Verifica tu conexion a internet o la clave sk_test.",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    private String codificarFormulario(Map<String, String> params) {
        if (params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entrada : params.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(URLEncoder.encode(entrada.getKey(), StandardCharsets.UTF_8));
            sb.append('=');
            sb.append(URLEncoder.encode(entrada.getValue(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}

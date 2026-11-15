package mx.uv.comedor.model;

import java.util.List;

public final class ConstantesComedor {

    private ConstantesComedor() {
    }

    // Roles
    public static final String ROL_ESTUDIANTE = "estudiante";
    public static final String ROL_PERSONAL = "personal";

    // Niveles de restriccion
    public static final String RESTRICCION_NINGUNA = "ninguna";
    public static final String RESTRICCION_SIN_MOSTRADOR = "sin_mostrador";
    public static final String RESTRICCION_SIN_PEDIDOS = "sin_pedidos_en_linea";

    // Tipos de menu
    public static final String MENU_COMIDA_DEL_DIA = "comida_del_dia";
    public static final String MENU_REGULAR = "regular";

    // Turnos
    public static final String TURNO_MATUTINO = "matutino";
    public static final String TURNO_VESPERTINO = "vespertino";
    public static final String TURNO_AMBOS = "ambos";

    // Modalidades de pago
    public static final String PAGO_APP_TARJETA = "app_tarjeta";
    public static final String PAGO_MOSTRADOR = "mostrador";

    // Estados de pago
    public static final String ESTADO_PAGO_PENDIENTE_MOSTRADOR = "pendiente_mostrador";
    public static final String ESTADO_PAGO_RETENIDO = "retenido";
    public static final String ESTADO_PAGO_COBRADO = "cobrado";
    public static final String ESTADO_PAGO_LIBERADO = "liberado";
    public static final String ESTADO_PAGO_CANCELADO = "cancelado";

    // Estados de pedido
    public static final String ESTADO_RECIBIDO = "recibido";
    public static final String ESTADO_EN_PREPARACION = "en_preparacion";
    public static final String ESTADO_LISTO = "listo_para_recoger";
    public static final String ESTADO_ENTREGADO = "entregado";
    public static final String ESTADO_NO_RECLAMADO = "no_reclamado";
    public static final String ESTADO_CANCELADO = "cancelado";

    public static final List<String> ESTADOS_ACTIVOS = List.of(
            ESTADO_RECIBIDO,
            ESTADO_EN_PREPARACION,
            ESTADO_LISTO
    );

    public static final List<String> ESTADOS_CERRADOS = List.of(
            ESTADO_ENTREGADO,
            ESTADO_NO_RECLAMADO
    );
}

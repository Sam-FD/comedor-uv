package mx.uv.comedor.config;

import mx.uv.comedor.model.ConstantesComedor;
import mx.uv.comedor.model.DetallePedido;
import mx.uv.comedor.model.Pedido;
import mx.uv.comedor.model.Platillo;
import mx.uv.comedor.model.Usuario;
import mx.uv.comedor.repository.PedidoRepository;
import mx.uv.comedor.repository.PlatilloRepository;
import mx.uv.comedor.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PlatilloRepository platilloRepository;
    private final PedidoRepository pedidoRepository;
    private final PasswordEncoder passwordEncoder;

    public DatosIniciales(
            UsuarioRepository usuarioRepository,
            PlatilloRepository platilloRepository,
            PedidoRepository pedidoRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.platilloRepository = platilloRepository;
        this.pedidoRepository = pedidoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        LocalDate hoy = LocalDate.now();
        LocalDateTime ahora = LocalDateTime.now();

        if (usuarioRepository.count() == 0) {
            String passwordDemo = passwordEncoder.encode("uv2026");

            Usuario laura = crearUsuario(
                    "S22014510", "Laura Mendoza Jimenez", "l.mendoza@estudiantes.uv.mx",
                    passwordDemo, ConstantesComedor.ROL_ESTUDIANTE, 0, null,
                    ConstantesComedor.RESTRICCION_NINGUNA, null
            );
            Usuario carlos = crearUsuario(
                    "S22018824", "Carlos Hernandez Lopez", "c.hernandez@estudiantes.uv.mx",
                    passwordDemo, ConstantesComedor.ROL_ESTUDIANTE, 2, ahora.minusDays(8),
                    ConstantesComedor.RESTRICCION_NINGUNA, null
            );
            Usuario ana = crearUsuario(
                    "S21009315", "Ana Sofia Ruiz Torres", "a.ruiz@estudiantes.uv.mx",
                    passwordDemo, ConstantesComedor.ROL_ESTUDIANTE, 3, ahora.minusDays(5),
                    ConstantesComedor.RESTRICCION_SIN_MOSTRADOR, ahora.plusDays(25)
            );
            Usuario diego = crearUsuario(
                    "S23004109", "Diego Morales Castro", "d.morales@estudiantes.uv.mx",
                    passwordDemo, ConstantesComedor.ROL_ESTUDIANTE, 4, ahora.minusDays(2),
                    ConstantesComedor.RESTRICCION_SIN_PEDIDOS, ahora.plusDays(32)
            );
            Usuario tomas = crearUsuario(
                    "S22017733", "Tomas Vazquez Perez", "t.vazquez@estudiantes.uv.mx",
                    passwordDemo, ConstantesComedor.ROL_ESTUDIANTE, 0, null,
                    ConstantesComedor.RESTRICCION_NINGUNA, null
            );
            Usuario rosa = crearUsuario(
                    "P10001", "Rosa Ramirez Ortega", "r.ramirez@uv.mx",
                    passwordDemo, ConstantesComedor.ROL_PERSONAL, 0, null,
                    ConstantesComedor.RESTRICCION_NINGUNA, null
            );

            usuarioRepository.saveAll(List.of(laura, carlos, ana, diego, tomas, rosa));

            List<Platillo> regulares = platilloRepository.saveAll(List.of(
                    crearPlatillo("Chilaquiles verdes con pollo", "Totopos banados en salsa verde, pollo deshebrado, crema y queso fresco", new BigDecimal("48.00"), ConstantesComedor.MENU_REGULAR, ConstantesComedor.TURNO_MATUTINO, null, true),
                    crearPlatillo("Molletes gratinados (3 pzas)", "Bolillo con frijoles refritos, queso manchego gratinado y pico de gallo", new BigDecimal("38.00"), ConstantesComedor.MENU_REGULAR, ConstantesComedor.TURNO_AMBOS, null, true),
                    crearPlatillo("Torta de milanesa de res", "Telera con milanesa, frijoles, aguacate, jitomate y chile chipotle", new BigDecimal("52.00"), ConstantesComedor.MENU_REGULAR, ConstantesComedor.TURNO_AMBOS, null, true),
                    crearPlatillo("Tacos dorados de pollo (4 pzas)", "Acompanados de lechuga, crema, queso y salsa verde", new BigDecimal("45.00"), ConstantesComedor.MENU_REGULAR, ConstantesComedor.TURNO_VESPERTINO, null, true),
                    crearPlatillo("Ensalada con pechuga asada", "Mezcla de lechugas, pepino, jitomate, croutones y aderezo ligero", new BigDecimal("55.00"), ConstantesComedor.MENU_REGULAR, ConstantesComedor.TURNO_AMBOS, null, true),
                    crearPlatillo("Agua fresca del dia (500 ml)", "Vaso de 500 ml de jamaica, horchata o limon con chia", new BigDecimal("15.00"), ConstantesComedor.MENU_REGULAR, ConstantesComedor.TURNO_AMBOS, null, true),
                    crearPlatillo("Cafe americano de olla", "Taza de cafe recien colado con un toque de canela", new BigDecimal("18.00"), ConstantesComedor.MENU_REGULAR, ConstantesComedor.TURNO_AMBOS, null, false)
            ));

            List<Platillo> comidasHoy = asegurarComidasDelDia(hoy);

            // Pedidos de ejemplo: uno activo en cola y dos cerrados de ayer para el historico
            LocalDate ayer = hoy.minusDays(1);

            Pedido pedidoActivo = new Pedido();
            pedidoActivo.setReferencia("A001");
            pedidoActivo.setUsuario(tomas);
            pedidoActivo.setTurno(ConstantesComedor.TURNO_MATUTINO);
            pedidoActivo.setFechaOperativa(hoy);
            pedidoActivo.setModalidadPago(ConstantesComedor.PAGO_MOSTRADOR);
            pedidoActivo.setEstadoPago(ConstantesComedor.ESTADO_PAGO_PENDIENTE_MOSTRADOR);
            pedidoActivo.setEstadoPedido(ConstantesComedor.ESTADO_RECIBIDO);
            pedidoActivo.setFechaCreacion(ahora.minusMinutes(6));
            Platillo comidaMatutina = comidasHoy.get(0);
            agregarItem(pedidoActivo, comidaMatutina, 1);
            pedidoActivo.setTotal(comidaMatutina.getPrecio());
            pedidoRepository.save(pedidoActivo);

            Pedido hist1 = new Pedido();
            hist1.setReferencia("A001");
            hist1.setUsuario(laura);
            hist1.setTurno(ConstantesComedor.TURNO_MATUTINO);
            hist1.setFechaOperativa(ayer);
            hist1.setModalidadPago(ConstantesComedor.PAGO_APP_TARJETA);
            hist1.setEstadoPago(ConstantesComedor.ESTADO_PAGO_COBRADO);
            hist1.setEstadoPedido(ConstantesComedor.ESTADO_ENTREGADO);
            hist1.setFechaCreacion(ahora.minusDays(1).minusHours(3));
            hist1.setFechaListo(ahora.minusDays(1).minusHours(2).minusMinutes(48));
            hist1.setFechaLimiteRecoleccion(ahora.minusDays(1).minusHours(2).minusMinutes(33));
            hist1.setFechaCierre(ahora.minusDays(1).minusHours(2).minusMinutes(42));
            agregarItem(hist1, regulares.get(1), 1);
            agregarItem(hist1, regulares.get(5), 1);
            hist1.setTotal(regulares.get(1).getPrecio().add(regulares.get(5).getPrecio()));
            pedidoRepository.save(hist1);

            Pedido hist2 = new Pedido();
            hist2.setReferencia("A002");
            hist2.setUsuario(carlos);
            hist2.setTurno(ConstantesComedor.TURNO_VESPERTINO);
            hist2.setFechaOperativa(ayer);
            hist2.setModalidadPago(ConstantesComedor.PAGO_MOSTRADOR);
            hist2.setEstadoPago(ConstantesComedor.ESTADO_PAGO_CANCELADO);
            hist2.setEstadoPedido(ConstantesComedor.ESTADO_NO_RECLAMADO);
            hist2.setFechaCreacion(ahora.minusDays(1).minusHours(1));
            hist2.setFechaListo(ahora.minusDays(1).minusMinutes(45));
            hist2.setFechaLimiteRecoleccion(ahora.minusDays(1).minusMinutes(30));
            hist2.setFechaCierre(ahora.minusDays(1).minusMinutes(29));
            agregarItem(hist2, regulares.get(2), 1);
            hist2.setTotal(regulares.get(2).getPrecio());
            pedidoRepository.save(hist2);
        } else {
            asegurarComidasDelDia(hoy);
        }
    }

    private List<Platillo> asegurarComidasDelDia(LocalDate hoy) {
        List<Platillo> existentesHoy = platilloRepository.findByTipoMenuAndFechaOperativaOrderByIdAsc(
                ConstantesComedor.MENU_COMIDA_DEL_DIA, hoy
        );
        if (!existentesHoy.isEmpty()) {
            return existentesHoy;
        }

        Platillo matutino = crearPlatillo(
                "Pechuga a la plancha con arroz y frijoles",
                "Incluye plato principal, arroz rojo, frijoles de la olla, tortillas y agua de jamaica",
                new BigDecimal("35.00"),
                ConstantesComedor.MENU_COMIDA_DEL_DIA,
                ConstantesComedor.TURNO_MATUTINO,
                hoy,
                true
        );
        Platillo vespertino = crearPlatillo(
                "Enchiladas veracruzanas de pollo",
                "Incluye 4 enchiladas en salsa de jitomate, frijoles refritos, queso fresco y agua de horchata",
                new BigDecimal("35.00"),
                ConstantesComedor.MENU_COMIDA_DEL_DIA,
                ConstantesComedor.TURNO_VESPERTINO,
                hoy,
                true
        );
        return platilloRepository.saveAll(List.of(matutino, vespertino));
    }

    private Usuario crearUsuario(
            String credencial,
            String nombre,
            String correo,
            String passwordHash,
            String rol,
            int faltas,
            LocalDateTime fechaUltimaFalta,
            String nivelRestriccion,
            LocalDateTime restriccionHasta
    ) {
        Usuario u = new Usuario();
        u.setCredencialInstitucional(credencial);
        u.setNombre(nombre);
        u.setCorreo(correo);
        u.setPasswordHash(passwordHash);
        u.setRol(rol);
        u.setFaltasAbandono(faltas);
        u.setFechaUltimaFalta(fechaUltimaFalta);
        u.setNivelRestriccion(nivelRestriccion);
        u.setRestriccionHasta(restriccionHasta);
        return u;
    }

    private Platillo crearPlatillo(
            String nombre,
            String descripcion,
            BigDecimal precio,
            String tipoMenu,
            String turno,
            LocalDate fechaOperativa,
            boolean disponible
    ) {
        Platillo p = new Platillo();
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecio(precio);
        p.setTipoMenu(tipoMenu);
        p.setTurno(turno);
        p.setFechaOperativa(fechaOperativa);
        p.setDisponible(disponible);
        return p;
    }

    private void agregarItem(Pedido pedido, Platillo platillo, int cantidad) {
        DetallePedido detalle = new DetallePedido();
        detalle.setPlatillo(platillo);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(platillo.getPrecio());
        detalle.setSubtotal(platillo.getPrecio().multiply(BigDecimal.valueOf(cantidad)));
        pedido.agregarDetalle(detalle);
    }
}

package mx.uv.comedor.config;

import mx.uv.comedor.model.ConstantesComedor;
import mx.uv.comedor.model.Platillo;
import mx.uv.comedor.model.Usuario;
import mx.uv.comedor.repository.PlatilloRepository;
import mx.uv.comedor.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PlatilloRepository platilloRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${comedor.admin.credencial:P10001}")
    private String adminCredencial;

    @Value("${comedor.admin.nombre:Rosa Ramirez Ortega}")
    private String adminNombre;

    @Value("${comedor.admin.correo:r.ramirez@uv.mx}")
    private String adminCorreo;

    @Value("${comedor.admin.password:uv2026}")
    private String adminPassword;

    public DatosIniciales(
            UsuarioRepository usuarioRepository,
            PlatilloRepository platilloRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.platilloRepository = platilloRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        asegurarCuentaPersonal();
        if (platilloRepository.count() == 0) {
            platilloRepository.saveAll(crearMenuRegularPorDefecto());
        }
    }

    private void asegurarCuentaPersonal() {
        String credencial = adminCredencial.trim().toUpperCase();
        if (!usuarioRepository.existsByCredencialInstitucionalIgnoreCase(credencial)) {
            Usuario personal = new Usuario();
            personal.setCredencialInstitucional(credencial);
            personal.setNombre(adminNombre.trim());
            personal.setCorreo(adminCorreo.trim().toLowerCase());
            personal.setPasswordHash(passwordEncoder.encode(adminPassword));
            personal.setRol(ConstantesComedor.ROL_PERSONAL);
            personal.setFaltasAbandono(0);
            personal.setNivelRestriccion(ConstantesComedor.RESTRICCION_NINGUNA);
            usuarioRepository.save(personal);
        }
    }

    private List<Platillo> crearMenuRegularPorDefecto() {
        return List.of(
                crearPlatillo("Chilaquiles verdes con pollo", "Totopos banados en salsa verde, pollo deshebrado, crema y queso fresco", new BigDecimal("48.00"), ConstantesComedor.TURNO_MATUTINO, true),
                crearPlatillo("Molletes gratinados (3 pzas)", "Bolillo con frijoles refritos, queso manchego gratinado y pico de gallo", new BigDecimal("38.00"), ConstantesComedor.TURNO_AMBOS, true),
                crearPlatillo("Torta de milanesa de res", "Telera con milanesa, frijoles, aguacate, jitomate y chile chipotle", new BigDecimal("52.00"), ConstantesComedor.TURNO_AMBOS, true),
                crearPlatillo("Tacos dorados de pollo (4 pzas)", "Acompanados de lechuga, crema, queso y salsa verde", new BigDecimal("45.00"), ConstantesComedor.TURNO_VESPERTINO, true),
                crearPlatillo("Ensalada con pechuga asada", "Mezcla de lechugas, pepino, jitomate, croutones y aderezo ligero", new BigDecimal("55.00"), ConstantesComedor.TURNO_AMBOS, true),
                crearPlatillo("Agua fresca del dia (500 ml)", "Vaso de 500 ml de jamaica, horchata o limon con chia", new BigDecimal("15.00"), ConstantesComedor.TURNO_AMBOS, true),
                crearPlatillo("Cafe americano de olla", "Taza de cafe recien colado con un toque de canela", new BigDecimal("18.00"), ConstantesComedor.TURNO_AMBOS, false)
        );
    }

    private Platillo crearPlatillo(String nombre, String descripcion, BigDecimal precio, String turno, boolean disponible) {
        Platillo p = new Platillo();
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecio(precio);
        p.setTipoMenu(ConstantesComedor.MENU_REGULAR);
        p.setTurno(turno);
        p.setFechaOperativa(null);
        p.setDisponible(disponible);
        return p;
    }
}

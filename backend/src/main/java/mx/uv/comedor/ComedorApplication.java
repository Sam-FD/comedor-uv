package mx.uv.comedor;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ComedorApplication {

    public record DatosConexionJdbc(String url, String usuario, String password) {
    }

    @PostConstruct
    public void configurarZonaHoraria() {
        // Garantiza horarios de servicio consistentes aunque el servidor cloud use UTC
        TimeZone.setDefault(TimeZone.getTimeZone("America/Mexico_City"));
    }

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Mexico_City"));
        configurarConexionBaseDatos();
        SpringApplication.run(ComedorApplication.class, args);
    }

    public static DatosConexionJdbc normalizarUrlBaseDatos(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return new DatosConexionJdbc(rawUrl, null, null);
        }
        String valor = rawUrl.trim();
        boolean teniaPrefijoJdbc = valor.startsWith("jdbc:");
        String sinJdbc = teniaPrefijoJdbc ? valor.substring(5) : valor;

        if (!sinJdbc.startsWith("postgres://") && !sinJdbc.startsWith("postgresql://")) {
            return new DatosConexionJdbc(valor, null, null);
        }

        String uriNormalizado = sinJdbc.startsWith("postgres://")
                ? "postgresql://" + sinJdbc.substring("postgres://".length())
                : sinJdbc;

        URI uri = URI.create(uriNormalizado);
        String rawUserInfo = uri.getRawUserInfo();
        if (teniaPrefijoJdbc && (rawUserInfo == null || rawUserInfo.isBlank()) && !sinJdbc.startsWith("postgres://")) {
            return new DatosConexionJdbc(valor, null, null);
        }

        String usuario = null;
        String password = null;
        if (rawUserInfo != null && !rawUserInfo.isBlank()) {
            int separador = rawUserInfo.indexOf(':');
            if (separador >= 0) {
                usuario = URLDecoder.decode(rawUserInfo.substring(0, separador), StandardCharsets.UTF_8);
                password = URLDecoder.decode(rawUserInfo.substring(separador + 1), StandardCharsets.UTF_8);
            } else {
                usuario = URLDecoder.decode(rawUserInfo, StandardCharsets.UTF_8);
            }
        }

        int puerto = uri.getPort() > 0 ? uri.getPort() : 5432;
        String path = uri.getRawPath() != null ? uri.getRawPath() : "";
        String query = (uri.getRawQuery() != null && !uri.getRawQuery().isBlank())
                ? "?" + uri.getRawQuery()
                : "";
        String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + puerto + path + query;
        return new DatosConexionJdbc(jdbcUrl, usuario, password);
    }

    private static void configurarConexionBaseDatos() {
        String rawUrl = obtenerPropiedadOEnv("DB_URL");
        if (rawUrl == null || rawUrl.isBlank()) {
            rawUrl = obtenerPropiedadOEnv("DATABASE_URL");
        }
        if (rawUrl == null || rawUrl.isBlank()) {
            return;
        }
        DatosConexionJdbc datos = normalizarUrlBaseDatos(rawUrl);
        if (datos.url() != null && !datos.url().isBlank()) {
            System.setProperty("spring.datasource.url", datos.url());
        }
        String dbUser = obtenerPropiedadOEnv("DB_USER");
        if ((dbUser == null || dbUser.isBlank()) && datos.usuario() != null && !datos.usuario().isBlank()) {
            System.setProperty("spring.datasource.username", datos.usuario());
        }
        String dbPass = obtenerPropiedadOEnv("DB_PASS");
        if ((dbPass == null || dbPass.isBlank()) && datos.password() != null) {
            System.setProperty("spring.datasource.password", datos.password());
        }
    }

    private static String obtenerPropiedadOEnv(String nombre) {
        String prop = System.getProperty(nombre);
        if (prop != null && !prop.isBlank()) {
            return prop;
        }
        return System.getenv(nombre);
    }
}

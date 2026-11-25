package mx.uv.comedor;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ComedorApplication {

    @PostConstruct
    public void configurarZonaHoraria() {
        // Garantiza horarios de servicio consistentes aunque el servidor cloud use UTC
        TimeZone.setDefault(TimeZone.getTimeZone("America/Mexico_City"));
    }

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Mexico_City"));
        SpringApplication.run(ComedorApplication.class, args);
    }
}

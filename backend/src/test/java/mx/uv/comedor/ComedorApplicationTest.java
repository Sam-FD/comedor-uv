package mx.uv.comedor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ComedorApplicationTest {

    @Test
    void normalizaUrlEstiloNeonPostgresql() {
        ComedorApplication.DatosConexionJdbc datos = ComedorApplication.normalizarUrlBaseDatos(
                "postgresql://neondb_owner:npg_123@ep-test.us-east-2.aws.neon.tech/neondb?sslmode=require"
        );

        assertEquals(
                "jdbc:postgresql://ep-test.us-east-2.aws.neon.tech:5432/neondb?sslmode=require",
                datos.url()
        );
        assertEquals("neondb_owner", datos.usuario());
        assertEquals("npg_123", datos.password());
    }

    @Test
    void conservaUrlJdbcLocalSinModificar() {
        String rawUrl = "jdbc:postgresql://localhost:5432/comedor_uv";
        ComedorApplication.DatosConexionJdbc datos = ComedorApplication.normalizarUrlBaseDatos(rawUrl);

        assertEquals(rawUrl, datos.url());
        assertNull(datos.usuario());
        assertNull(datos.password());
    }
}

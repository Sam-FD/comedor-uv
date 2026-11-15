package mx.uv.comedor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "comedor.reglas")
public class ReglasComedorProperties {

    private int umbralNivel1 = 3;
    private int duracionNivel1Meses = 1;
    private int umbralNivel2 = 4;
    private int extensionNivel2Dias = 15;
    private int diasCorteLimpio = 60;
    private int minutosVigenciaRecoleccion = 15;
    private String horaCorteMatutino = "14:30";

    public int getUmbralNivel1() {
        return umbralNivel1;
    }

    public void setUmbralNivel1(int umbralNivel1) {
        this.umbralNivel1 = umbralNivel1;
    }

    public int getDuracionNivel1Meses() {
        return duracionNivel1Meses;
    }

    public void setDuracionNivel1Meses(int duracionNivel1Meses) {
        this.duracionNivel1Meses = duracionNivel1Meses;
    }

    public int getUmbralNivel2() {
        return umbralNivel2;
    }

    public void setUmbralNivel2(int umbralNivel2) {
        this.umbralNivel2 = umbralNivel2;
    }

    public int getExtensionNivel2Dias() {
        return extensionNivel2Dias;
    }

    public void setExtensionNivel2Dias(int extensionNivel2Dias) {
        this.extensionNivel2Dias = extensionNivel2Dias;
    }

    public int getDiasCorteLimpio() {
        return diasCorteLimpio;
    }

    public void setDiasCorteLimpio(int diasCorteLimpio) {
        this.diasCorteLimpio = diasCorteLimpio;
    }

    public int getMinutosVigenciaRecoleccion() {
        return minutosVigenciaRecoleccion;
    }

    public void setMinutosVigenciaRecoleccion(int minutosVigenciaRecoleccion) {
        this.minutosVigenciaRecoleccion = minutosVigenciaRecoleccion;
    }

    public String getHoraCorteMatutino() {
        return horaCorteMatutino;
    }

    public void setHoraCorteMatutino(String horaCorteMatutino) {
        this.horaCorteMatutino = horaCorteMatutino;
    }
}

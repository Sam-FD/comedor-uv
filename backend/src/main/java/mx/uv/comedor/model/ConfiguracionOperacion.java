package mx.uv.comedor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_operacion")
public class ConfiguracionOperacion {

    @Id
    @Column(length = 50)
    private String clave;

    @Column(length = 100, nullable = false)
    private String valor;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    public ConfiguracionOperacion() {
    }

    public ConfiguracionOperacion(String clave, String valor, LocalDateTime fechaActualizacion) {
        this.clave = clave;
        this.valor = valor;
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}

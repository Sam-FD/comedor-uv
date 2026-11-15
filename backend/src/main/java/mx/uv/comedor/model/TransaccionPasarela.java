package mx.uv.comedor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import mx.uv.comedor.dto.TransaccionPasarelaDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacciones_pasarela")
public class TransaccionPasarela {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "pedido_id", nullable = false, unique = true)
    private Integer pedidoId;

    @Column(name = "transaccion_id", length = 120, nullable = false)
    private String transaccionId;

    @Column(length = 30, nullable = false)
    private String modo;

    @Column(name = "estado_pasarela", length = 40, nullable = false)
    private String estadoPasarela;

    @Column(name = "estado_comedor", length = 30, nullable = false)
    private String estadoComedor;

    @Column(name = "ultimos_digitos", length = 4, nullable = false)
    private String ultimosDigitos;

    @Column(precision = 8, scale = 2, nullable = false)
    private BigDecimal monto;

    @Column(length = 10, nullable = false)
    private String moneda;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    public TransaccionPasarelaDto aDto() {
        return new TransaccionPasarelaDto(
                transaccionId,
                modo,
                estadoPasarela,
                estadoComedor,
                ultimosDigitos,
                monto,
                moneda,
                fechaActualizacion
        );
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Integer pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getTransaccionId() {
        return transaccionId;
    }

    public void setTransaccionId(String transaccionId) {
        this.transaccionId = transaccionId;
    }

    public String getModo() {
        return modo;
    }

    public void setModo(String modo) {
        this.modo = modo;
    }

    public String getEstadoPasarela() {
        return estadoPasarela;
    }

    public void setEstadoPasarela(String estadoPasarela) {
        this.estadoPasarela = estadoPasarela;
    }

    public String getEstadoComedor() {
        return estadoComedor;
    }

    public void setEstadoComedor(String estadoComedor) {
        this.estadoComedor = estadoComedor;
    }

    public String getUltimosDigitos() {
        return ultimosDigitos;
    }

    public void setUltimosDigitos(String ultimosDigitos) {
        this.ultimosDigitos = ultimosDigitos;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}

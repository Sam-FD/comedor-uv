package mx.uv.comedor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "credencial_institucional", length = 30, unique = true, nullable = false)
    private String credencialInstitucional;

    @Column(length = 100, nullable = false)
    private String nombre;

    @Column(length = 100, unique = true, nullable = false)
    private String correo;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(length = 20, nullable = false)
    private String rol;

    @Column(name = "faltas_abandono", nullable = false)
    private Integer faltasAbandono = 0;

    @Column(name = "fecha_ultima_falta")
    private LocalDateTime fechaUltimaFalta;

    @Column(name = "nivel_restriccion", length = 30, nullable = false)
    private String nivelRestriccion = ConstantesComedor.RESTRICCION_NINGUNA;

    @Column(name = "restriccion_hasta")
    private LocalDateTime restriccionHasta;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCredencialInstitucional() {
        return credencialInstitucional;
    }

    public void setCredencialInstitucional(String credencialInstitucional) {
        this.credencialInstitucional = credencialInstitucional;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Integer getFaltasAbandono() {
        return faltasAbandono;
    }

    public void setFaltasAbandono(Integer faltasAbandono) {
        this.faltasAbandono = faltasAbandono;
    }

    public LocalDateTime getFechaUltimaFalta() {
        return fechaUltimaFalta;
    }

    public void setFechaUltimaFalta(LocalDateTime fechaUltimaFalta) {
        this.fechaUltimaFalta = fechaUltimaFalta;
    }

    public String getNivelRestriccion() {
        return nivelRestriccion;
    }

    public void setNivelRestriccion(String nivelRestriccion) {
        this.nivelRestriccion = nivelRestriccion;
    }

    public LocalDateTime getRestriccionHasta() {
        return restriccionHasta;
    }

    public void setRestriccionHasta(LocalDateTime restriccionHasta) {
        this.restriccionHasta = restriccionHasta;
    }
}

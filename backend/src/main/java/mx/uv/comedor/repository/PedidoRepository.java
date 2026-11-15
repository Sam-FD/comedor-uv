package mx.uv.comedor.repository;

import mx.uv.comedor.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    @Query("""
        SELECT DISTINCT p FROM Pedido p
        JOIN FETCH p.usuario
        LEFT JOIN FETCH p.detalles d
        LEFT JOIN FETCH d.platillo
        WHERE p.id = :id
    """)
    Optional<Pedido> findByIdConDetalle(@Param("id") Integer id);

    @Query("""
        SELECT DISTINCT p FROM Pedido p
        JOIN FETCH p.usuario
        LEFT JOIN FETCH p.detalles d
        LEFT JOIN FETCH d.platillo
        WHERE p.fechaOperativa = :fecha AND UPPER(p.referencia) = UPPER(:referencia)
    """)
    Optional<Pedido> findByFechaOperativaAndReferenciaConDetalle(
            @Param("fecha") LocalDate fecha,
            @Param("referencia") String referencia
    );

    @Query("""
        SELECT DISTINCT p FROM Pedido p
        JOIN FETCH p.usuario
        LEFT JOIN FETCH p.detalles d
        LEFT JOIN FETCH d.platillo
        WHERE p.usuario.id = :usuarioId AND p.estadoPedido IN :estados
        ORDER BY p.fechaCreacion DESC
    """)
    List<Pedido> findActivosPorUsuario(
            @Param("usuarioId") Integer usuarioId,
            @Param("estados") List<String> estados
    );

    @Query("""
        SELECT DISTINCT p FROM Pedido p
        JOIN FETCH p.usuario
        LEFT JOIN FETCH p.detalles d
        LEFT JOIN FETCH d.platillo
        WHERE p.usuario.id = :usuarioId
        ORDER BY p.fechaCreacion DESC
    """)
    List<Pedido> findHistorialPorUsuario(@Param("usuarioId") Integer usuarioId);

    @Query("""
        SELECT DISTINCT p FROM Pedido p
        JOIN FETCH p.usuario
        LEFT JOIN FETCH p.detalles d
        LEFT JOIN FETCH d.platillo
        WHERE p.estadoPedido IN :estados
        ORDER BY p.fechaCreacion ASC
    """)
    List<Pedido> findColaOperativa(@Param("estados") List<String> estados);

    @Query("""
        SELECT DISTINCT p FROM Pedido p
        JOIN FETCH p.usuario
        LEFT JOIN FETCH p.detalles d
        LEFT JOIN FETCH d.platillo
        WHERE p.estadoPedido = :estado AND p.fechaLimiteRecoleccion <= :ahora
    """)
    List<Pedido> findPedidosListosVencidos(
            @Param("estado") String estado,
            @Param("ahora") LocalDateTime ahora
    );

    @Query("""
        SELECT p.referencia FROM Pedido p
        WHERE p.fechaOperativa = :fecha
        ORDER BY p.referencia DESC
    """)
    List<String> findReferenciasDelDiaDesc(@Param("fecha") LocalDate fecha);

    @Query("""
        SELECT DISTINCT p FROM Pedido p
        JOIN FETCH p.usuario
        LEFT JOIN FETCH p.detalles d
        LEFT JOIN FETCH d.platillo
        WHERE p.estadoPedido IN :estadosCerrados
          AND (:fecha IS NULL OR p.fechaOperativa = :fecha)
          AND (:turno IS NULL OR p.turno = :turno)
          AND (:modalidadPago IS NULL OR p.modalidadPago = :modalidadPago)
          AND (:estadoPedido IS NULL OR p.estadoPedido = :estadoPedido)
        ORDER BY p.fechaCierre DESC, p.id DESC
    """)
    List<Pedido> findHistorico(
            @Param("estadosCerrados") List<String> estadosCerrados,
            @Param("fecha") LocalDate fecha,
            @Param("turno") String turno,
            @Param("modalidadPago") String modalidadPago,
            @Param("estadoPedido") String estadoPedido
    );
}

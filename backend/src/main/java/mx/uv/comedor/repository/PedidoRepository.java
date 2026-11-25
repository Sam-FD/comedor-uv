package mx.uv.comedor.repository;

import mx.uv.comedor.model.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.platillo"})
    Optional<Pedido> findConDetalleById(Integer id);

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.platillo"})
    Optional<Pedido> findByFechaOperativaAndReferenciaIgnoreCase(LocalDate fechaOperativa, String referencia);

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.platillo"})
    List<Pedido> findByUsuarioIdAndEstadoPedidoInOrderByFechaCreacionDesc(Integer usuarioId, List<String> estados);

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.platillo"})
    List<Pedido> findByUsuarioIdOrderByFechaCreacionDesc(Integer usuarioId);

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.platillo"})
    List<Pedido> findByEstadoPedidoInOrderByFechaCreacionAsc(List<String> estados);

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.platillo"})
    List<Pedido> findByEstadoPedidoAndFechaLimiteRecoleccionLessThanEqual(String estadoPedido, LocalDateTime ahora);

    @Query("""
        SELECT p.referencia FROM Pedido p
        WHERE p.fechaOperativa = :fecha
        ORDER BY p.referencia DESC
    """)
    List<String> findReferenciasDelDiaDesc(@Param("fecha") LocalDate fecha);

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.platillo"})
    @Query("""
        SELECT p FROM Pedido p
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

package mx.uv.comedor.repository;

import mx.uv.comedor.model.TransaccionPasarela;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransaccionPasarelaRepository extends JpaRepository<TransaccionPasarela, Integer> {

    Optional<TransaccionPasarela> findByPedidoId(Integer pedidoId);
}

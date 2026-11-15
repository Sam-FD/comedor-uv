package mx.uv.comedor.repository;

import mx.uv.comedor.model.Platillo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PlatilloRepository extends JpaRepository<Platillo, Integer> {

    List<Platillo> findByTipoMenuAndFechaOperativaOrderByIdAsc(String tipoMenu, LocalDate fechaOperativa);

    List<Platillo> findByTipoMenuAndFechaOperativaAndTurnoOrderByIdAsc(String tipoMenu, LocalDate fechaOperativa, String turno);

    List<Platillo> findByTipoMenuOrderByIdAsc(String tipoMenu);
}

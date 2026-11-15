package mx.uv.comedor.repository;

import mx.uv.comedor.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCredencialInstitucionalIgnoreCase(String credencialInstitucional);

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCredencialInstitucionalIgnoreCase(String credencialInstitucional);

    boolean existsByCorreoIgnoreCase(String correo);
}

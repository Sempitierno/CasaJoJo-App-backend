package cl.lacasajojo.usuarios.repository;

import cl.lacasajojo.common.modelo.Rol;
import cl.lacasajojo.usuarios.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
    boolean existsByCorreoAndIdNot(String correo, Long id);
    List<Usuario> findAllByOrderByIdDesc();
    List<Usuario> findByRolOrderByIdDesc(Rol rol);
    long countByRol(Rol rol);
}

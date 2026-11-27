package cl.lacasajojo.usuarios.service;

import cl.lacasajojo.common.error.ApiException;
import cl.lacasajojo.common.modelo.Rol;
import cl.lacasajojo.common.seguridad.JwtService;
import cl.lacasajojo.common.seguridad.UsuarioAutenticado;
import cl.lacasajojo.usuarios.dto.*;
import cl.lacasajojo.usuarios.entity.Usuario;
import cl.lacasajojo.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.List;
import java.util.Locale;

/** Reglas de negocio de usuarios. Los controladores solo validan permisos y delegan aquí. */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    public static final String MENSAJE_CORREO_DUPLICADO = "Ya existe una cuenta registrada con este correo";
    public static final String MENSAJE_CREDENCIALES = "Correo o contraseña incorrectos";
    static final long VIGENCIA_CODIGO_MS = 15 * 60 * 1000L;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PedidosClient pedidosClient;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.recuperacion.mostrar-codigo:true}")
    private boolean mostrarCodigo;

    /** "ANA@Correo.cl" y "ana@correo.cl" son el mismo correo (igual que en la app). */
    public static String normalizarCorreo(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public UsuarioDto crear(String nombreCompleto, String correo, String password, Rol rol) {
        String correoNormalizado = normalizarCorreo(correo);
        if (usuarioRepository.existsByCorreo(correoNormalizado)) throw correoDuplicado();
        Usuario u = new Usuario();
        u.setNombreCompleto(nombreCompleto.trim());
        u.setCorreo(correoNormalizado);
        u.setRol(rol);
        u.setClaveHash(passwordEncoder.encode(password));
        u.setCreadoEn(clock.millis());
        try {
            return UsuarioDto.de(usuarioRepository.saveAndFlush(u));
        } catch (DataIntegrityViolationException e) {
            throw correoDuplicado();
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        Usuario u = usuarioRepository.findByCorreo(normalizarCorreo(req.correo()))
                .orElseThrow(() -> ApiException.noAutenticado(MENSAJE_CREDENCIALES));
        if (!passwordEncoder.matches(req.password(), u.getClaveHash())) {
            throw ApiException.noAutenticado(MENSAJE_CREDENCIALES);
        }
        String token = jwtService.generar(new UsuarioAutenticado(u.getId(), u.getCorreo(), u.getRol()));
        return new LoginResponse(token, clock.millis() + jwtService.getDuracionMs(), UsuarioDto.de(u));
    }

    /** Genera un código de 6 dígitos válido por 15 minutos. Solo se guarda su hash. */
    @Transactional
    public RecuperarResponse solicitarRecuperacion(RecuperarRequest req) {
        Usuario u = usuarioRepository.findByCorreo(normalizarCorreo(req.correo()))
                .orElseThrow(() -> ApiException.noEncontrado("No existe una cuenta con ese correo"));
        String codigo = String.format("%06d", random.nextInt(1_000_000));
        long expira = clock.millis() + VIGENCIA_CODIGO_MS;
        u.setCodigoRecuperacionHash(passwordEncoder.encode(codigo));
        u.setCodigoRecuperacionExpiraEn(expira);
        return new RecuperarResponse("Código de recuperación generado. Vence en 15 minutos.",
                mostrarCodigo ? codigo : null, expira);
    }

    @Transactional
    public void restablecerPassword(RestablecerRequest req) {
        Usuario u = usuarioRepository.findByCorreo(normalizarCorreo(req.correo()))
                .orElseThrow(() -> ApiException.noEncontrado("No existe una cuenta con ese correo"));
        boolean vigente = u.getCodigoRecuperacionHash() != null
                && u.getCodigoRecuperacionExpiraEn() != null
                && u.getCodigoRecuperacionExpiraEn() >= clock.millis();
        if (!vigente || !passwordEncoder.matches(req.codigo(), u.getCodigoRecuperacionHash())) {
            throw ApiException.campo("codigo", "El código es incorrecto o ya venció");
        }
        u.setClaveHash(passwordEncoder.encode(req.nuevaPassword()));
        u.setCodigoRecuperacionHash(null);
        u.setCodigoRecuperacionExpiraEn(null);
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto> listar(Rol rol) {
        List<Usuario> usuarios = rol == null ? usuarioRepository.findAllByOrderByIdDesc()
                : usuarioRepository.findByRolOrderByIdDesc(rol);
        return usuarios.stream().map(UsuarioDto::de).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioDto obtener(Long id) {
        return UsuarioDto.de(buscar(id));
    }

    @Transactional
    public UsuarioDto actualizarPerfil(Long id, ActualizarPerfilRequest req) {
        Usuario u = buscar(id);
        String correo = normalizarCorreo(req.correo());
        if (usuarioRepository.existsByCorreoAndIdNot(correo, id)) throw correoDuplicado();
        u.setNombreCompleto(req.nombreCompleto().trim());
        u.setCorreo(correo);
        return UsuarioDto.de(u);
    }

    @Transactional
    public void cambiarPassword(Long id, CambiarPasswordRequest req) {
        Usuario u = buscar(id);
        if (!passwordEncoder.matches(req.passwordActual(), u.getClaveHash())) {
            throw ApiException.campo("passwordActual", "La contraseña actual no es correcta");
        }
        u.setClaveHash(passwordEncoder.encode(req.nuevaPassword()));
    }

    @Transactional
    public UsuarioDto cambiarFoto(Long id, String foto) {
        Usuario u = buscar(id);
        u.setFotoPerfil(foto);
        return UsuarioDto.de(u);
    }

    @Transactional
    public UsuarioDto cambiarRol(Long id, Rol rol) {
        Usuario u = buscar(id);
        if (u.getRol() == Rol.ADMINISTRADOR && rol != Rol.ADMINISTRADOR) protegerUltimoAdmin();
        u.setRol(rol);
        return UsuarioDto.de(u);
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario u = buscar(id);
        if (u.getRol() == Rol.ADMINISTRADOR) protegerUltimoAdmin();
        if (pedidosClient.clienteTienePedidos(id)) {
            throw ApiException.conflicto("No se puede eliminar un usuario que tiene pedidos registrados");
        }
        usuarioRepository.delete(u);
    }

    private void protegerUltimoAdmin() {
        if (usuarioRepository.countByRol(Rol.ADMINISTRADOR) <= 1) {
            throw ApiException.conflicto("Debe quedar al menos un administrador");
        }
    }

    private static ApiException correoDuplicado() {
        return ApiException.campo(HttpStatus.CONFLICT, "correo", MENSAJE_CORREO_DUPLICADO);
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> ApiException.noEncontrado("Usuario no encontrado"));
    }
}

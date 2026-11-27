package cl.lacasajojo.common.modelo;

/**
 * Los 4 roles del sistema y sus privilegios:
 * <ul>
 *   <li>ADMINISTRADOR: todo (menú, usuarios, roles, pedidos).</li>
 *   <li>COCINERO: ve todas las comandas, las avanza RECIBIDO → EN_PREPARACION → LISTO_RETIRO,
 *       les saca foto y marca productos agotados.</li>
 *   <li>CAJERO: ve todas las comandas, entrega los pedidos listos (LISTO_RETIRO → ENTREGADO)
 *       y marca productos agotados.</li>
 *   <li>CLIENTE: ve el menú publicado, crea pedidos y ve solo los suyos.</li>
 * </ul>
 */
public enum Rol {
    ADMINISTRADOR, COCINERO, CAJERO, CLIENTE;

    public boolean esPersonal() {
        return this != CLIENTE;
    }
}

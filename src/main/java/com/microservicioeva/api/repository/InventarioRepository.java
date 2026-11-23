package com.microservicioeva.api.repository;

import com.microservicioeva.api.model.Inventario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JDBC para operaciones sobre la tabla inventario en MySQL.
 */
@Repository
public class InventarioRepository {

    private static final Logger logger = LoggerFactory.getLogger(InventarioRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public InventarioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper reutilizable para mapear ResultSet -> Inventario
    private final RowMapper<Inventario> inventarioRowMapper = new RowMapper<Inventario>() {
        @Override
        public Inventario mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Inventario(
                    rs.getInt("stock_minimo"),
                    rs.getInt("stock_maximo"),
                    rs.getDouble("cantidad"),
                    rs.getInt("id_producto"),
                    rs.getInt("id_inventario")
            );
        }
    };

    /**
     * Consulta paginada de todos los registros de inventario.
     *
     * @param page   número de página (0-based)
     * @param size   tamaño de página
     * @return lista paginada de registros de inventario
     */
    public List<Inventario> findAll(int page, int size) {
        int offset = page * size;
        String sql = "SELECT id_inventario, id_producto, cantidad, stock_minimo, stock_maximo " +
                     "FROM inventario LIMIT ? OFFSET ?";
        logger.debug("Ejecutando consulta paginada: page={}, size={}, offset={}", page, size, offset);
        try {
            return jdbcTemplate.query(sql, inventarioRowMapper, size, offset);
        } catch (Exception e) {
            logger.error("Error al consultar inventario paginado (page={}, size={}): {}", page, size, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Cuenta el total de registros en inventario para calcular paginación.
     *
     * @return total de registros
     */
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM inventario";
        logger.debug("Ejecutando conteo total de inventario");
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            logger.error("Error al contar registros de inventario: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Busca un registro de inventario por su ID.
     *
     * @param idInventario identificador del inventario
     * @return Optional con el inventario si existe
     */
    public Optional<Inventario> findById(int idInventario) {
        String sql = "SELECT id_inventario, id_producto, cantidad, stock_minimo, stock_maximo " +
                     "FROM inventario WHERE id_inventario = ?";
        logger.debug("Buscando inventario con id_inventario={}", idInventario);
        try {
            List<Inventario> results = jdbcTemplate.query(sql, inventarioRowMapper, idInventario);
            if (results.isEmpty()) {
                logger.info("No se encontró inventario con id_inventario={}", idInventario);
                return Optional.empty();
            }
            return Optional.of(results.get(0));
        } catch (Exception e) {
            logger.error("Error al buscar inventario por id={}: {}", idInventario, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Elimina un registro de inventario por su ID.
     *
     * @param idInventario identificador del inventario a eliminar
     * @return número de filas afectadas
     */
    public int deleteById(int idInventario) {
        String sql = "DELETE FROM inventario WHERE id_inventario = ?";
        logger.debug("Eliminando inventario con id_inventario={}", idInventario);
        try {
            int rowsAffected = jdbcTemplate.update(sql, idInventario);
            logger.info("Inventario con id_inventario={} eliminado. Filas afectadas: {}", idInventario, rowsAffected);
            return rowsAffected;
        } catch (Exception e) {
            logger.error("Error al eliminar inventario con id={}: {}", idInventario, e.getMessage(), e);
            throw e;
        }
    }
}

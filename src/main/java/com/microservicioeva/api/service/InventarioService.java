package com.microservicioeva.api.service;

import com.microservicioeva.api.dto.PageResponse;
import com.microservicioeva.api.exception.ApiException;
import com.microservicioeva.api.model.Inventario;
import com.microservicioeva.api.repository.InventarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de lógica de negocio para la gestión del inventario.
 * Maneja validaciones, registros de logs y delega al repositorio JDBC.
 */
@Service
public class InventarioService {

    private static final Logger logger = LoggerFactory.getLogger(InventarioService.class);

    private final InventarioRepository inventarioRepository;

    public InventarioService(InventarioRepository inventarioRepository) {
        this.inventarioRepository = inventarioRepository;
    }

    /**
     * Consulta todos los productos del inventario con paginación.
     *
     * @param page número de página (0-based, default 0)
     * @param size tamaño de página (default 10, max 100)
     * @return PageResponse con la lista paginada de inventario
     */
    public PageResponse<Inventario> consultarInventario(int page, int size) {
        logger.info("Iniciando consulta paginada de inventario: page={}, size={}", page, size);

        try {
            // Validaciones de parámetros
            if (page < 0) {
                logger.warn("Parámetro page inválido: {}", page);
                throw new ApiException("El parámetro 'page' no puede ser negativo.", 400);
            }
            if (size <= 0 || size > 100) {
                logger.warn("Parámetro size inválido: {}", size);
                throw new ApiException("El parámetro 'size' debe estar entre 1 y 100.", 400);
            }

            int total = inventarioRepository.countAll();
            List<Inventario> items = inventarioRepository.findAll(page, size);

            logger.info("Consulta paginada exitosa: {} registros encontrados de {} totales", items.size(), total);
            return new PageResponse<>(items, page, size, total);

        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Error inesperado al consultar inventario paginado: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error interno al consultar el inventario.", ex);
        }
    }

    /**
     * Consulta un producto del inventario por su ID.
     *
     * @param idInventario ID del inventario a buscar
     * @return objeto Inventario encontrado
     */
    public Inventario consultarPorId(int idInventario) {
        logger.info("Consultando inventario con id_inventario={}", idInventario);

        try {
            if (idInventario <= 0) {
                logger.warn("ID de inventario inválido: {}", idInventario);
                throw new ApiException("El ID del inventario debe ser mayor a 0.", 400);
            }

            Optional<Inventario> inventario = inventarioRepository.findById(idInventario);

            if (inventario.isEmpty()) {
                logger.warn("Inventario no encontrado con id_inventario={}", idInventario);
                throw new ApiException("No se encontró el inventario con ID: " + idInventario, 400);
            }

            logger.info("Inventario encontrado con id_inventario={}", idInventario);
            return inventario.get();

        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Error inesperado al consultar inventario por id={}: {}", idInventario, ex.getMessage(), ex);
            throw new RuntimeException("Error interno al consultar el inventario.", ex);
        }
    }

    /**
     * Elimina un producto del inventario por su ID.
     *
     * @param idInventario ID del inventario a eliminar
     */
    public void eliminarInventario(int idInventario) {
        logger.info("Iniciando eliminación de inventario con id_inventario={}", idInventario);

        try {
            if (idInventario <= 0) {
                logger.warn("ID de inventario inválido para eliminar: {}", idInventario);
                throw new ApiException("El ID del inventario debe ser mayor a 0.", 400);
            }

            // Verificar que existe antes de eliminar
            Optional<Inventario> inventario = inventarioRepository.findById(idInventario);
            if (inventario.isEmpty()) {
                logger.warn("No se puede eliminar: inventario no encontrado con id_inventario={}", idInventario);
                throw new ApiException("No se encontró el inventario con ID: " + idInventario + " para eliminar.", 400);
            }

            int rowsAffected = inventarioRepository.deleteById(idInventario);

            if (rowsAffected == 0) {
                logger.warn("No se eliminó ningún registro para id_inventario={}", idInventario);
                throw new ApiException("No se pudo eliminar el inventario con ID: " + idInventario, 400);
            }

            logger.info("Inventario con id_inventario={} eliminado exitosamente", idInventario);

        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Error inesperado al eliminar inventario con id={}: {}", idInventario, ex.getMessage(), ex);
            throw new RuntimeException("Error interno al eliminar el inventario.", ex);
        }
    }
}

package com.microservicioeva.api.controller;

import com.microservicioeva.api.dto.PageResponse;
import com.microservicioeva.api.exception.ApiException;
import com.microservicioeva.api.model.Inventario;
import com.microservicioeva.api.service.InventarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para la gestión del inventario de productos.
 *
 * <p>Endpoints disponibles:</p>
 * <ul>
 *   <li>GET  /api/v1/inventario             - Consultar inventario con paginación</li>
 *   <li>GET  /api/v1/inventario/{id}        - Consultar un producto por ID</li>
 *   <li>DELETE /api/v1/inventario/{id}      - Eliminar un producto del inventario</li>
 * </ul>
 *
 * <p>CORS configurado para aceptar peticiones externas desde cualquier origen.</p>
 */
@RestController
@RequestMapping("/api/v1/inventario")
@CrossOrigin(
    origins = "*",
    allowedHeaders = "*",
    methods = {
        RequestMethod.GET,
        RequestMethod.DELETE,
        RequestMethod.OPTIONS
    }
)
public class InventarioController {

    private static final Logger logger = LoggerFactory.getLogger(InventarioController.class);

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    /**
     * Consulta todos los productos del inventario con soporte de paginación.
     *
     * @param page número de página (0-based, default: 0)
     * @param size cantidad de registros por página (default: 10, max: 100)
     * @return 200 con PageResponse, 400 si parámetros inválidos, 500 si error interno
     */
    @GetMapping
    public ResponseEntity<?> consultarInventario(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("GET /api/v1/inventario - page={}, size={}", page, size);

        try {
            PageResponse<Inventario> response = inventarioService.consultarInventario(page, size);
            logger.info("Respuesta exitosa: {} elementos en página {}", response.getContent().size(), page);
            return ResponseEntity.ok(response);

        } catch (ApiException ex) {
            logger.warn("Error de validación en GET /api/v1/inventario: {}", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "status", 400,
                            "error", "Bad Request",
                            "message", ex.getMessage()
                    ));

        } catch (Exception ex) {
            logger.error("Error interno en GET /api/v1/inventario: {}", ex.getMessage(), ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", 500,
                            "error", "Internal Server Error",
                            "message", "Error interno al consultar el inventario."
                    ));
        }
    }

    /**
     * Consulta un producto del inventario por su ID.
     *
     * @param id ID del inventario
     * @return 200 con Inventario, 400 si no existe o ID inválido, 500 si error interno
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> consultarPorId(@PathVariable int id) {

        logger.info("GET /api/v1/inventario/{}", id);

        try {
            Inventario inventario = inventarioService.consultarPorId(id);
            logger.info("Inventario encontrado para id={}", id);
            return ResponseEntity.ok(inventario);

        } catch (ApiException ex) {
            logger.warn("Error de validación en GET /api/v1/inventario/{}: {}", id, ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "status", 400,
                            "error", "Bad Request",
                            "message", ex.getMessage()
                    ));

        } catch (Exception ex) {
            logger.error("Error interno en GET /api/v1/inventario/{}: {}", id, ex.getMessage(), ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", 500,
                            "error", "Internal Server Error",
                            "message", "Error interno al consultar el inventario."
                    ));
        }
    }

    /**
     * Elimina un producto del inventario por su ID.
     *
     * @param id ID del inventario a eliminar
     * @return 200 con mensaje de éxito, 400 si no existe o ID inválido, 500 si error interno
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarInventario(@PathVariable int id) {

        logger.info("DELETE /api/v1/inventario/{}", id);

        try {
            inventarioService.eliminarInventario(id);
            logger.info("Inventario con id={} eliminado correctamente", id);
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "Inventario con ID " + id + " eliminado exitosamente."
            ));

        } catch (ApiException ex) {
            logger.warn("Error de validación en DELETE /api/v1/inventario/{}: {}", id, ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "status", 400,
                            "error", "Bad Request",
                            "message", ex.getMessage()
                    ));

        } catch (Exception ex) {
            logger.error("Error interno en DELETE /api/v1/inventario/{}: {}", id, ex.getMessage(), ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", 500,
                            "error", "Internal Server Error",
                            "message", "Error interno al eliminar el inventario."
                    ));
        }
    }
}

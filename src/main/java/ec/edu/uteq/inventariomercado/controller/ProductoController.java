package ec.edu.uteq.inventariomercado.controller;

import ec.edu.uteq.inventariomercado.dto.ApiResponse;
import ec.edu.uteq.inventariomercado.dto.ProductoPageResult;
import ec.edu.uteq.inventariomercado.dto.ProductoRequest;
import ec.edu.uteq.inventariomercado.dto.ProductoResponse;
import ec.edu.uteq.inventariomercado.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> listar(Pageable pageable) {
        ProductoPageResult resultado = productoService.listarActivos(pageable);

        return ResponseEntity.ok(
                ApiResponse.success(resultado.productos(), "Listado obtenido correctamente", resultado.meta())
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductoResponse>> crear(@Valid @RequestBody ProductoRequest request) {
        ProductoResponse creado = productoService.crear(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(creado, "Producto creado correctamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);

        return ResponseEntity.ok(ApiResponse.success(null, "Producto eliminado correctamente"));
    }
}

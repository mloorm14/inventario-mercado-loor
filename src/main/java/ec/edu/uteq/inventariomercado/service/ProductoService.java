package ec.edu.uteq.inventariomercado.service;

import ec.edu.uteq.inventariomercado.domain.Producto;
import ec.edu.uteq.inventariomercado.dto.Meta;
import ec.edu.uteq.inventariomercado.dto.ProductoPageResult;
import ec.edu.uteq.inventariomercado.dto.ProductoRequest;
import ec.edu.uteq.inventariomercado.dto.ProductoResponse;
import ec.edu.uteq.inventariomercado.exception.ProductoNoEncontradoException;
import ec.edu.uteq.inventariomercado.repository.ProductoRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    private static final String CACHE_NAME = "productos";

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Cacheable(
            value = CACHE_NAME,
            key = "'page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize + ':sort:' + #pageable.sort.toString()"
    )
    public ProductoPageResult listarActivos(Pageable pageable) {
        Page<Producto> pagina = productoRepository.findByActivoTrue(pageable);

        List<ProductoResponse> productos = pagina.getContent().stream()
                .map(ProductoResponse::from)
                .toList();

        Meta meta = new Meta(pagina.getNumber(), pagina.getSize(), pagina.getTotalElements(), pagina.getTotalPages());

        return new ProductoPageResult(productos, meta);
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Producto producto = new Producto(request.nombre(), request.categoria(), request.stock(), request.precio());
        Producto guardado = productoRepository.save(producto);
        return ProductoResponse.from(guardado);
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    @Transactional
    public void eliminar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException(id));

        producto.setActivo(false);
        productoRepository.save(producto);
    }
}

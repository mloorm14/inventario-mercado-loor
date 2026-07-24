package ec.edu.uteq.inventariomercado.dto;

import ec.edu.uteq.inventariomercado.domain.Producto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductoResponse(
        Long id,
        String nombre,
        String categoria,
        Integer stock,
        BigDecimal precio,
        Boolean activo,
        OffsetDateTime creadoEn
) {

    public static ProductoResponse from(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getCategoria(),
                producto.getStock(),
                producto.getPrecio(),
                producto.getActivo(),
                producto.getCreadoEn()
        );
    }
}

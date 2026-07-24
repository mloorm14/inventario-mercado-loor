package ec.edu.uteq.inventariomercado.dto;

import java.util.List;

public record ProductoPageResult(
        List<ProductoResponse> productos,
        Meta meta
) {
}

package ec.edu.uteq.inventariomercado.dto;

public record Meta(
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}

package ec.edu.uteq.inventariomercado.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductoRequest(

        @NotBlank
        String nombre,

        @NotBlank
        String categoria,

        @NotNull
        @Min(0)
        Integer stock,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal precio

) {
}

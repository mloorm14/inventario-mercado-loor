package ec.edu.uteq.inventariomercado.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        Meta meta,
        List<ErrorItem> errors
) {

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null, null);
    }

    public static <T> ApiResponse<T> success(T data, String message, Meta meta) {
        return new ApiResponse<>(true, data, message, meta, null);
    }

    public static <T> ApiResponse<T> error(String message, List<ErrorItem> errors) {
        return new ApiResponse<>(false, null, message, null, errors);
    }
}

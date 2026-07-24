package ec.edu.uteq.inventariomercado.web;

import ec.edu.uteq.inventariomercado.dto.ApiResponse;
import ec.edu.uteq.inventariomercado.dto.ErrorItem;
import ec.edu.uteq.inventariomercado.exception.ProductoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Credenciales invalidas", null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        List<ErrorItem> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorItem(error.getField(), error.getDefaultMessage()))
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Error de validacion", errores));
    }

    @ExceptionHandler(ProductoNoEncontradoException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductoNoEncontrado(ProductoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), null));
    }
}

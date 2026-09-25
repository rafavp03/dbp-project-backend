package dbp.projectbackend;

import dbp.projectbackend.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ProblemDetail build(int status, String title, String detail, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        problemDetail.setInstance(java.net.URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler({DuplicateResourceException.class})
    public ProblemDetail duplicateResourceHandler(DuplicateResourceException ex, HttpServletRequest request){
        return build(409, "Duplicate Resource", ex.getMessage(), request);
    }

    @ExceptionHandler({InsufficientStockException.class})
    public ProblemDetail insufficientStockHandler(InsufficientStockException ex, HttpServletRequest request){
        return build(409, "Insufficient Stock", ex.getMessage(), request);
    }

    @ExceptionHandler({InvalidOperationException.class})
    public ProblemDetail invalidOperationHandler(InvalidOperationException ex, HttpServletRequest request){
        return build(400, "Invalid Operation", ex.getMessage(), request);
    }

    @ExceptionHandler({UnauthorizedException.class})
    public ProblemDetail unauthorizedHandler(UnauthorizedException ex, HttpServletRequest request){
        return build(403, "Unauthorized", ex.getMessage(), request);
    }

    @ExceptionHandler({ResourceNotFoundException.class})
    public ProblemDetail notFoundHandler(ResourceNotFoundException ex, HttpServletRequest request){
        return build(404, "Resource Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ProblemDetail badCredentialsHandler(HttpServletRequest request){
        return build(401, "Unauthorized", "Email o contraseña incorrectos", request);
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ProblemDetail accessDeniedHandler(HttpServletRequest request){
        return build(403, "Forbidden", "No tienes permisos para realizar esta acción", request);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ProblemDetail validationHandler(MethodArgumentNotValidException ex, HttpServletRequest request){
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Datos inválidos");
        return build(400, "Validation Error", detail, request);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ProblemDetail notReadableHandler(HttpServletRequest request){
        return build(400, "Malformed Request Body", "El cuerpo de la petición no es un JSON válido o tiene un formato incorrecto", request);
    }

    @ExceptionHandler({LimiteConsultasException.class})
    public ProblemDetail limiteConsultasHandler(LimiteConsultasException ex, HttpServletRequest request){
        return build(429, "Too Many Requests", ex.getMessage(), request);
    }

    @ExceptionHandler({AsistenteNoDisponibleException.class})
    public ProblemDetail asistenteNoDisponibleHandler(AsistenteNoDisponibleException ex, HttpServletRequest request){
        return build(503, "Service Unavailable", ex.getMessage(), request);
    }

    // Parametro con formato invalido, ej. ?desde=25-09-2026 o ?por=YAPEE
    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ProblemDetail typeMismatchHandler(MethodArgumentTypeMismatchException ex, HttpServletRequest request){
        return build(400, "Invalid Parameter", "Valor invalido para el parametro '" + ex.getName() + "': " + ex.getValue(), request);
    }

    @ExceptionHandler({Exception.class})
    public ProblemDetail genericHandler(Exception ex, HttpServletRequest request){
        return build(500, "Internal Server Error", "Ocurrió un error inesperado. Intenta nuevamente más tarde.", request);
    }
}
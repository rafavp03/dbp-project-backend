package dbp.projectbackend;

import dbp.projectbackend.exceptions.*;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({DuplicateResourceException.class})
    public ProblemDetail duplicateResourceHandler(DuplicateResourceException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatus(409);
        problemDetail.setTitle("Duplicate Resource");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler({InsufficientStockException.class})
    public ProblemDetail insufficientStockHandler(InsufficientStockException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatus(409);
        problemDetail.setTitle("Insufficient Stock");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler({InvalidOperationException.class})
    public ProblemDetail invalidOperationHandler(InvalidOperationException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatus(400);
        problemDetail.setTitle("Invalid Operation");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler({ResourceNotFoundException.class})
    public ProblemDetail notFoundHandler(ResourceNotFoundException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatus(404);
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ProblemDetail badCredentialsHandler(){
        ProblemDetail problemDetail = ProblemDetail.forStatus(401);
        problemDetail.setTitle("Unauthorized");
        problemDetail.setDetail("Email o contraseña incorrectos");
        return problemDetail;
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ProblemDetail accessDeniedHandler(){
        ProblemDetail problemDetail = ProblemDetail.forStatus(403);
        problemDetail.setTitle("Forbidden");
        problemDetail.setDetail("No tienes permisos para realizar esta acción");
        return problemDetail;
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ProblemDetail validationHandler(MethodArgumentNotValidException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatus(400);
        problemDetail.setTitle("Validation Error");
        problemDetail.setDetail(ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Datos inválidos"));
        return problemDetail;
    }
}

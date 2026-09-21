package dbp.projectbackend;

import dbp.projectbackend.exceptions.DataIntegrityViolationException;
import dbp.projectbackend.exceptions.ResourceNotFoundException;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({DataIntegrityViolationException.class})
    public ProblemDetail dataIntegrityViolationHandler(){
        ProblemDetail problemDetail = ProblemDetail.forStatus(409);
        problemDetail.setTitle("Data Integrity Violation");
        problemDetail.setDetail("Error de restricción en la base de datos");
        return problemDetail;
    }

    @ExceptionHandler({ResourceNotFoundException.class})
    public ProblemDetail notFoundHandler(ResourceNotFoundException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatus(404);
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }
}

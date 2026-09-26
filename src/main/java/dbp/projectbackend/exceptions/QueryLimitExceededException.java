package dbp.projectbackend.exceptions;

public class QueryLimitExceededException extends RuntimeException {
    public QueryLimitExceededException(String message) {
        super(message);
    }
}

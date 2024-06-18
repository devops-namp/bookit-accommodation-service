package uns.ac.rs.exception;

public abstract class GenericException extends RuntimeException {
    public GenericException(String message) {
        super(message);
    }

    public abstract int getErrorCode();
}

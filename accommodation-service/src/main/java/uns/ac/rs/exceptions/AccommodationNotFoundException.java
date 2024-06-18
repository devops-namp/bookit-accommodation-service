package uns.ac.rs.exceptions;

public class AccommodationNotFoundException extends GenericException {

    public AccommodationNotFoundException() {
        super("Accommodation not found");
    }

    @Override
    public int getErrorCode() {
        return 404;
    }
}

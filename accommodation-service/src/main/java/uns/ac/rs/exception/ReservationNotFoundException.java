package uns.ac.rs.exception;

public class ReservationNotFoundException extends GenericException{

    public ReservationNotFoundException(String s) {
        super(s);
    }

    @Override
    public int getErrorCode() {
        return 404;
    }

}

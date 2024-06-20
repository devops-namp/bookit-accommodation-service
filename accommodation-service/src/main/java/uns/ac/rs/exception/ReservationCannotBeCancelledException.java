package uns.ac.rs.exception;

public class ReservationCannotBeCancelledException extends GenericException{


    public ReservationCannotBeCancelledException(String s) {
        super(s);
    }

    @Override
    public int getErrorCode() {
        return 400;
    }
}

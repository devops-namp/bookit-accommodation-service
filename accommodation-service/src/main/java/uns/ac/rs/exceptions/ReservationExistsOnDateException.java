package uns.ac.rs.exceptions;

public class ReservationExistsOnDateException extends GenericException {
    public ReservationExistsOnDateException() {
        super("Unable to update availability because reservation exists in chosen interval");
    }

    @Override
    public int getErrorCode() {
        return 400;
    }
}

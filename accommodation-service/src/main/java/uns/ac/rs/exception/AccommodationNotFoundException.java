package uns.ac.rs.exception;

public class AccommodationNotFoundException  extends GenericException{

    public AccommodationNotFoundException (String s) {
        super(s);
    }

    @Override
    public int getErrorCode() {
        return 404;
    }

}

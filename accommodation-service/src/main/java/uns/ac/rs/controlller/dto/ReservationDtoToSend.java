package uns.ac.rs.controlller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uns.ac.rs.entity.Reservation;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDtoToSend {
    private Long id;
    private AccommodationDto accommodationDto;
    private String guestUsername;
    private String hostUsername;
    private String fromDate;
    private String toDate;
    private int numOfGusts;
    private double totalPrice;
    private String state;

    public ReservationDtoToSend(Reservation reservation, String fromDate, String toDate, int numOfGusts, double totalPrice, AccommodationDto accommodationDto) {
        this(
                reservation.getId(),
                accommodationDto,
                reservation.getGuestUsername(),
                accommodationDto.getHostUsername(),
                fromDate,
                toDate,
                numOfGusts,
                totalPrice,
                String.valueOf(reservation.getState())
        );
    }

    public ReservationDtoToSend(Reservation r) {
        setId(r.getId());
        setAccommodationDto(new AccommodationDto(r.getAccommodation()));
        setFromDate(String.valueOf(r.getFromDate()));
        setToDate(String.valueOf(r.getToDate()));
        setNumOfGusts(r.getNumOfGuests());
        setTotalPrice(r.getTotalPrice());
        setGuestUsername(r.getGuestUsername());
        setState(String.valueOf(r.getState()));

    }
}

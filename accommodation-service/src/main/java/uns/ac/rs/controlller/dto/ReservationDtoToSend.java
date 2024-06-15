package uns.ac.rs.controlller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import uns.ac.rs.entity.Reservation;

@Getter
@Setter
@AllArgsConstructor
public class ReservationDtoToSend {
    private Long id;
    private AccommodationDto accommodationDto;
    private String guestUsername;
    private String hostUsername;
    private String fromDate;
    private String toDate;
    private int numOfGusts;
    private double totalPrice;

    public ReservationDtoToSend(Reservation reservation, String fromDate, String toDate, int numOfGusts, double totalPrice, AccommodationDto accommodationDto) {
        this(
                reservation.getId(),
                accommodationDto,
                reservation.getGuestUsername(),
                accommodationDto.getHostUsername(),
                fromDate,
                toDate,
                numOfGusts,
                totalPrice
        );
    }

}

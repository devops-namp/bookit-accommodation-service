package uns.ac.rs.controlller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import uns.ac.rs.entity.Accommodation;
import uns.ac.rs.entity.Reservation;

import java.util.ArrayList;

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

    public ReservationDtoToSend(Reservation reservation, String fromDate, String toDate) {
        this(
                reservation.getId(),
                new AccommodationDto(reservation.getAccommodation()),
                reservation.getGuestUsername(),
                reservation.getAccommodation().getHostUsername(),
                fromDate,
                toDate
        );
    }

}

package uns.ac.rs.controlller.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uns.ac.rs.entity.Accommodation;
import uns.ac.rs.entity.PriceAdjustmentDate;
import uns.ac.rs.entity.Reservation;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDto {
    private String accommodationId;
    private String guestUsername;
    private String hostUsername;
    private String fromDate;
    private String toDate;
    private int numOfGusts;
    private double totalPrice;

    public ReservationDto(Reservation reservation)
    {
        this.accommodationId=String.valueOf(reservation.getAccommodation().getId());
        this.guestUsername = reservation.getGuestUsername();
        this.hostUsername = reservation.getAccommodation().getHostUsername();
        this.fromDate = String.valueOf(reservation.getFromDate());
        this.toDate = String.valueOf(reservation.getToDate());
        this.numOfGusts = reservation.getNumOfGuests();
        this.totalPrice = reservation.getTotalPrice();
    }
}

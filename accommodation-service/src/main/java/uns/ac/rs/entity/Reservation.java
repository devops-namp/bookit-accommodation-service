package uns.ac.rs.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uns.ac.rs.controlller.dto.AccommodationDto;
import uns.ac.rs.controlller.dto.ReservationDto;
import uns.ac.rs.service.Utils;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "accommodation_id")
    private Accommodation accommodation;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceAdjustmentDate> priceAdjustmentDate;

    private String guestUsername;

    private String state;

    private int numOfGuests;

    private LocalDate fromDate;
    private LocalDate toDate;
    private double totalPrice;

    public Reservation(ReservationDto reservationDto, Accommodation accommodation) {
        setAccommodation(accommodation);
        setGuestUsername(reservationDto.getGuestUsername());
        setState(String.valueOf(Reservation.ReservationState.PENDING));
        setNumOfGuests(reservationDto.getNumOfGusts());
//        setFromDate(Utils.convertToLocaldate(reservationDto.getFromDate()).minusDays(1));
//        setToDate(Utils.convertToLocaldate(reservationDto.getToDate()).plusDays(1));
        setFromDate(Utils.convertToLocaldate(reservationDto.getFromDate());
        setToDate(Utils.convertToLocaldate(reservationDto.getToDate());
        setTotalPrice(reservationDto.getTotalPrice());
    }


    public enum ReservationState {
        PENDING, APPROVED, DECLINED
    }
}
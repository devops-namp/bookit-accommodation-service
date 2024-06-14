package uns.ac.rs.controlller.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import uns.ac.rs.entity.Accommodation;
import uns.ac.rs.entity.PriceAdjustmentDate;
import uns.ac.rs.entity.Reservation;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ReservationDto {
    private String accommodationId;
    private String guestUsername;
    private String hostUsername;
    private String fromDate;
    private String toDate;
}

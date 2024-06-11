package uns.ac.rs.controlller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uns.ac.rs.entity.Accommodation;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationWithPrice {
    private AccommodationDto accommodation;
    private Double totalPrice;
}
package uns.ac.rs.controlller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uns.ac.rs.entity.PriceAdjustment;

@Getter
@AllArgsConstructor
public class PriceAdjustmentDto {
    private Long id;
    private Long accommodationId;
    private PriceAdjustmentDateDto priceAdjustmentDate;

    public PriceAdjustmentDto(PriceAdjustment priceAdjustment) {
        this(
            priceAdjustment.getId(),
            priceAdjustment.getAccommodation().getId(),
            new PriceAdjustmentDateDto(priceAdjustment.getPriceAdjustmentDate())
        );
    }
}

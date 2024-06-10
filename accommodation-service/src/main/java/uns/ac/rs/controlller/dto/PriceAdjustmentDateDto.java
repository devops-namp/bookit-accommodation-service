package uns.ac.rs.controlller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uns.ac.rs.entity.PriceAdjustmentDate;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class PriceAdjustmentDateDto {
    private LocalDate date;
    private double price;
    private Long priceAdjustmentId;

    public PriceAdjustmentDateDto(PriceAdjustmentDate priceAdjustmentDate) {
        this(
            priceAdjustmentDate.getDate(),
            priceAdjustmentDate.getPrice(),
            priceAdjustmentDate.getPriceAdjustment().getId()
        );
    }
}

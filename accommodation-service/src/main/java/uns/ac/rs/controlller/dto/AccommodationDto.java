package uns.ac.rs.controlller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uns.ac.rs.entity.Accommodation;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class AccommodationDto {
    private Long id;
    private String name;
    private String location;
    private String filters;
    private int minGuests;
    private int maxGuests;
    private String priceType;
    private List<PriceAdjustmentDto> priceAdjustments;
    private List<String> images;

    public AccommodationDto(Accommodation accommodation) {
        this(
            accommodation.getId(),
            accommodation.getName(),
            accommodation.getLocation(),
            accommodation.getFilters(),
            accommodation.getMinGuests(),
            accommodation.getMaxGuests(),
            accommodation.getPriceType(),
            accommodation.getPriceAdjustments() != null ?
                accommodation.getPriceAdjustments().stream().map(PriceAdjustmentDto::new).toList() : new ArrayList<>(),
            accommodation.getImages()
        );
    }
}

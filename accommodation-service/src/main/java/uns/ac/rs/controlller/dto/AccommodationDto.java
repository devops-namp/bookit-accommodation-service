package uns.ac.rs.controlller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import uns.ac.rs.entity.Accommodation;
import uns.ac.rs.entity.Image;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Getter
@Setter
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
    private List<ImageDto> images;

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
            accommodation.getImages() != null ?
                    accommodation.getImages().stream().map(image -> new ImageDto(Base64.getEncoder().encodeToString(image.getImageData()))).toList() : new ArrayList<>()
        );
    }
}

package uns.ac.rs.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uns.ac.rs.controlller.dto.AccommodationDto;
import uns.ac.rs.controlller.dto.ImageDto;
import uns.ac.rs.controlller.dto.PriceAdjustmentDto;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bookit-accommodation")
public class Accommodation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String location;
    private String filters;
    private int minGuests;
    private int maxGuests;
    private String priceType;
    private String hostUsername;
    private Boolean autoAcceptReservations;
    private boolean deleted;

    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<PriceAdjustment> priceAdjustments;

    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Image> images;

    public Accommodation(AccommodationDto accommodationDto) {
        this.id = accommodationDto.getId();
        this.name = accommodationDto.getName();
        this.location = accommodationDto.getLocation();
        this.filters = accommodationDto.getFilters();
        this.minGuests = accommodationDto.getMinGuests();
        this.maxGuests = accommodationDto.getMaxGuests();
        this.priceType = accommodationDto.getPriceType();

        this.images = new ArrayList<>();
        for (ImageDto imageData : accommodationDto.getImages()) {
            Image image = new Image();
            image.setImageData(imageData.getBase64Image());
            image.setAccommodation(this);
            this.images.add(image);
        }

        if (accommodationDto.getPriceAdjustments() != null) {
            this.priceAdjustments = new ArrayList<>();
            for (PriceAdjustmentDto priceAdjustmentDto : accommodationDto.getPriceAdjustments()) {
                PriceAdjustment priceAdjustment = new PriceAdjustment();
                priceAdjustment.setAccommodation(this);
                priceAdjustment.setPriceAdjustmentDate(new PriceAdjustmentDate(
                        priceAdjustmentDto.getPriceAdjustmentDate().getDate(),
                        priceAdjustmentDto.getPriceAdjustmentDate().getPrice()
                ));
                this.priceAdjustments.add(priceAdjustment);
            }
        } else {
            this.priceAdjustments = new ArrayList<>();
        }

        this.reservations = new ArrayList<>();
        this.deleted = false;
    }
}
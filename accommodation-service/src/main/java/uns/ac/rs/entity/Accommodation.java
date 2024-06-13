package uns.ac.rs.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uns.ac.rs.controlller.dto.AccommodationDto;
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

    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL)
    private List<PriceAdjustment> priceAdjustments;

    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL)
    private List<Reservation> reservations;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "images", joinColumns = @JoinColumn(name = "accommodation_id"))
    private List<String> images;

    public Accommodation(AccommodationDto accommodationDto) {
        this.id = accommodationDto.getId();
        this.name = accommodationDto.getName();
        this.location = accommodationDto.getLocation();
        this.filters = accommodationDto.getFilters();
        this.minGuests = accommodationDto.getMinGuests();
        this.maxGuests = accommodationDto.getMaxGuests();
        this.priceType = accommodationDto.getPriceType();
        this.images = accommodationDto.getImages();

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
    }
}
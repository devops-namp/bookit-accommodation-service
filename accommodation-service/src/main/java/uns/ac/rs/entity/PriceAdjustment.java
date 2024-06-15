package uns.ac.rs.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class PriceAdjustment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "accommodation_id")
    private Accommodation accommodation;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "price_adjustment_date_id", referencedColumnName = "id")
    private PriceAdjustmentDate priceAdjustmentDate;
}
package uns.ac.rs.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class PriceAdjustmentDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    private LocalDate date;
    private double price;

    @OneToOne(mappedBy = "priceAdjustmentDate")
    private PriceAdjustment priceAdjustment;

    public PriceAdjustmentDate(LocalDate date, double price) {
        this.date = date;
        this.price = price;
    }

}

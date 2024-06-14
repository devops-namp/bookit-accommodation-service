package uns.ac.rs.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import uns.ac.rs.entity.PriceAdjustment;

import java.util.List;

@ApplicationScoped
public class PriceAdjustmentRepository implements PanacheRepository<PriceAdjustment> {
    public List<PriceAdjustment> findByAccommodationId(Long accommodationId) {
        return find("accommodation.id", accommodationId).list();
    }
}

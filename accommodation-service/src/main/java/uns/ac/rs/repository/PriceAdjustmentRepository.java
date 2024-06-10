package uns.ac.rs.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import uns.ac.rs.entity.PriceAdjustment;

@ApplicationScoped
public class PriceAdjustmentRepository implements PanacheRepository<PriceAdjustment> {
}

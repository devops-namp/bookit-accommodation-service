package uns.ac.rs.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import uns.ac.rs.entity.PriceAdjustmentDate;

@ApplicationScoped
public class PriceAdjustmentDateRepository implements PanacheRepository<PriceAdjustmentDate> {

    @Transactional
    public void clearReservationId(Long reservationId) {
        getEntityManager().createQuery("UPDATE PriceAdjustmentDate pad SET pad.reservation = NULL WHERE pad.reservation.id = :reservationId")
                .setParameter("reservationId", reservationId)
                .executeUpdate();
    }
}

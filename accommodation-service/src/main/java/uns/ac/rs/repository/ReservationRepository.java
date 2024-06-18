package uns.ac.rs.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import uns.ac.rs.entity.PriceAdjustment;
import uns.ac.rs.entity.Reservation;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class ReservationRepository implements PanacheRepository<Reservation> {

    public List<Reservation> getByHost(String username) {
        return list("SELECT r FROM Reservation r JOIN r.accommodation a WHERE a.hostUsername = ?1", username);
    }

    public List<Reservation> getByGuest(String username) {
        return list("SELECT r FROM Reservation r WHERE r.guestUsername = ?1", username);
    }

    public boolean exists(Long accommodationId, LocalDate date) {
        return list("SELECT r FROM Reservation r where r.accommodation.id = ?1 AND " +
            "r.fromDate <= ?2 AND r.toDate >= ?2 AND (r.state = 'PENDING' OR r.state = 'APPROVED')", accommodationId, date).size() > 0;
    }

    @Transactional
    public void reject(Long reservationId) {
        update("status = ?1 where id = ?2", "REJECTED", reservationId);
    }
}
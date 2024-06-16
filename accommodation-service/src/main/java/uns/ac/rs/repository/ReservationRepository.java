package uns.ac.rs.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import uns.ac.rs.entity.Reservation;

import java.util.List;

@ApplicationScoped
public class ReservationRepository implements PanacheRepository<Reservation> {

    public List<Reservation> getByHost(String username) {
        return list("SELECT r FROM Reservation r JOIN r.accommodation a WHERE a.hostUsername = ?1", username);
    }

    public List<Reservation> getByGuest(String username) {
        return list("SELECT r FROM Reservation r WHERE r.guestUsername = ?1", username);
    }


    @Transactional
    public void reject(Long reservationId) {
        update("status = ?1 where id = ?2", "REJECTED", reservationId);
    }
}
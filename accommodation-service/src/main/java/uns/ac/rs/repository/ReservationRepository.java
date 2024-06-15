package uns.ac.rs.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import uns.ac.rs.entity.Reservation;

import java.util.List;

@ApplicationScoped
public class ReservationRepository implements PanacheRepository<Reservation> {

    public List<Reservation> getByUser(String username) {
        return list("SELECT r FROM Reservation r JOIN r.accommodation a WHERE a.hostUsername = ?1", username);
    }


}
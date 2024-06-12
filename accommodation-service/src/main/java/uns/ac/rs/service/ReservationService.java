package uns.ac.rs.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import uns.ac.rs.entity.Reservation;
import uns.ac.rs.repository.ReservationRepository;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ReservationService {

    @Inject
    ReservationRepository reservationRepository;

    public List<Reservation> listAll() {
        return reservationRepository.listAll();
    }

    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findByIdOptional(id);
    }

    @Transactional
    public Reservation create(Reservation reservation) {
        reservationRepository.persist(reservation);
        return reservation;
    }

    @Transactional
    public Optional<Reservation> update(Long id, Reservation reservation) {
        Optional<Reservation> existingReservation = reservationRepository.findByIdOptional(id);
        if (existingReservation.isPresent()) {
            Reservation updatedReservation = existingReservation.get();
            updatedReservation.setAccommodation(reservation.getAccommodation());
            updatedReservation.setPriceAdjustmentDate(reservation.getPriceAdjustmentDate());
            return Optional.of(updatedReservation);
        }
        return Optional.empty();
    }

    @Transactional
    public boolean delete(Long id) {
        return reservationRepository.deleteById(id);
    }
}

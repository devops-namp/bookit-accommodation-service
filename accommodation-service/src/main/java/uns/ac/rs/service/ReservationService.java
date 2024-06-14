package uns.ac.rs.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import uns.ac.rs.controlller.dto.ReservationDto;
import uns.ac.rs.entity.Accommodation;
import uns.ac.rs.entity.PriceAdjustment;
import uns.ac.rs.entity.PriceAdjustmentDate;
import uns.ac.rs.entity.Reservation;
import uns.ac.rs.repository.PriceAdjustmentDateRepository;
import uns.ac.rs.repository.PriceAdjustmentRepository;
import uns.ac.rs.repository.ReservationRepository;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ReservationService {

    @Inject
    ReservationRepository reservationRepository;

    @Inject
    PriceAdjustmentRepository priceAdjustmentRepository;

    @Inject
    PriceAdjustmentDateRepository priceAdjustmentDateRepository;

    @Inject
    AccommodationService accommodationService;

    public List<Reservation> listAll() {
        return reservationRepository.listAll();
    }

    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findByIdOptional(id);
    }

    @Transactional
    public Reservation create(ReservationDto reservationDto) {
        Optional<Accommodation> accommodationOptional = accommodationService.getById(Long.parseLong(reservationDto.getAccommodationId()));
        if (!accommodationOptional.isPresent()) {
            throw new IllegalArgumentException("Accommodation not found");
        }
        Accommodation accommodation = accommodationOptional.get();
        Reservation reservation = new Reservation();
        reservation.setAccommodation(accommodation);
        reservation.setGuestUsername(reservationDto.getGuestUsername());

        List<PriceAdjustment> priceAdjustments = priceAdjustmentRepository.findByAccommodationId(accommodation.getId());
        for (PriceAdjustment pa : priceAdjustments) {
            PriceAdjustmentDate pad = pa.getPriceAdjustmentDate();
            pad.setReservation(reservation);
        }
        reservation.setPriceAdjustmentDate(priceAdjustments.stream().map(PriceAdjustment::getPriceAdjustmentDate).toList());
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

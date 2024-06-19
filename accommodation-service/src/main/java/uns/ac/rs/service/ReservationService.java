package uns.ac.rs.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import uns.ac.rs.controlller.dto.AccommodationDto;
import uns.ac.rs.controlller.dto.ReservationDto;
import uns.ac.rs.controlller.dto.ReservationDtoToSend;
import uns.ac.rs.entity.Accommodation;
import uns.ac.rs.entity.Reservation;
import uns.ac.rs.repository.PriceAdjustmentDateRepository;
import uns.ac.rs.repository.PriceAdjustmentRepository;
import uns.ac.rs.repository.ReservationRepository;

import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<ReservationDtoToSend> listAll() {
        List<ReservationDtoToSend> reservationDtoToSends = new ArrayList<>();
        for(Reservation reservation : reservationRepository.listAll()){
            ReservationDtoToSend reservationDtoToSend = new ReservationDtoToSend(reservation,String.valueOf(reservation.getFromDate()),String.valueOf(reservation.getToDate()),reservation.getNumOfGuests(), reservation.getTotalPrice(),new AccommodationDto(reservation.getAccommodation()));
            reservationDtoToSends.add(reservationDtoToSend);
        }

        return reservationDtoToSends;
    }

    public Optional<ReservationDtoToSend> findById(Long id) {
        Optional<Reservation> optionalReservation = reservationRepository.findByIdOptional(id);

        if (optionalReservation.isPresent()) {
            ReservationDtoToSend reservationDtoToSend = new ReservationDtoToSend(optionalReservation.get());
            return Optional.of(reservationDtoToSend);
        } else {
            return Optional.empty();
        }
    }

    @Transactional
    public Reservation create(ReservationDto reservationDto) {
        Optional<Accommodation> accommodationOptional = accommodationService.getById(Long.parseLong(reservationDto.getAccommodationId()));
        if (!accommodationOptional.isPresent()) {
            throw new IllegalArgumentException("Accommodation not found");
        }
        Reservation reservation = new Reservation(reservationDto,accommodationOptional.orElseGet(null));



//        Accommodation accommodation = accommodationOptional.get();
//        List<PriceAdjustment> priceAdjustments = priceAdjustmentRepository.findByAccommodationId(accommodation.getId());
//        List<PriceAdjustmentDate> priceAdjustmentDates = new ArrayList<>();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        LocalDate fromDate = LocalDate.parse(reservationDto.getFromDate(), formatter).minusDays(1);
//        LocalDate toDate = LocalDate.parse(reservationDto.getToDate(), formatter).plusDays(1);


//        for (PriceAdjustment pa : priceAdjustments) {
//            PriceAdjustmentDate pad = pa.getPriceAdjustmentDate();
//            if(pad.getDate().isAfter(fromDate) && pad.getDate().isBefore(toDate))
//                priceAdjustmentDates.add(pad);
//        }
//        reservation.setPriceAdjustmentDate(priceAdjustments.stream().map(PriceAdjustment::getPriceAdjustmentDate).toList());
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

    public void approve(Long reservationId) {
        // TODO

    }



    @Transactional
    public List<ReservationDtoToSend> getByHost(String username) {
        return reservationRepository.getByHost(username).stream()
                .map(ReservationDtoToSend::new)
                .collect(Collectors.toList());
    }

    public List<ReservationDtoToSend> getByGuest(String username) {
        return reservationRepository.getByGuest(username).stream()
                .map(ReservationDtoToSend::new)
                .collect(Collectors.toList());
    }
    public void reject(Long reservationId) {
        reservationRepository.reject(reservationId);
    }

    public boolean hasFutureReservations(String username, String role) {
        if (role.equals("GUEST")) {
            return reservationRepository.getByGuest(username)
                .stream()
                .filter(reservation -> reservation.getState().equals("APPROVED") && reservation.getToDate().isAfter(LocalDate.now()))
                .toList().size() > 0;
        }
        if (role.equals("HOST")) {
            return reservationRepository.getByHost(username)
                .stream()
                .filter(reservation -> reservation.getState().equals("APPROVED") && reservation.getToDate().isAfter(LocalDate.now()))
                .toList().size() > 0;
        }
        return false;
    }

}

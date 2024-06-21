package uns.ac.rs.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import uns.ac.rs.controlller.dto.AccommodationDto;
import uns.ac.rs.controlller.dto.ReservationDto;
import uns.ac.rs.controlller.dto.ReservationDtoToSend;
import uns.ac.rs.entity.*;
import uns.ac.rs.entity.events.AutoApproveEvent;
import uns.ac.rs.entity.events.NotificationEvent;
import uns.ac.rs.entity.events.NotificationType;
import uns.ac.rs.exceptions.ReservationCannotBeCancelledException;
import uns.ac.rs.exceptions.ReservationNotFoundException;
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

    @Inject
    @Channel("notification-queue")
    Emitter<NotificationEvent> eventEmitter;

    @Inject
    @Channel("autoapprove-acc-to-user-queue")
    Emitter<AutoApproveEvent> autoApproveEmmiter;

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
        if (accommodationOptional.isEmpty()) {
            throw new IllegalArgumentException("Accommodation not found");
        }
        Reservation reservation = new Reservation(reservationDto,accommodationOptional.orElseGet(null));
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

    @Transactional
    public void approve(Long reservationId) {
        // promeni se status
        changeStatus(reservationId, Reservation.ReservationState.APPROVED);
        // dodaju se priceAdjustmenti
        addPriceAdjuctmentDates(reservationId);
        // odbijamo ostale koji su u tom periodu
        rejectOthers(reservationId);
        // saljemo notifikaciju gostu
    }


    private void rejectOthers(Long reservationId) {
        Optional<Reservation> r = reservationRepository.findByIdOptional(reservationId);
        if (r.isEmpty()) throw new ReservationNotFoundException("Reservation with id " + reservationId + " does not exist.");
        reservationRepository.rejectOthers(r.get());
    }

    private void addPriceAdjuctmentDates(Long reservationId) {
        Optional<Reservation> reservationOptional = reservationRepository.findByIdOptional(reservationId);
        if (reservationOptional.isEmpty()) throw new IllegalArgumentException("Reservation not found");
        Reservation reservation = reservationOptional.get();
        Accommodation accommodation = reservation.getAccommodation();

        List<PriceAdjustment> priceAdjustments = priceAdjustmentRepository.findByAccommodationId(accommodation.getId());
        List<PriceAdjustmentDate> priceAdjustmentDates = new ArrayList<>();
        LocalDate fromDate = reservation.getFromDate().minusDays(1);
        LocalDate toDate = reservation.getToDate().plusDays(1);

        for (PriceAdjustment pa : priceAdjustments) {
            PriceAdjustmentDate pad = pa.getPriceAdjustmentDate();
            if(pad.getDate().isAfter(fromDate) && pad.getDate().isBefore(toDate)) {
                priceAdjustmentDates.add(pad);
            }
        }
        reservation.setPriceAdjustmentDate(priceAdjustmentDates);
        reservationRepository.persist(reservation);

        for(PriceAdjustmentDate priceAdjustmentDate : priceAdjustmentDates){
            priceAdjustmentDate.setReservation(reservation);
            priceAdjustmentDateRepository.persist(priceAdjustmentDate);
        }
    }


    @Transactional
    public List<ReservationDtoToSend> getByHost(String username) {
        return reservationRepository.getByHost(username).stream()
                .map(ReservationDtoToSend::new)
                .collect(Collectors.toList());
    }


    @Transactional
    public List<ReservationDtoToSend> getByGuest(String username) {
        return reservationRepository.getByGuest(username).stream()
                .map(ReservationDtoToSend::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ReservationDtoToSend> getByGuestHistory(String username) {
        return reservationRepository.getByGuestHistory(username).stream()
                .map(ReservationDtoToSend::new)
                .collect(Collectors.toList());
    }


    @Transactional
    public Reservation changeStatus(Long reservationId, Reservation.ReservationState newState) {
        Optional<Reservation> optionalReservation = reservationRepository.findByIdOptional(reservationId);
        Reservation reservation = optionalReservation.orElseThrow(() ->
                new ReservationNotFoundException("Reservation not found with id " + reservationId));
        reservation.setState(newState);
        reservationRepository.persist(reservation);
        sendAcceptedOrDeclinedNotification(reservation);
        return reservation;
    }

    public boolean hasFutureReservations(String username, String role) {
        if (role.equals("GUEST")) {
            return !reservationRepository.getByGuest(username)
                    .stream()
                    .filter(reservation -> reservation.getState().equals(Reservation.ReservationState.APPROVED) && reservation.getToDate().isAfter(LocalDate.now()))
                    .toList().isEmpty();
        }
        if (role.equals("HOST")) {
            return !reservationRepository.getByHost(username)
                    .stream()
                    .filter(reservation -> reservation.getState().equals(Reservation.ReservationState.APPROVED) && reservation.getToDate().isAfter(LocalDate.now()))
                    .toList().isEmpty();
        }
        return false;
    }

    private void sendAcceptedOrDeclinedNotification(Reservation r) {
        String text;
        if (r.getState().equals(Reservation.ReservationState.APPROVED)) {
            text = "Your reservation has been approved";
        } else {
            text = "Your reservation has been rejected";
        }
        NotificationEvent e = new NotificationEvent(text, r.getGuestUsername(), NotificationType.RESERVATION_REQUEST_RESOLVED);
        eventEmitter.send(e);
    }

    public void rejectByGuest(Long reservationId) {
        Reservation r = changeStatus(reservationId, Reservation.ReservationState.DECLINED);
        if (!r.getFromDate().isAfter(LocalDate.now().plusDays(1))) throw new ReservationCannotBeCancelledException("Reservation cannot be cancelled as it starts tomorrow or has already started");
        NotificationEvent e = new NotificationEvent(r.getGuestUsername() + " canceled the reservation.", r.getGuestUsername(), NotificationType.RESERVATION_DECLINED);
        eventEmitter.send(e);
        autoApproveEmmiter.send(new AutoApproveEvent(r.getGuestUsername(), AutoApproveEvent.AutoApproveEventType.INCREMENT));
        deletePriceAdjustments(r);
    }

    private void deletePriceAdjustments(Reservation r) {
        priceAdjustmentDateRepository.clearReservationId(r.getId());
    }

    public void hadleAutoapprove(Accommodation accommodation, Reservation created) {
        if (accommodation.getAutoAcceptReservations()) {
            approve(created.getId());
        } else {
            eventEmitter.send(new NotificationEvent("You have new reservation request", accommodation.getHostUsername(), NotificationType.RESERVATION_REQUEST_CREATED));
        }

    }
}

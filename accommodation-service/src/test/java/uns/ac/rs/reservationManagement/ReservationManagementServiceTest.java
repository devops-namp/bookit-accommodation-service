
package uns.ac.rs.reservationManagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import io.quarkus.test.junit.QuarkusTest;

import uns.ac.rs.controlller.dto.ReservationDto;
import uns.ac.rs.controlller.dto.ReservationDtoToSend;
import uns.ac.rs.entity.*;
import uns.ac.rs.entity.events.NotificationEvent;
import uns.ac.rs.repository.*;
import uns.ac.rs.service.*;

@QuarkusTest
class ReservationManagementServiceTest {

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    PriceAdjustmentRepository priceAdjustmentRepository;

    @Mock
    PriceAdjustmentDateRepository priceAdjustmentDateRepository;

    @Mock
    AccommodationService accommodationService;

    @Mock
    @Channel("notification-queue")
    Emitter<NotificationEvent> eventEmitter;

    @InjectMocks
    ReservationService reservationService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testApprove() {
        Long reservationId = 1L;
        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setState(Reservation.ReservationState.PENDING);
        reservation.setFromDate(LocalDate.of(2023, 6, 1));
        reservation.setToDate(LocalDate.of(2023, 6, 10));

        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        reservation.setAccommodation(accommodation);

        when(reservationRepository.findByIdOptional(reservationId)).thenReturn(Optional.of(reservation));
        when(priceAdjustmentRepository.findByAccommodationId(accommodation.getId())).thenReturn(Collections.emptyList());
        doNothing().when(reservationRepository).persist(any(Reservation.class));
        doNothing().when(priceAdjustmentDateRepository).persist(any(PriceAdjustmentDate.class));
        doNothing().when(reservationRepository).rejectOthers(any(Reservation.class));
        when(priceAdjustmentRepository.findByAccommodationId(accommodation.getId())).thenReturn(Collections.emptyList());

        reservationService.approve(reservationId);

        assertEquals(Reservation.ReservationState.APPROVED, reservation.getState());

        assertNotNull(reservation.getPriceAdjustmentDate());
        verify(reservationRepository, times(2)).persist(any(Reservation.class));

        verify(eventEmitter).send(any(NotificationEvent.class));
    }

    @Test
    void testGetByHost() {
        String username = "hostUsername";

        Accommodation accommodation1 = new Accommodation();
        accommodation1.setId(1L);
        Accommodation accommodation2 = new Accommodation();
        accommodation2.setId(2L);

        List<Reservation> reservations = Arrays.asList(
                new Reservation(new ReservationDto("1l", "", "", "19-06-2024", "21-06-2024", 3, 6.00), accommodation1),  // Prva rezervacija sa smeštajem 1
                new Reservation(new ReservationDto("2l", "", "", "24-06-2024", "29-06-2024", 3, 6.00), accommodation2)   // Druga rezervacija sa smeštajem 2
        );

        when(reservationRepository.getByHost(username)).thenReturn(reservations);
        List<ReservationDtoToSend> result = reservationService.getByHost(username);
        assertEquals(reservations.size(), result.size());
        for (int i = 0; i < reservations.size(); i++) {
            assertEquals(reservations.get(i).getId(), result.get(i).getId());
        }
    }

    @Test
    void testHandleAutoapprove_AutoApproveFalse() {
        Accommodation accommodation = new Accommodation();
        accommodation.setAutoApprove(false);
        accommodation.setHostUsername("hostUsername");

        Reservation created = new Reservation();
        created.setId(1L);

        when(reservationRepository.findById(created.getId())).thenReturn(created);

        reservationService.hadleAutoapprove(accommodation, created);
        verify(eventEmitter).send(any(NotificationEvent.class));
    }


    @Test
    void testGetByGuest() {
        String username = "guestUsername";

        List<Reservation> reservations = Arrays.asList(
                new Reservation(new ReservationDto("1l", username, "", "19-06-2024", "21-06-2024", 3, 6.00), new Accommodation()),
                new Reservation(new ReservationDto("2l", username, "", "24-06-2024", "29-06-2024", 3, 6.00), new Accommodation())
        );

        when(reservationRepository.getByGuest(username)).thenReturn(reservations);
        List<ReservationDtoToSend> result = reservationService.getByGuest(username);
        assertEquals(reservations.size(), result.size());
        for (int i = 0; i < reservations.size(); i++) {
            assertEquals(reservations.get(i).getId(), result.get(i).getId());
        }
    }

    @Test
    void testChangeStatus() {
        Long reservationId = 1L;
        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setState(Reservation.ReservationState.PENDING);
        reservation.setGuestUsername("guestUsername");

        when(reservationRepository.findByIdOptional(reservationId)).thenReturn(Optional.of(reservation));
        doNothing().when(reservationRepository).persist(any(Reservation.class));

        reservationService.changeStatus(reservationId, Reservation.ReservationState.APPROVED);

        assertEquals(Reservation.ReservationState.APPROVED, reservation.getState());
        verify(reservationRepository).persist(reservation);
        verify(eventEmitter).send(any(NotificationEvent.class));
    }

//    @Test
//    void testHandleAutoapprove_AutoApproveTrue() {
//        Accommodation accommodation = new Accommodation();
//        accommodation.setId(1L);  // Dodavanje ID-a smeštaja
//        accommodation.setAutoApprove(true);
//        accommodation.setHostUsername("hostUsername");
//
//        Reservation created = new Reservation();
//        created.setId(1L);
//        created.setState(Reservation.ReservationState.PENDING);
//        created.setAccommodation(accommodation);  // Povezivanje smeštaja sa rezervacijom
//        created.setFromDate(LocalDate.now().plusDays(1));  // Postavljanje datuma početka rezervacije
//        created.setToDate(LocalDate.now().plusDays(5));  // Postavljanje datuma završetka rezervacije
//
//        when(reservationRepository.findByIdOptional(created.getId())).thenReturn(Optional.of(created));
//
//        reservationService.hadleAutoapprove(accommodation, created);
//
//        assertEquals(Reservation.ReservationState.APPROVED, created.getState());
//        verify(reservationRepository, times(2)).persist(any(Reservation.class));
//        verify(eventEmitter, times(2)).send(any(NotificationEvent.class));
//    }

    @Test
    void testHandleAutoapprove_AutoApproveTrue() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);  // Dodavanje ID-a smeštaja
        accommodation.setAutoApprove(true);
        accommodation.setHostUsername("hostUsername");

        Reservation created = new Reservation();
        created.setId(1L);
        created.setState(Reservation.ReservationState.PENDING);
        created.setAccommodation(accommodation);  // Povezivanje smeštaja sa rezervacijom
        created.setFromDate(LocalDate.now().plusDays(1));  // Postavljanje datuma početka rezervacije
        created.setToDate(LocalDate.now().plusDays(5));  // Postavljanje datuma završetka rezervacije

        when(reservationRepository.findByIdOptional(created.getId())).thenReturn(Optional.of(created));

        reservationService.hadleAutoapprove(accommodation, created);

        assertEquals(Reservation.ReservationState.APPROVED, created.getState());
        verify(reservationRepository, times(2)).persist(any(Reservation.class));
        verify(eventEmitter, times(1)).send(any(NotificationEvent.class));
    }




}

package uns.ac.rs;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uns.ac.rs.controlller.dto.ReservationDto;
import uns.ac.rs.controlller.dto.ReservationDtoToSend;
import uns.ac.rs.entity.Accommodation;
import uns.ac.rs.entity.Reservation;
import uns.ac.rs.repository.ReservationRepository;
import uns.ac.rs.service.AccommodationService;
import uns.ac.rs.service.ReservationService;
import uns.ac.rs.service.Utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class ReservationServiceTest {

    @Mock
    ReservationRepository reservationRepository;

    @InjectMocks
    ReservationService reservationService;

    @Mock
    AccommodationService accommodationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAll() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);

        Reservation reservation = new Reservation();
        reservation.setAccommodation(accommodation);

        List<Reservation> reservations = List.of(reservation);
        when(reservationRepository.listAll()).thenReturn(reservations);

        List<ReservationDtoToSend> result = reservationService.listAll();
        assertEquals(1, result.size());
        verify(reservationRepository, times(1)).listAll();
    }

    @Test
    void testGetById() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setAccommodation(accommodation);

        when(reservationRepository.findByIdOptional(1L)).thenReturn(Optional.of(reservation));

        Optional<ReservationDtoToSend> result = reservationService.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(reservation.getId(), result.get().getId());
        verify(reservationRepository, times(1)).findByIdOptional(1L);
    }

    @Test
    void testAddReservation() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setName("Test Accommodation");
        accommodation.setLocation("Test Location");
        accommodation.setFilters("Pool");
        accommodation.setMinGuests(1);
        accommodation.setMaxGuests(2);
        accommodation.setPriceType("Fixed");
        accommodation.setHostUsername("Host");

        when(accommodationService.getById(1L)).thenReturn(Optional.of(accommodation));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setFromDate(Utils.convertToLocaldate("15-06-2024"));
        reservation.setToDate(Utils.convertToLocaldate("20-06-2024"));
        reservation.setGuestUsername("guestUser");
        reservation.setNumOfGuests(2);
        reservation.setTotalPrice(200.0);
        reservation.setAccommodation(accommodation);

        ReservationDto reservationDto = new ReservationDto(reservation);
        reservationDto.setFromDate("15-06-2024");
        reservationDto.setToDate("20-06-2024");

        doNothing().when(reservationRepository).persist(any(Reservation.class));

        when(reservationRepository.findByIdOptional(1L)).thenReturn(Optional.of(reservation));

        reservationService.create(reservationDto);

        Optional<ReservationDtoToSend> result = reservationService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(reservation.getTotalPrice(), result.get().getTotalPrice());
        assertEquals(reservation.getGuestUsername(), result.get().getGuestUsername());
        assertEquals(reservation.getAccommodation().getId(), result.get().getAccommodationDto().getId());
    }



    @Test
    void testDeleteReservation() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        when(reservationRepository.findByIdOptional(reservation.getId())).thenReturn(Optional.of(reservation));

        reservationService.delete(reservation.getId());

        verify(reservationRepository, times(1)).deleteById(reservation.getId());
    }
}

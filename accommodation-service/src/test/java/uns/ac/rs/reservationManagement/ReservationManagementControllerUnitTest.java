package uns.ac.rs.reservationManagement;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uns.ac.rs.controlller.ReservationController;
import uns.ac.rs.controlller.dto.ReservationDtoToSend;
import uns.ac.rs.entity.Reservation;
import uns.ac.rs.service.AccommodationService;
import uns.ac.rs.service.ReservationService;

import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@QuarkusTest
public class ReservationManagementControllerUnitTest {

    @Mock
    ReservationService reservationService;

    @Mock
    AccommodationService accommodationService;

    @InjectMocks
    ReservationController reservationController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testListAll() {
        List<Reservation> reservations = List.of(new Reservation(), new Reservation());
        when(reservationService.listAll()).thenReturn(reservations);

        List<Reservation> result = reservationController.listAll();

        assertEquals(2, result.size());
        verify(reservationService, times(1)).listAll();
    }

    @Test
    public void testFindById() {
        Long id = 1L;
        Reservation reservation = new Reservation();
        when(reservationService.findById(id)).thenReturn(Optional.of(reservation));

        Response response = reservationController.findById(id);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(reservation, response.getEntity());
        verify(reservationService, times(1)).findById(id);
    }

    @Test
    public void testFindByIdNotFound() {
        Long id = 1L;
        when(reservationService.findById(id)).thenReturn(Optional.empty());

        Response response = reservationController.findById(id);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(reservationService, times(1)).findById(id);
    }
    @Test
    public void testGetByHost() {
        String username = "host1";
        List<ReservationDtoToSend> reservations = List.of(new ReservationDtoToSend(), new ReservationDtoToSend());
        when(reservationService.getByHost(username)).thenReturn(reservations);

        Response response = reservationController.getByHost(username);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(reservations, response.getEntity());
        verify(reservationService, times(1)).getByHost(username);
    }

    @Test
    public void testGetByGuest() {
        String username = "guest1";
        List<ReservationDtoToSend> reservations = List.of(new ReservationDtoToSend(), new ReservationDtoToSend());
        when(reservationService.getByGuest(username)).thenReturn(reservations);

        Response response = reservationController.getByGuest(username);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(reservations, response.getEntity());
        verify(reservationService, times(1)).getByGuest(username);
    }

    @Test
    public void testApproveReservation() {
        Long reservationId = 1L;

        Response response = reservationController.approveReservation(reservationId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(reservationService, times(1)).approve(reservationId);
    }

    @Test
    public void testRejectReservationHost() {
        Long reservationId = 1L;

        Response response = reservationController.rejectReservationHost(reservationId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(reservationService, times(1)).changeStatus(reservationId, Reservation.ReservationState.DECLINED);
    }

    @Test
    public void testRejectReservationGuest() {
        Long reservationId = 1L;

        Response response = reservationController.rejectReservationGuest(reservationId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(reservationService, times(1)).rejectByGuest(reservationId);
    }

}

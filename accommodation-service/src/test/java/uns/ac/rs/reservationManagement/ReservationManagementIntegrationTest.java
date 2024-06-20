package uns.ac.rs.reservationManagement;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uns.ac.rs.controlller.dto.ReservationDto;
import uns.ac.rs.entity.Accommodation;
import uns.ac.rs.entity.Reservation;
import uns.ac.rs.repository.AccommodationRepository;
import uns.ac.rs.repository.PriceAdjustmentDateRepository;
import uns.ac.rs.repository.PriceAdjustmentRepository;
import uns.ac.rs.repository.ReservationRepository;
import uns.ac.rs.resources.PostgresResource;

import jakarta.inject.Inject;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import java.time.LocalDate;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
public class ReservationManagementIntegrationTest {

    @Inject
    ReservationRepository reservationRepository;

    @Inject
    AccommodationRepository accommodationRepository;

    @Inject
    PriceAdjustmentRepository priceAdjustmentRepository;

    @Inject
    PriceAdjustmentDateRepository priceAdjustmentDateRepository;

    private Long testAccommodationId;
    private Long testReservationId;

    @BeforeEach
    @Transactional
    public void setup() {
        priceAdjustmentRepository.deleteAll();

        priceAdjustmentDateRepository.deleteAll();
        reservationRepository.deleteAll();
        accommodationRepository.deleteAll();

        Accommodation accommodation = new Accommodation();
        accommodation.setName("Test Accommodation");
        accommodation.setLocation("Test Location");
        accommodation.setFilters("Test Filters");
        accommodation.setMinGuests(1);
        accommodation.setMaxGuests(4);
        accommodation.setPriceType("per night");
        accommodation.setHostUsername("host");
        accommodation.setAutoAcceptReservations(false);
        accommodationRepository.persist(accommodation);
        testAccommodationId = accommodation.getId();

        Reservation reservation = new Reservation();
        reservation.setAccommodation(accommodation);
        reservation.setGuestUsername("guest");
        reservation.setState(Reservation.ReservationState.PENDING);
        reservation.setNumOfGuests(2);
        reservation.setFromDate(LocalDate.of(2024, 7, 1));
        reservation.setToDate(LocalDate.of(2024, 7, 10));
        reservation.setTotalPrice(200.0);
        reservationRepository.persist(reservation);
        testReservationId = reservation.getId();
    }

    @Test
    public void testCreateReservation() {
        ReservationDto reservationDto = new ReservationDto();
        reservationDto.setAccommodationId(testAccommodationId.toString());
        reservationDto.setGuestUsername("guest");
        reservationDto.setNumOfGusts(2);
        reservationDto.setFromDate("01-07-2024");
        reservationDto.setToDate("10-07-2024");
        reservationDto.setTotalPrice(200.0);

        given()
                .contentType(ContentType.JSON)
                .body(reservationDto)
                .when()
                .post("/reservations")
                .then()
                .statusCode(201)
                .body("guestUsername", is("guest"));
    }

    @Test
    public void testGetReservationsByHost() {
        String hostUsername = "host";

        given()
                .pathParam("username", hostUsername)
                .when()
                .get("/reservations/getByHost/{username}")
                .then()
                .statusCode(200)
                .body("size()", is(1));  // Očekivana veličina liste je 1 jer smo dodali jednu rezervaciju u setup metodi
    }

    @Test
    public void testApproveReservation() {
        given()
                .contentType(ContentType.JSON)
                .pathParam("reservationId", testReservationId)
                .when()
                .post("/reservations/approve/{reservationId}")
                .then()
                .statusCode(200);
    }


    @Test
    public void testGetReservationsByGuest() {
        String guestUsername = "guest";

        given()
                .pathParam("username", guestUsername)
                .when()
                .get("/reservations/getByGuest/{username}")
                .then()
                .statusCode(200)
                .body("size()", is(1))  // Očekivana veličina liste je 1 jer smo dodali jednu rezervaciju u setup metodi
                .body("[0].guestUsername", is(guestUsername));
    }

    @Test
    public void testRejectReservationHost() {
        given()
                .contentType(ContentType.JSON)
                .pathParam("reservationId", testReservationId)
                .when()
                .post("/reservations/rejectHost/{reservationId}")
                .then()
                .statusCode(200);

        // Proverimo da li je status rezervacije promenjen
        given()
                .pathParam("username", "guest")
                .when()
                .get("/reservations/getByGuest/{username}")
                .then()
                .statusCode(200)
                .body("[0].state", is("DECLINED"));
    }


    @Test
    public void testRejectReservationGuest() {
        given()
                .contentType(ContentType.JSON)
                .pathParam("reservationId", testReservationId)
                .when()
                .post("/reservations/rejectGuest/{reservationId}")
                .then()
                .statusCode(200);

        // Proverimo da li je status rezervacije promenjen
        given()
                .pathParam("username", "guest")
                .when()
                .get("/reservations/getByGuest/{username}")
                .then()
                .statusCode(200)
                .body("[0].state", is("DECLINED"));
    }


}

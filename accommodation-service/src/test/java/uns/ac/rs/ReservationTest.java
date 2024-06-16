package uns.ac.rs;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import uns.ac.rs.controlller.dto.ReservationDto;
import uns.ac.rs.entity.Accommodation;
import uns.ac.rs.entity.Reservation;
import uns.ac.rs.resources.PostgresResource;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
class ReservationTest {

    @Test
    void testGetAll() {
        given()
                .when().get("/reservations")
                .then()
                .statusCode(200)
                .body("$.size()", is(greaterThanOrEqualTo(0)));
    }

    @Test
    void testGetById() {
        given()
                .when().get("/reservations/1")
                .then()
                .statusCode(200)
                .body("id", is(1));
    }

    @Test
    void testAddReservation() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setName("Test Accommodation");
        accommodation.setLocation("Test Location");
        accommodation.setFilters("pool,wifi");
        accommodation.setMinGuests(1);
        accommodation.setMaxGuests(2);
        accommodation.setPriceType("price-per-unit");
        accommodation.setHostUsername("Host");

        Reservation reservation = new Reservation();
        reservation.setGuestUsername("Test Guest");
        reservation.setFromDate(LocalDate.of(2024, 6, 1));
        reservation.setToDate(LocalDate.of(2024, 6, 10));
        reservation.setAccommodation(accommodation);

        ReservationDto reservationDto = new ReservationDto(reservation);
        reservationDto.setFromDate("15-06-2024");
        reservationDto.setToDate("20-06-2024");

        given()
                .contentType(ContentType.JSON)
                .body(reservationDto)
                .when().post("/reservations")
                .then()
                .statusCode(201)
                .body("guestUsername", is("Test Guest"));
    }

    @Test
    void testDeleteReservation() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setName("Test Accommodation");
        accommodation.setLocation("Test Location");
        accommodation.setFilters("Pool");
        accommodation.setMinGuests(1);
        accommodation.setMaxGuests(2);
        accommodation.setPriceType("Fixed");
        accommodation.setHostUsername("Host");

        Reservation reservation = new Reservation();
        reservation.setGuestUsername("Test Guest");
        reservation.setFromDate(LocalDate.of(2024, 7, 1));
        reservation.setToDate(LocalDate.of(2024, 7, 10));
        reservation.setAccommodation(accommodation);

        ReservationDto reservationDto = new ReservationDto(reservation);
        reservationDto.setFromDate("15-06-2024");
        reservationDto.setToDate("20-06-2024");

        int reservationId = given()
                .contentType(ContentType.JSON)
                .body(reservationDto)
                .when().post("/reservations")
                .then()
                .statusCode(201)
                .extract().path("id");

        given()
                .when().delete("/reservations/" + reservationId)
                .then()
                .statusCode(204);
    }
}

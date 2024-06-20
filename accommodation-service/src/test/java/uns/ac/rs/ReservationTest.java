package uns.ac.rs;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uns.ac.rs.controlller.dto.ReservationDto;
import uns.ac.rs.entity.Accommodation;
import uns.ac.rs.entity.PriceAdjustment;
import uns.ac.rs.entity.PriceAdjustmentDate;
import uns.ac.rs.entity.Reservation;
import uns.ac.rs.repository.AccommodationRepository;
import uns.ac.rs.repository.PriceAdjustmentDateRepository;
import uns.ac.rs.repository.PriceAdjustmentRepository;
import uns.ac.rs.repository.ReservationRepository;
import uns.ac.rs.resources.PostgresResource;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
class ReservationTest {

    @Inject
    ReservationRepository reservationRepository;

    @Inject
    AccommodationRepository accommodationRepository;

    @Inject
    PriceAdjustmentRepository priceAdjustmentRepository;

    @Inject
    PriceAdjustmentDateRepository priceAdjustmentDateRepository;

    private Long accommo1;
    private Long accommo2;
    private Long accommo3;

    private Long reserv1;
    private Long reserv2;
    private Long reserv3;

    private Accommodation testAcc1;



    @BeforeEach
    @Transactional
    public void setup() {
        priceAdjustmentRepository.deleteAll();
        priceAdjustmentDateRepository.deleteAll();
        reservationRepository.deleteAll();
        accommodationRepository.deleteAll();

        // Accommodations
        Accommodation accommodation1 = new Accommodation();
        accommodation1.setName("Mountain Cabin Retreat");
        accommodation1.setLocation("Aspen");
        accommodation1.setFilters("wifi,parking,fireplace,bath");
        accommodation1.setMinGuests(2);
        accommodation1.setMaxGuests(6);
        accommodation1.setPriceType("price_per_person");
        accommodation1.setHostUsername("username2");
        accommodation1.setAutoAcceptReservations(false);
        accommodationRepository.persist(accommodation1);
        accommo1 = accommodation1.getId();
        testAcc1 = accommodation1;


        Accommodation accommodation2 = new Accommodation();
        accommodation2.setName("City Center Studio");
        accommodation2.setLocation("New York");
        accommodation2.setFilters("wifi,parking,tv");
        accommodation2.setMinGuests(1);
        accommodation2.setMaxGuests(2);
        accommodation2.setPriceType("price_per_person");
        accommodation2.setHostUsername("host2");
        accommodation2.setAutoAcceptReservations(false);
        accommodationRepository.persist(accommodation2);
        accommo2 = accommodation2.getId();

        Accommodation accommodation3 = new Accommodation();
        accommodation3.setName("Beachfront Condo");
        accommodation3.setLocation("Malibu");
        accommodation3.setFilters("wifi,parking,pool");
        accommodation3.setMinGuests(1);
        accommodation3.setMaxGuests(4);
        accommodation3.setPriceType("price_per_unit");
        accommodation3.setHostUsername("host2");
        accommodation3.setAutoAcceptReservations(false);
        accommodationRepository.persist(accommodation3);
        accommo3 = accommodation3.getId();

        // Reservations
        Reservation reservation1 = new Reservation();
        reservation1.setAccommodation(accommodation1);
        reservation1.setGuestUsername("guestUser1");
        reservation1.setState(Reservation.ReservationState.APPROVED);
        reservation1.setNumOfGuests(2);
        reservation1.setFromDate(LocalDate.of(2024, 7, 10));
        reservation1.setToDate(LocalDate.of(2024, 7, 14));
        reservation1.setTotalPrice(600.00);
        reservationRepository.persist(reservation1);
        reserv1 = reservation1.getId();

        Reservation reservation2 = new Reservation();
        reservation2.setAccommodation(accommodation2);
        reservation2.setGuestUsername("guestUser2");
        reservation2.setState(Reservation.ReservationState.PENDING);
        reservation2.setNumOfGuests(1);
        reservation2.setFromDate(LocalDate.of(2024, 7, 12));
        reservation2.setToDate(LocalDate.of(2024, 7, 16));
        reservation2.setTotalPrice(400.00);
        reservationRepository.persist(reservation2);
        reserv2 = reservation2.getId();

        Reservation reservation3 = new Reservation();
        reservation3.setAccommodation(accommodation2);
        reservation3.setGuestUsername("guestUser1");
        reservation3.setState(Reservation.ReservationState.APPROVED);
        reservation3.setNumOfGuests(2);
        reservation3.setFromDate(LocalDate.of(2024, 7, 21));
        reservation3.setToDate(LocalDate.of(2024, 7, 22));
        reservation3.setTotalPrice(600.00);
        reservationRepository.persist(reservation3);
        reserv3 = reservation3.getId();

        // Price Adjustment Dates with Reservations
        PriceAdjustmentDate pad1 = new PriceAdjustmentDate(LocalDate.of(2024, 7, 10), 150.00);
        pad1.setReservation(reservation1);
        priceAdjustmentDateRepository.persist(pad1);

        PriceAdjustmentDate pad2 = new PriceAdjustmentDate(LocalDate.of(2024, 7, 11), 150.00);
        pad2.setReservation(reservation1);
        priceAdjustmentDateRepository.persist(pad2);

        PriceAdjustmentDate pad3 = new PriceAdjustmentDate(LocalDate.of(2024, 7, 12), 150.00);
        pad3.setReservation(reservation1);
        priceAdjustmentDateRepository.persist(pad3);

        PriceAdjustmentDate pad4 = new PriceAdjustmentDate(LocalDate.of(2024, 7, 13), 150.00);
        pad4.setReservation(reservation1);
        priceAdjustmentDateRepository.persist(pad4);

        PriceAdjustmentDate pad5 = new PriceAdjustmentDate(LocalDate.of(2024, 7, 14), 150.00);
        pad5.setReservation(reservation1);
        priceAdjustmentDateRepository.persist(pad5);

        PriceAdjustmentDate pad6 = new PriceAdjustmentDate(LocalDate.of(2024, 7, 21), 290.00);
        pad6.setReservation(reservation3);
        priceAdjustmentDateRepository.persist(pad6);

        PriceAdjustmentDate pad7 = new PriceAdjustmentDate(LocalDate.of(2024, 7, 22), 290.00);
        pad7.setReservation(reservation3);
        priceAdjustmentDateRepository.persist(pad7);

        // Price Adjustment Dates without Reservations
        priceAdjustmentDateRepository.persist(new PriceAdjustmentDate(LocalDate.of(2024, 7, 10), 100.00));
        priceAdjustmentDateRepository.persist(new PriceAdjustmentDate(LocalDate.of(2024, 7, 11), 100.00));
        priceAdjustmentDateRepository.persist(new PriceAdjustmentDate(LocalDate.of(2024, 7, 12), 100.00));
        priceAdjustmentDateRepository.persist(new PriceAdjustmentDate(LocalDate.of(2024, 7, 13), 100.00));
        priceAdjustmentDateRepository.persist(new PriceAdjustmentDate(LocalDate.of(2024, 7, 14), 100.00));

        priceAdjustmentDateRepository.persist(new PriceAdjustmentDate(LocalDate.of(2024, 7, 12), 250.00));
        priceAdjustmentDateRepository.persist(new PriceAdjustmentDate(LocalDate.of(2024, 7, 13), 250.00));
        priceAdjustmentDateRepository.persist(new PriceAdjustmentDate(LocalDate.of(2024, 7, 14), 250.00));
        priceAdjustmentDateRepository.persist(new PriceAdjustmentDate(LocalDate.of(2024, 7, 15), 250.00));
        priceAdjustmentDateRepository.persist(new PriceAdjustmentDate(LocalDate.of(2024, 7, 16), 250.00));

        priceAdjustmentDateRepository.persist(new PriceAdjustmentDate(LocalDate.of(2024, 4, 10), 200.00));
        priceAdjustmentDateRepository.persist(new PriceAdjustmentDate(LocalDate.of(2024, 4, 11), 200.00));
        priceAdjustmentDateRepository.persist(new PriceAdjustmentDate(LocalDate.of(2024, 4, 12), 200.00));
        priceAdjustmentDateRepository.persist(new PriceAdjustmentDate(LocalDate.of(2024, 4, 13), 200.00));
        priceAdjustmentDateRepository.persist(new PriceAdjustmentDate(LocalDate.of(2024, 4, 15), 220.00));
        priceAdjustmentDateRepository.persist(new PriceAdjustmentDate(LocalDate.of(2024, 4, 16), 220.00));
        priceAdjustmentDateRepository.persist(new PriceAdjustmentDate(LocalDate.of(2024, 4, 17), 220.00));
        priceAdjustmentDateRepository.persist(new PriceAdjustmentDate(LocalDate.of(2024, 4, 18), 220.00));

        // Price Adjustments
        priceAdjustmentRepository.persist(new PriceAdjustment(accommodation1, pad1));
        priceAdjustmentRepository.persist(new PriceAdjustment(accommodation1, pad2));
        priceAdjustmentRepository.persist(new PriceAdjustment(accommodation1, pad3));
        priceAdjustmentRepository.persist(new PriceAdjustment(accommodation1, pad4));
        priceAdjustmentRepository.persist(new PriceAdjustment(accommodation1, pad5));
        priceAdjustmentRepository.persist(new PriceAdjustment(accommodation2, pad6));
        priceAdjustmentRepository.persist(new PriceAdjustment(accommodation2, pad7));
    }

    @Test
    void testGetAll() {
        given()
                .when().get("/reservations")
                .then()
                .statusCode(200)
                .body("$.size()", is(greaterThanOrEqualTo(0)));
    }

    @Test
    void testAddReservation() {
        Reservation reservation = new Reservation();
        reservation.setGuestUsername("Test Guest");
        reservation.setFromDate(LocalDate.of(2024, 6, 1));
        reservation.setToDate(LocalDate.of(2024, 6, 10));
        reservation.setAccommodation(testAcc1);

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
        Reservation reservation = new Reservation();
        reservation.setGuestUsername("Test Guest");
        reservation.setFromDate(LocalDate.of(2024, 7, 1));
        reservation.setToDate(LocalDate.of(2024, 7, 10));
        reservation.setAccommodation(testAcc1);

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

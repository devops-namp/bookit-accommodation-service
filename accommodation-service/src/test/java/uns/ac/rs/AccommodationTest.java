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
import uns.ac.rs.controlller.request.AdjustPriceRequest;
import uns.ac.rs.controlller.request.RemovePriceRequest;
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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
class AccommodationTest {
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

    private PriceAdjustment priceAdjustmentTest1;



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

        PriceAdjustmentDate pad8 = new PriceAdjustmentDate(LocalDate.of(2024, 7, 11), 150.00);
        priceAdjustmentDateRepository.persist(pad8);

        PriceAdjustmentDate pad9 = new PriceAdjustmentDate(LocalDate.of(2024, 7, 12), 150.00);
        priceAdjustmentDateRepository.persist(pad9);

        PriceAdjustmentDate pad10 = new PriceAdjustmentDate(LocalDate.of(2024, 4, 15), 150.00);
        priceAdjustmentDateRepository.persist(pad10);

        PriceAdjustmentDate pad11 = new PriceAdjustmentDate(LocalDate.of(2024, 4, 16), 150.00);
        priceAdjustmentDateRepository.persist(pad11);

        PriceAdjustmentDate pad12 = new PriceAdjustmentDate(LocalDate.of(2024, 7, 5), 150.00);
        priceAdjustmentDateRepository.persist(pad12);

        PriceAdjustmentDate pad13 = new PriceAdjustmentDate(LocalDate.of(2024, 7, 6), 150.00);
        priceAdjustmentDateRepository.persist(pad13);

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
        PriceAdjustment priceAdjustment = new PriceAdjustment(accommodation1, pad1);
        priceAdjustmentRepository.persist(priceAdjustment);
        priceAdjustmentTest1 = priceAdjustment;

        priceAdjustmentRepository.persist(new PriceAdjustment(accommodation1, pad2));
        priceAdjustmentRepository.persist(new PriceAdjustment(accommodation1, pad3));
        priceAdjustmentRepository.persist(new PriceAdjustment(accommodation1, pad4));
        priceAdjustmentRepository.persist(new PriceAdjustment(accommodation1, pad5));
        priceAdjustmentRepository.persist(new PriceAdjustment(accommodation2, pad6));
        priceAdjustmentRepository.persist(new PriceAdjustment(accommodation2, pad7));
        priceAdjustmentRepository.persist(new PriceAdjustment(accommodation2, pad8));
        priceAdjustmentRepository.persist(new PriceAdjustment(accommodation2, pad9));
        priceAdjustmentRepository.persist(new PriceAdjustment(accommodation2, pad10));
        priceAdjustmentRepository.persist(new PriceAdjustment(accommodation2, pad11));
        priceAdjustmentRepository.persist(new PriceAdjustment(accommodation2, pad12));
        priceAdjustmentRepository.persist(new PriceAdjustment(accommodation2, pad13));
    }


    @Test
    void testGetAll() {
        given()
                .when().get("/accommodation")
                .then()
                .statusCode(200)
                .body("$.size()", is(greaterThanOrEqualTo(0)));
    }

    @Test
    void testGetById() {
        given()
                .when().get("/accommodation/"+accommo1)
                .then()
                .statusCode(200)
                .body("id", is(accommo1.intValue()));
    }

    @Test
    @TestSecurity(user = "host", roles = "HOST")
    void testAddAccommodation() {
        Accommodation accommodation = new Accommodation();
        accommodation.setImages(List.of());
        accommodation.setName("Test Accommodation");
        accommodation.setLocation("Test Location");

        given()
                .contentType(ContentType.JSON)
                .body(accommodation)
                .when().post("/accommodation")
                .then()
                .statusCode(201)
                .body("name", is("Test Accommodation"));
    }

    @Test
    @TestSecurity(user = "username2", roles = "HOST")
    void testUpdateAccommodation() {
        Accommodation updatedAccommodation = new Accommodation();
        updatedAccommodation.setName("Updated Accommodation");

        given()
                .contentType(ContentType.JSON)
                .body(updatedAccommodation)
                .when().put("/accommodation/"+accommo1)
                .then()
                .statusCode(200)
                .body("name", is("Updated Accommodation"));
    }

    @Test
    @TestSecurity(user = "host", roles = "HOST")
    void testDeleteAccommodation() {
        Accommodation accommodation = new Accommodation();
        accommodation.setImages(List.of());
        accommodation.setName("Test Accommodation");
        accommodation.setLocation("Test Location");

        given()
            .contentType(ContentType.JSON)
            .body(accommodation)
            .when().post("/accommodation")
            .then()
            .statusCode(201)
            .body("name", is("Test Accommodation"));

        given()
                .when().delete("/accommodation/"+accommo2)
                .then()
                .statusCode(204);
    }

    @Test
    @TestSecurity(user = "host", roles = "HOST")
    void testAdjustPrices() {
        var request = new AdjustPriceRequest();

        var price1 = new AdjustPriceRequest.IntervalPrice(
            LocalDate.of(2024, Month.APRIL, 15),
            LocalDate.of(2024, Month.MAY, 30),
            500
        );
        var price2 = new AdjustPriceRequest.IntervalPrice(
            LocalDate.of(2024, Month.APRIL, 17),
            LocalDate.of(2024, Month.APRIL, 25),
            600
        );
        var price3 = new AdjustPriceRequest.IntervalPrice(
            LocalDate.of(2024, Month.APRIL, 25),
            LocalDate.of(2024, Month.APRIL, 30),
            700
        );
        var price4 = new AdjustPriceRequest.IntervalPrice(
            LocalDate.of(2024, Month.JULY, 15),
            LocalDate.of(2024, Month.JULY, 30),
            100
        );

        request.setPricesPerInterval(List.of(price1, price2, price3, price4));

        var response = given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().put("/accommodation/price/" + accommo1)
            .then()
            .statusCode(200)
            .body("id", CoreMatchers.is(accommo1.intValue()))
            .extract()
            .response();

        List<Object> priceAdjustments = response.jsonPath().getList("priceAdjustments");
        assertThat(priceAdjustments, hasSize(67));

        List<String> datesStr = response.jsonPath().getList("priceAdjustments.priceAdjustmentDate.date");
        var localDates = datesStr.stream().map(LocalDate::parse).toList();
        var sortedDates = localDates.stream().sorted().toList();
        assertThat(localDates, is(sortedDates));

        var specificDate = LocalDate.of(2024, Month.APRIL, 15);
        int index = localDates.indexOf(specificDate);
        assert index != -1;
        double price = response.jsonPath().getDouble("priceAdjustments[" + index + "].priceAdjustmentDate.price");
        assertThat(price, is(500.0));

        specificDate = LocalDate.of(2024, Month.APRIL, 18);
        index = localDates.indexOf(specificDate);
        assert index != -1;
        price = response.jsonPath().getDouble("priceAdjustments[" + index + "].priceAdjustmentDate.price");
        assertThat(price, is(600.0));

        specificDate = LocalDate.of(2024, Month.APRIL, 25);
        index = localDates.indexOf(specificDate);
        assert index != -1;
        price = response.jsonPath().getDouble("priceAdjustments[" + index + "].priceAdjustmentDate.price");
        assertThat(price, is(700.0));

        specificDate = LocalDate.of(2024, Month.JULY, 30);
        index = localDates.indexOf(specificDate);
        assert index != -1;
        price = response.jsonPath().getDouble("priceAdjustments[" + index + "].priceAdjustmentDate.price");
        assertThat(price, is(100.0));
    }

    @Test
    @TestSecurity(user = "host", roles = "HOST")
    void testRemovePrices() {
        var request = new RemovePriceRequest();

        var interval1 = new RemovePriceRequest.IntervalPrice(
            LocalDate.of(2024, Month.APRIL, 12),
            LocalDate.of(2024, Month.APRIL, 17)
        );
        var interval2 = new RemovePriceRequest.IntervalPrice(
            LocalDate.of(2024, Month.APRIL, 15),
            LocalDate.of(2024, Month.APRIL, 18)
        );
        var interval3 = new RemovePriceRequest.IntervalPrice(
            LocalDate.of(2024, Month.JULY, 5),
            LocalDate.of(2024, Month.JULY, 10)
        );

        request.setPricesPerInterval(List.of(interval1, interval2, interval3));

        var response = given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().delete("/accommodation/price/" + accommo2)
            .then()
            .statusCode(200)
            .body("id", CoreMatchers.is(accommo2.intValue()))
            .extract()
            .response();

        List<Object> priceAdjustments = response.jsonPath().getList("priceAdjustments");
        assertThat(priceAdjustments, hasSize(4));

        List<String> datesStr = response.jsonPath().getList("priceAdjustments.priceAdjustmentDate.date");
        var localDates = datesStr.stream().map(LocalDate::parse).toList();
        var sortedDates = localDates.stream().sorted().toList();
        assertThat(localDates, is(sortedDates));

        var specificDate = LocalDate.of(2024, Month.JULY, 11);
        int index = localDates.indexOf(specificDate);
        assert index != -1;

        specificDate = LocalDate.of(2024, Month.JULY, 12);
        index = localDates.indexOf(specificDate);
        assert index != -1;

        specificDate = LocalDate.of(2024, Month.JULY, 21);
        index = localDates.indexOf(specificDate);
        assert index != -1;

        specificDate = LocalDate.of(2024, Month.JULY, 22);
        index = localDates.indexOf(specificDate);
        assert index != -1;
    }

    @Test
    @TestSecurity(user = "host", roles = "HOST")
    void testRemovePrices_reservationExists() {
        var request = new RemovePriceRequest();

        var interval1 = new RemovePriceRequest.IntervalPrice(
            LocalDate.of(2024, Month.APRIL, 12),
            LocalDate.of(2024, Month.APRIL, 17)
        );
        var interval2 = new RemovePriceRequest.IntervalPrice(
            LocalDate.of(2024, Month.APRIL, 15),
            LocalDate.of(2024, Month.APRIL, 18)
        );
        var interval3 = new RemovePriceRequest.IntervalPrice(
            LocalDate.of(2024, Month.JULY, 18),
            LocalDate.of(2024, Month.JULY, 21)
        );

        request.setPricesPerInterval(List.of(interval1, interval2, interval3));

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().delete("/accommodation/price/" + accommo2)
            .then()
            .statusCode(400)
            .body(containsString("Unable to update availability because reservation exists in chosen interval"));
    }

    @Test
    void testSearch() {
        given()
                .queryParam("name", "Ocean View")
                .queryParam("location", "Miami Beach")
                .queryParam("filters", "wifi,parking,kitchen")
                .queryParam("numGuests", 2)
                .queryParam("fromDate", "2024-06-01")
                .queryParam("toDate", "2024-06-30")
                .queryParam("fromPrice", 150.00)
                .queryParam("toPrice", 300.00)
                .when().get("/accommodation/search")
                .then()
                .statusCode(200)
                .body("$.size()", is(greaterThanOrEqualTo(0)));
    }

    @Test
    void testSearchWithSpecificFilters() {
        given()
                .queryParam("filters", "wifi,parking,fireplace,bath")
                .queryParam("fromDate", "2024-07-10")
                .queryParam("toDate", "2024-07-14")
                .queryParam("fromPrice", 150.00)
                .queryParam("toPrice", 300.00)
                .when().get("/accommodation/search")
                .then()
                .statusCode(200)
                .body("$.size()", is(greaterThanOrEqualTo(0)));
    }

    @Test
    void testSearchWithSpecificNumberOfGuests() {
        given()
                .queryParam("numGuests", 1)
                .queryParam("fromDate", "2024-07-10")
                .queryParam("toDate", "2024-07-14")
                .queryParam("fromPrice", 50.00)
                .queryParam("toPrice", 400.00)
                .when().get("/accommodation/search")
                .then()
                .statusCode(200)
                .body("$.size()", is(greaterThanOrEqualTo(0)));
    }

    @Test
    void testSearchWithMissingDateRange() {
        given()
                .queryParam("fromPrice", 150.00)
                .queryParam("toPrice", 300.00)
                .when().get("/accommodation/search")
                .then()
                .statusCode(400);
    }

    @Test
    void testSearchWithMissingPriceRange() {
        given()
                .queryParam("fromDate", "2024-07-10")
                .queryParam("toDate", "2024-07-14")
                .when().get("/accommodation/search")
                .then()
                .statusCode(400);
    }
}

package uns.ac.rs;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import uns.ac.rs.controlller.request.AdjustPriceRequest;
import uns.ac.rs.entity.Accommodation;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
class AccommodationTest {

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
                .when().get("/accommodation/1")
                .then()
                .statusCode(200)
                .body("id", is(1));
    }

    @Test
    void testAddAccommodation() {
        Accommodation accommodation = new Accommodation();
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
    void testUpdateAccommodation() {
        Accommodation updatedAccommodation = new Accommodation();
        updatedAccommodation.setName("Updated Accommodation");

        given()
                .contentType(ContentType.JSON)
                .body(updatedAccommodation)
                .when().put("/accommodation/1")
                .then()
                .statusCode(200)
                .body("name", is("Updated Accommodation"));
    }

    @Test
    void testDeleteAccommodation() {
        Accommodation accommodation = new Accommodation();
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
                .when().delete("/accommodation/2")
                .then()
                .statusCode(204);
    }

    @Test
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
            .when().put("/accommodation/price/1")
            .then()
            .statusCode(200)
            .body("id", CoreMatchers.is(1))
            .extract()
            .response();

        List<Object> priceAdjustments = response.jsonPath().getList("priceAdjustments");
        assertThat(priceAdjustments, hasSize(62));

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
    void testSearch() {
        given()
                .queryParam("name", "Ocean View")
                .queryParam("location", "Miami Beach, FL")
                .queryParam("filters", "wifi,free parking,kitchen")
                .queryParam("minGuests", 1)
                .queryParam("maxGuests", 4)
                .queryParam("fromDate", "2024-06-01")
                .queryParam("toDate", "2024-06-30")
                .queryParam("price", 150.00)
                .queryParam("priceType", "price per unit")
                .when().get("/accommodation/search")
                .then()
                .statusCode(200)
                .body("$.size()", is(greaterThanOrEqualTo(0)));
    }
}

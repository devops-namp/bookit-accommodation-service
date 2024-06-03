package uns.ac.rs;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import uns.ac.rs.entity.Accommodation;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

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
        given()
                .when().delete("/accommodation/1")
                .then()
                .statusCode(204);
    }
}

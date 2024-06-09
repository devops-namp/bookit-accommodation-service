package uns.ac.rs.controlller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import uns.ac.rs.entity.Accommodation;
import uns.ac.rs.service.AccommodationService;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;


import org.eclipse.microprofile.reactive.messaging.Incoming;
import io.vertx.core.json.JsonObject;

@Path("/accommodation")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccommodationController {

    @Inject
    AccommodationService accommodationService;

    @Inject
    @Channel("filter-request-queue")
    Emitter<String> stringEmitter;


//    @Incoming("test-queue")
//    public void consume(JsonObject json) {
//        Book book = json.mapTo(Book.class);
//        System.out.println("Received book: " + book.title + " by " + book.author);
//    }
    @GET
    public List<Accommodation> getAll() {
        System.out.println("Dobavi mi sve korisnike");
        stringEmitter.send("dobavi");
        return accommodationService.getAll();
    }

    @Incoming("filter-response-queue")
    public void consume(JsonObject json) {
        Book book = json.mapTo(Book.class);
        System.out.println("Primljena knjiga " + book.title + " by " + book.author);
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Optional<Accommodation> accommodation = accommodationService.getById(id);
        return accommodation.map(value -> Response.ok(value).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    public Response addAccommodation(Accommodation accommodation) {
        accommodationService.addAccommodation(accommodation);
        return Response.status(Response.Status.CREATED).entity(accommodation).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateAccommodation(@PathParam("id") Long id, Accommodation accommodation) {
        accommodationService.updateAccommodation(id, accommodation);
        return Response.ok(accommodation).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteAccommodation(@PathParam("id") Long id) {
        accommodationService.deleteAccommodation(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    
}

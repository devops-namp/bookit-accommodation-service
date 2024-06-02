package uns.ac.rs.controlller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uns.ac.rs.entity.Accommodation;
import uns.ac.rs.service.AccommodationService;

import java.util.List;
import java.util.Optional;

@Path("/accommodation")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccommodationController {

    @Inject
    AccommodationService accommodationService;

    @GET
    @Path("/plain")
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return "accommodation";
    }

    @GET
    public List<Accommodation> getAll() {
        return accommodationService.getAll();
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

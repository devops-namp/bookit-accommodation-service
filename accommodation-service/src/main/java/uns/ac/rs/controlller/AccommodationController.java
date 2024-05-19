package uns.ac.rs.controlller;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import uns.ac.rs.service.AccommodationService;

@Path("/accommodation")
public class AccommodationController {

    @Inject
    AccommodationService accommodationService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return "accommodation";
    }
}

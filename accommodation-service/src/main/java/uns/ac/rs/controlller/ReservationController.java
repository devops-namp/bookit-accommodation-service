package uns.ac.rs.controlller;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uns.ac.rs.controlller.dto.AccommodationDto;
import uns.ac.rs.controlller.dto.ReservationDto;
import uns.ac.rs.controlller.dto.ReservationDtoToSend;
import uns.ac.rs.entity.Reservation;
import uns.ac.rs.service.AccommodationService;
import uns.ac.rs.service.ReservationService;

import java.util.List;
import java.util.Optional;

@Path("/reservations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReservationController {

    @Inject
    ReservationService reservationService;
    @Inject
    AccommodationService accommodationService;

    @GET
    public List<ReservationDtoToSend> listAll() {
        return reservationService.listAll();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Long id) {
        Optional<ReservationDtoToSend> reservation = reservationService.findById(id);
        return reservation.map(value -> Response.ok(value).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    public Response create(ReservationDto reservationDto) {
        Reservation created = reservationService.create(reservationDto);
        AccommodationDto accommodationDto = new AccommodationDto(accommodationService.getById(Long.valueOf(reservationDto.getAccommodationId())).orElseGet(null));
        ReservationDtoToSend reservationDtoToSend = new ReservationDtoToSend(created,reservationDto.getFromDate(), reservationDto.getToDate(), reservationDto.getNumOfGusts(), reservationDto.getTotalPrice(),accommodationDto);
        return Response.status(Response.Status.CREATED).entity(reservationDtoToSend).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, Reservation reservation) {
        Optional<Reservation> updated = reservationService.update(id, reservation);
        return updated.map(value -> Response.ok(value).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = reservationService.delete(id);
        if (deleted) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    @GET
    @Path("getByHost/{username}")
    @PermitAll
    public Response getByHost(@PathParam("username") String username) {
        System.out.println("USLI SMO U TRAZENJE VLASNIKOVIH REZERVACIJA");
        List<ReservationDtoToSend> retVal = reservationService.getByHost(username);
        return Response.ok(retVal).build();
    }

    @GET
    @Path("getByGuest/{username}")
    @PermitAll
    public Response getByGuest(@PathParam("username") String username) {
        System.out.println("USLI SMO U TRAZENJE GOSTOVIH REZERVACIJA");
        List<ReservationDtoToSend> retVal = reservationService.getByGuest(username);
        return Response.ok(retVal).build();
    }

    @POST
    @Path("/approve/{reservationId}")
    @PermitAll
    public Response approveReservation(@PathParam("id") Long reservationId) {
        System.out.println("USLI SMO U ODOBRAVANJE REZERVACIJE");
        reservationService.approve(reservationId);
        return Response.ok().build();
    }

    @POST
    @Path("/reject/{reservationId}")
    @PermitAll
    public Response rejectReservation(@PathParam("id") Long reservationId) {
        System.out.println("USLI SMO U ODBIJANJE REZERVACIJE");
        reservationService.reject(reservationId);
        return Response.ok().build();
    }

}
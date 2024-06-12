package uns.ac.rs.controlller;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import uns.ac.rs.controlller.dto.AccommodationDto;
import uns.ac.rs.controlller.dto.AccommodationWithPrice;
import uns.ac.rs.entity.Accommodation;
import uns.ac.rs.controlller.request.AdjustPriceRequest;
import uns.ac.rs.service.AccommodationService;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.*;

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


    @GET
    @PermitAll
    public List<AccommodationDto> getAll() {
        System.out.println("Dobavi mi sve korisnike");
        stringEmitter.send("dobavi");
        return accommodationService.getAll().stream().map(AccommodationDto::new).toList();
    }

    @Incoming("filter-response-queue")
    public void consume(JsonObject json) {
        Book book = json.mapTo(Book.class);
        System.out.println("Primljena knjiga " + book.title + " by " + book.author);
    }

    @GET
    @Path("/{id}")
    @PermitAll
    public Response getById(@PathParam("id") Long id) {
        Optional<Accommodation> accommodation = accommodationService.getById(id);
        return accommodation.map(value -> Response.ok(new AccommodationDto(value)).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @RolesAllowed({"HOST" })
    public Response addAccommodation(Accommodation accommodation) {
        accommodationService.addAccommodation(accommodation);
        return Response.status(Response.Status.CREATED).entity(new AccommodationDto(accommodation)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({ "HOST" })
    public Response updateAccommodation(@PathParam("id") Long id, Accommodation accommodation) {
        accommodationService.updateAccommodation(id, accommodation);
        return Response.ok(new AccommodationDto(accommodation)).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({ "HOST" })
    public Response deleteAccommodation(@PathParam("id") Long id) {
        accommodationService.deleteAccommodation(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @PUT
    @Path("/price/{id}")
    @RolesAllowed({ "HOST" })
    public Response adjustPrices(@PathParam("id") Long id, AdjustPriceRequest request) {
        Map<LocalDate, Double> newPricesMap = new HashMap<>();

        request.getPricesPerInterval().forEach(intervalPrice -> {
            var startDate = intervalPrice.getStartDate();
            var endDate = intervalPrice.getEndDate();
            var price = intervalPrice.getPrice();

            startDate.datesUntil(endDate.plusDays(1)).forEach(date ->
                newPricesMap.put(date, price)
            );
        });

        var accommodation = accommodationService.adjustPrices(id, newPricesMap);
        if (accommodation == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(new AccommodationDto(accommodation)).build();
    }
    @GET
    @Path("/search")
    @PermitAll
    public List<AccommodationWithPrice> searchAccommodations(@QueryParam("name") String name,
                                                             @QueryParam("location") String location,
                                                             @QueryParam("filters") List<String> filters,
                                                             @QueryParam("numGuests") Integer numGuests,
                                                             @QueryParam("fromDate") String fromDateStr,
                                                             @QueryParam("toDate") String toDateStr,
                                                             @QueryParam("toPrice") Double toPrice,
                                                             @QueryParam("fromPrice") Double fromPrice,
                                                             @QueryParam("priceType") String priceType) {
        LocalDate fromDate = fromDateStr != null ? LocalDate.parse(fromDateStr) : null;
        LocalDate toDate = toDateStr != null ? LocalDate.parse(toDateStr) : null;
        System.out.println("USAOOOO");
        System.out.println("Parameters:");
        System.out.println("name: " + name);
        System.out.println("location: " + location);
        System.out.println("numGuests: " + numGuests);
        System.out.println("fromDate: " + fromDate);
        System.out.println("toDate: " + toDate);
        System.out.println("toPrice: " + toPrice);
        System.out.println("fromPrice: " + fromPrice);
        System.out.println("priceType: " + priceType);
        for(String filter : filters)
            System.out.println("filter: " + filter);
        return accommodationService.searchAccommodations(name, location, filters, numGuests, fromDate, toDate, fromPrice,toPrice, priceType);
    }
}

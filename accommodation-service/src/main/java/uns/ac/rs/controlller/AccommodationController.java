package uns.ac.rs.controlller;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.jboss.resteasy.reactive.ResponseStatus;
import uns.ac.rs.controlller.dto.AccommodationDto;
import uns.ac.rs.controlller.dto.AccommodationWithPrice;
import uns.ac.rs.controlller.dto.DateInfoDto;
import uns.ac.rs.controlller.dto.ImageDto;
import uns.ac.rs.controlller.request.RemovePriceRequest;
import uns.ac.rs.entity.Accommodation;
import uns.ac.rs.controlller.request.AdjustPriceRequest;
import uns.ac.rs.entity.events.AutoApproveEvent;
import uns.ac.rs.service.AccommodationService;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;

import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;


import org.eclipse.microprofile.reactive.messaging.Incoming;
import io.vertx.core.json.JsonObject;
import uns.ac.rs.service.ReservationService;

@Path("/accommodation")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccommodationController {

    @Inject
    AccommodationService accommodationService;

    @Inject
    SecurityIdentity securityIdentity;

    Logger LOG = Logger.getLogger(String.valueOf(AccommodationController.class));


    @GET
    @PermitAll
    public List<AccommodationDto> getAll() {
        return accommodationService.getAll().stream().map(AccommodationDto::new).toList();
    }

    @Incoming("filter-response-queue")
    public void consume(JsonObject json) {
        Book book = json.mapTo(Book.class);
        System.out.println("Primljena knjiga " + book.title + " by " + book.author);
    }


    @Incoming("autoapprove-user-to-acc-queue")
    public void setAutoapprove(JsonObject json) {
        AutoApproveEvent event = json.mapTo(AutoApproveEvent.class);
        if (event.getType().equals(AutoApproveEvent.AutoApproveEventType.GET_BY_USER)) {
            accommodationService.setAutoApprove(event);
        } else if (event.getType().equals(AutoApproveEvent.AutoApproveEventType.CHANGE)) {
            accommodationService.changeAutoapproveInAccommodations(event);
        }
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
    @RolesAllowed({"HOST"})
    public Response addAccommodation(AccommodationDto accommodationDto) {
        LOG.info("Adding accommodation");
        String username = securityIdentity.getPrincipal().getName();
        LOG.info("Adding accommodation for host: " + username);
        Accommodation accommodation = new Accommodation(accommodationDto);
        accommodation.setHostUsername(username);
        accommodation = accommodationService.addAccommodation(accommodation);
        LOG.info("Added accommodation with ID: " + accommodation.getId());
        accommodationDto.setId(accommodation.getId());
        accommodationService.getAutoApprove(accommodation.getId(), username);
        return Response.status(Response.Status.CREATED).entity(accommodationDto).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({ "HOST" })
    public Response updateAccommodation(@PathParam("id") Long id, AccommodationDto accommodation) {
        String username = securityIdentity.getPrincipal().getName();
        LOG.info("Updating accommodation: " + id.toString() + " for host: " + username);
        var accommodationResult = accommodationService.updateAccommodation(id, accommodation, username);
        LOG.info("Updated accommodation");
        return Response.ok(new AccommodationDto(accommodationResult)).build();
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
        System.out.println("Adding prices for accommodation: " + id);

        request.getPricesPerInterval().forEach(intervalPrice -> {

            var startDate = intervalPrice.getStartDate();
            var endDate = intervalPrice.getEndDate();
            var price = intervalPrice.getPrice();

            startDate.datesUntil(endDate.plusDays(1)).forEach(date ->
                newPricesMap.put(date, price)
            );
            System.out.println("Added price for interval: " + startDate + " - " + endDate + " : " + price);
        });

        var accommodation = accommodationService.adjustPrices(id, newPricesMap);
        return Response.ok(new AccommodationDto(accommodation)).build();
    }

    @DELETE
    @Path("/price/{id}")
    @RolesAllowed({ "HOST" })
    public Response removePrices(@PathParam("id") Long id, RemovePriceRequest request) {
        Set<LocalDate> newPrices = new HashSet<>();
        System.out.println("Removing prices for accommodation: " + id);

        request.getPricesPerInterval().forEach(intervalPrice -> {
            var startDate = intervalPrice.getStartDate();
            var endDate = intervalPrice.getEndDate();
            startDate.datesUntil(endDate.plusDays(1)).forEach(newPrices::add);
            System.out.println("Removed price for interval: " + startDate + " - " + endDate);
        });

        var accommodation = accommodationService.removePrices(id, newPrices);
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

        // dodato zbog testova
        if (fromDateStr == null || toDateStr == null) {
            throw new WebApplicationException("Both fromDate and toDate must be provided", 400);
        }

        if (fromPrice == null || toPrice == null) {
            throw new WebApplicationException("Both fromPrice and toPrice must be provided", 400);
        }


        System.out.println("Parameters:");
        System.out.println("name: " + name);
        System.out.println("location: " + location);
        System.out.println("numGuests: " + numGuests);
        System.out.println("fromDate: " + fromDate);
        System.out.println("toDate: " + toDate);
        System.out.println("toPrice: " + toPrice);
        System.out.println("fromPrice: " + fromPrice);
        System.out.println("priceType: " + priceType);

        List<String> correctFilters = new ArrayList<>();
        if (filters != null && !filters.isEmpty()) {
            String firstFilter = filters.get(0);
            if (firstFilter.contains(",")) {
                correctFilters = Arrays.asList(firstFilter.split(","));
            } else {
                correctFilters.add(firstFilter);
            }
        }

        System.out.println("filtersssss: " + correctFilters);
        return accommodationService.searchAccommodations(name, location, correctFilters, numGuests, fromDate, toDate, fromPrice,toPrice, priceType);
    }


    @GET
    @Path("/dates/{id}")
    @PermitAll
    public List<DateInfoDto> getDatesInfo(@PathParam("id") Long id, @QueryParam("month") Integer month, @QueryParam("year") Integer year) {
        return accommodationService.getMonthInformation(id, month, year);
    }
}

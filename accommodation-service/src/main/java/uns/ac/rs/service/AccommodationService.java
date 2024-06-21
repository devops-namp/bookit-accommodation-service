package uns.ac.rs.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import uns.ac.rs.controlller.dto.AccommodationDto;
import uns.ac.rs.controlller.dto.AccommodationWithPrice;
import uns.ac.rs.controlller.dto.DateInfoDto;
import uns.ac.rs.entity.*;
import uns.ac.rs.entity.events.AutoApproveEvent;
import uns.ac.rs.entity.events.NotificationEvent;
import uns.ac.rs.entity.events.NotificationType;
import uns.ac.rs.exceptions.AccommodationNotFoundException;
import uns.ac.rs.exceptions.ReservationExistsOnDateException;
import uns.ac.rs.repository.*;

import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static io.quarkus.hibernate.orm.panache.PanacheEntityBase.persist;

@ApplicationScoped
public class AccommodationService {

    Logger LOG = Logger.getLogger(String.valueOf(AccommodationService.class));

    @Inject
    AccommodationRepository accommodationRepository;

    @Inject
    PriceAdjustmentRepository priceAdjustmentRepository;

    @Inject
    PriceAdjustmentDateRepository priceAdjustmentDateRepository;

    @Inject
    ImageRepository imageRepository;

    @Inject
    ReservationRepository reservationRepository;

    @Inject
    ReservationService reservationService;

    @Inject
    @Channel("autoapprove-acc-to-user-queue")
    Emitter<AutoApproveEvent> autoApproveEmmiter;

    @Transactional
    public List<Accommodation> getAll() {
        return accommodationRepository.listAll();
    }

    @Transactional
    public Optional<Accommodation> getById(Long id) {
        return accommodationRepository.findByIdOptional(id);
    }

    @Transactional
    public Accommodation addAccommodation(Accommodation accommodation) {
        accommodation.setDeleted(false);
        accommodationRepository.persist(accommodation);
        System.out.println("Assigned ID: " + accommodation.getId());
        return accommodation;
    }

    @Transactional
    public Accommodation updateAccommodation(Long id, AccommodationDto updatedAccommodation, String username) {
        Accommodation existingAccommodation = accommodationRepository.findByIdOptional(id).orElse(null);
        if (existingAccommodation == null || !existingAccommodation.getHostUsername().equals(username)) {
            return null;
        }
        updateAccommodationDetails(existingAccommodation, updatedAccommodation);
        replaceAccommodationImages(existingAccommodation, updatedAccommodation);
        accommodationRepository.persist(existingAccommodation);
        return accommodationRepository.findById(id);
    }

    private void updateAccommodationDetails(Accommodation accommodation, AccommodationDto dto) {
        accommodation.setName(dto.getName());
        accommodation.setLocation(dto.getLocation());
        accommodation.setFilters(dto.getFilters());
        accommodation.setMinGuests(dto.getMinGuests());
        accommodation.setMaxGuests(dto.getMaxGuests());
        accommodation.setPriceType(dto.getPriceType());
    }

    private void replaceAccommodationImages(Accommodation accommodation, AccommodationDto dto) {
        if (accommodation.getImages() != null) {
            accommodation.getImages().forEach(imageRepository::delete);
        }

        List<Image> images = Optional.ofNullable(dto.getImages())
                .orElseGet(ArrayList::new)
                .stream()
                .map(imageDto -> {
                    Image image = new Image();
                    image.setImageData(imageDto.getBase64Image());
                    image.setAccommodation(accommodation);
                    imageRepository.persist(image);
                    return image;
                })
                .collect(Collectors.toList());
        accommodation.setImages(images);
    }


    @Transactional
    public void deleteAccommodation(Long id) {
        accommodationRepository.findByIdOptional(id).ifPresent(accommodationRepository::delete);
    }

    @Transactional
    public void deleteAccommodation(String hostUsername) {
        accommodationRepository.deleteByHost(hostUsername);
        reservationRepository.deleteByHost(hostUsername);
    }

    @Transactional
    public Accommodation adjustPrices(Long id, Map<LocalDate, Double> newPrices) {
        var accommodation = accommodationRepository.findByIdOptional(id).orElseThrow(AccommodationNotFoundException::new);

        for (var entry : newPrices.entrySet()) {
            if (reservationRepository.exists(id, entry.getKey())) {
                throw new ReservationExistsOnDateException();
            }
        }

        var priceAdjustments = accommodation.getPriceAdjustments();
        var existingPrices = new HashMap<LocalDate, PriceAdjustment>();
        priceAdjustments.forEach(pa -> existingPrices.put(pa.getPriceAdjustmentDate().getDate(), pa));

        priceAdjustments.clear();

        for (var entry : newPrices.entrySet()) {
            var date = entry.getKey();
            var price = entry.getValue();
            if (existingPrices.containsKey(date)) {
                var priceAdjustmentDate = existingPrices.get(date).getPriceAdjustmentDate();
                priceAdjustmentDate.setPrice(price);
                priceAdjustmentDateRepository.persist(priceAdjustmentDate);
            } else {
                var priceAdjustment = new PriceAdjustment();
                priceAdjustment.setAccommodation(accommodation);
                var priceAdjustmentDate = new PriceAdjustmentDate(date, price);
                priceAdjustment.setPriceAdjustmentDate(priceAdjustmentDate);
                priceAdjustmentRepository.persist(priceAdjustment);
                priceAdjustmentDate.setPriceAdjustment(priceAdjustment);
                priceAdjustmentDateRepository.persist(priceAdjustmentDate);
                existingPrices.put(date, priceAdjustment);
            }
        }

        priceAdjustments = new ArrayList<>(existingPrices.values());
        priceAdjustments.sort(Comparator.comparing(obj -> obj.getPriceAdjustmentDate().getDate()));

        accommodation.setPriceAdjustments(priceAdjustments);
        accommodationRepository.persist(accommodation);
        return accommodation;
    }

    @Transactional
    public Accommodation removePrices(Long id, Set<LocalDate> toRemove) {
        var accommodation = accommodationRepository.findByIdOptional(id).orElseThrow(AccommodationNotFoundException::new);

        for (var date : toRemove) {
            if (reservationRepository.exists(id, date)) {
                throw new ReservationExistsOnDateException();
            }
        }

        var existing = new HashMap<LocalDate, PriceAdjustment>();
        var toKeep = new ArrayList<PriceAdjustment>();

        accommodation.getPriceAdjustments().forEach(pa -> {
            existing.put(pa.getPriceAdjustmentDate().getDate(), pa);
            if (!toRemove.contains(pa.getPriceAdjustmentDate().getDate())) {
                toKeep.add(pa);
            }
        });

        toRemove.forEach(date -> {
            if (existing.containsKey(date)) {
                priceAdjustmentRepository.deleteById(existing.get(date).getId());
            }
        });

        toKeep.sort(Comparator.comparing(obj -> obj.getPriceAdjustmentDate().getDate()));
        accommodation.setPriceAdjustments(toKeep);
        accommodationRepository.persist(accommodation);
        return accommodation;
    }

    @Transactional
    public List<AccommodationWithPrice> searchAccommodations(String name, String location, List<String> filters, Integer numGuests
                                                        , LocalDate fromDate, LocalDate toDate, Double fromPrice, Double toPrice,
                                                       String priceType) {
        List<AccommodationWithPrice> accommodationDtos = new ArrayList<>();
        for(AccommodationWithPrice accommodationWithPrice : accommodationRepository.search(name, location, filters, numGuests, fromDate, toDate, fromPrice,toPrice, priceType)){
            accommodationDtos.add(accommodationWithPrice);
        }
        return accommodationDtos;
    }

    @Transactional
    public List<DateInfoDto> getMonthInformation(Long id, Integer month, Integer year) {
        LOG.info("Getting month information for accommodation with id: " + id + " for month: " + month + " and year: " + year);
        var accommodation = accommodationRepository.findByIdOptional(id).orElseThrow();
        return accommodationRepository.getMonthInformation(accommodation, month, year);
    }

    public void getAutoApprove(Long accommodationId, String username) {
        AutoApproveEvent event = new AutoApproveEvent(username, accommodationId, AutoApproveEvent.AutoApproveEventType.GET_BY_USER);
        autoApproveEmmiter.send(event);
    }

    @Transactional
    public void setAutoApprove(AutoApproveEvent event) {
        Accommodation accommodation = accommodationRepository.findById(event.getAccommodationId());
        if (accommodation != null) {
            accommodation.setAutoAcceptReservations(event.isAutoapprove());
            persist(accommodation);
        } else {
            throw new AccommodationNotFoundException();
        }
    }

    @Transactional
    public void changeAutoapproveInAccommodations(AutoApproveEvent event) {
        accommodationRepository.changeAutoapproveInAccommodations(event);
        if (event.isAutoapprove()) {
            approveExistingReservations(event.getUsername());

        }
    }

    public void approveExistingReservations(String username) {
        Reservation r = reservationRepository.findFirstPending(username);
        while (r != null) {
            reservationService.approve(r.getId());
            reservationService.eventEmitter.send(new NotificationEvent("Your reservation has been approved", r.getGuestUsername(), NotificationType.RESERVATION_REQUEST_RESOLVED));
            r = reservationRepository.findFirstPending(username);
        }
    }

    public List<AccommodationDto> getOwnersAccommodations(String ownerUsername) {
        List<Accommodation> accommodations = accommodationRepository.findByHostUsername(ownerUsername);
        if (accommodations == null || accommodations.isEmpty()) {
            return List.of();
        }

        return accommodations.stream()
                .map(AccommodationDto::new)
                .collect(Collectors.toList());
    }
}

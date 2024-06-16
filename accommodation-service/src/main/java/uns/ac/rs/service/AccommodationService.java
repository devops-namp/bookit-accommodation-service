package uns.ac.rs.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import uns.ac.rs.controlller.dto.AccommodationDto;
import uns.ac.rs.controlller.dto.AccommodationWithPrice;
import uns.ac.rs.controlller.dto.DateInfoDto;
import uns.ac.rs.entity.Accommodation;
import uns.ac.rs.entity.Image;
import uns.ac.rs.entity.PriceAdjustment;
import uns.ac.rs.entity.PriceAdjustmentDate;
import uns.ac.rs.repository.AccommodationRepository;
import uns.ac.rs.repository.ImageRepository;
import uns.ac.rs.repository.PriceAdjustmentDateRepository;
import uns.ac.rs.repository.PriceAdjustmentRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
    public Accommodation adjustPrices(Long id, Map<LocalDate, Double> newPrices) {
        var accommodationOptional = accommodationRepository.findByIdOptional(id);
        if (accommodationOptional.isEmpty()) {
            return null;
        }
        var accommodation = accommodationOptional.get();

        for (var priceAdjustment : accommodation.getPriceAdjustments()) {
            priceAdjustmentRepository.delete(priceAdjustment);
        }
        accommodation.getPriceAdjustments().clear();

        List<PriceAdjustment> priceAdjustments = new ArrayList<>();

        newPrices.forEach((date, price) -> {
            var priceAdjustment = new PriceAdjustment();
            priceAdjustment.setAccommodation(accommodation);
            priceAdjustmentRepository.persist(priceAdjustment);

            var priceAdjustmentDate = new PriceAdjustmentDate(date, price);
            priceAdjustmentDate.setPriceAdjustment(priceAdjustment);
            priceAdjustmentDateRepository.persist(priceAdjustmentDate);

            priceAdjustment.setPriceAdjustmentDate(priceAdjustmentDate);
            priceAdjustmentRepository.persist(priceAdjustment);
            priceAdjustments.add(priceAdjustment);
        });

        priceAdjustments.sort(Comparator.comparing(obj -> obj.getPriceAdjustmentDate().getDate()));

        accommodation.setPriceAdjustments(priceAdjustments);
        accommodationRepository.persist(accommodation);
        return accommodation;
    }

    @Transactional
    public Accommodation removePrices(Long id, List<LocalDate> toRemove) {
        // TODO: implement logic here
        return null;
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
}

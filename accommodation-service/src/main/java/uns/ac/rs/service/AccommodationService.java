package uns.ac.rs.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import uns.ac.rs.controlller.dto.AccommodationDto;
import uns.ac.rs.controlller.dto.AccommodationWithPrice;
import uns.ac.rs.entity.Accommodation;
import uns.ac.rs.entity.PriceAdjustment;
import uns.ac.rs.entity.PriceAdjustmentDate;
import uns.ac.rs.repository.AccommodationRepository;
import uns.ac.rs.repository.PriceAdjustmentDateRepository;
import uns.ac.rs.repository.PriceAdjustmentRepository;

import java.time.LocalDate;
import java.util.*;

@ApplicationScoped
public class AccommodationService {

    @Inject
    AccommodationRepository accommodationRepository;

    @Inject
    PriceAdjustmentRepository priceAdjustmentRepository;

    @Inject
    PriceAdjustmentDateRepository priceAdjustmentDateRepository;

    public List<Accommodation> getAll() {
        return accommodationRepository.listAll();
    }

    public Optional<Accommodation> getById(Long id) {
        return accommodationRepository.findByIdOptional(id);
    }

    @Transactional
    public void addAccommodation(Accommodation accommodation) {
        accommodationRepository.persist(accommodation);
    }

    @Transactional
    public void updateAccommodation(Long id, Accommodation updatedAccommodation) {
        accommodationRepository.findByIdOptional(id).ifPresent(existingAccommodation -> {
            existingAccommodation.setName(updatedAccommodation.getName());
            existingAccommodation.setLocation(updatedAccommodation.getLocation());
            existingAccommodation.setFilters(updatedAccommodation.getFilters());
            existingAccommodation.setMinGuests(updatedAccommodation.getMinGuests());
            existingAccommodation.setMaxGuests(updatedAccommodation.getMaxGuests());
            existingAccommodation.setPriceType(updatedAccommodation.getPriceType());
            existingAccommodation.setPriceAdjustments(updatedAccommodation.getPriceAdjustments());
            existingAccommodation.setImages(updatedAccommodation.getImages());
            accommodationRepository.persist(existingAccommodation);
        });
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

    public List<AccommodationWithPrice> searchAccommodations(String name, String location, List<String> filters, Integer numGuests
                                                        , LocalDate fromDate, LocalDate toDate, Double fromPrice, Double toPrice,
                                                       String priceType) {
        List<AccommodationWithPrice> accommodationDtos = new ArrayList<>();
        System.out.println("USAO U SERVIS");
        for(AccommodationWithPrice accommodationWithPrice : accommodationRepository.search(name, location, filters, numGuests, fromDate, toDate, fromPrice,toPrice, priceType)){
            accommodationDtos.add(accommodationWithPrice);
        }
        return accommodationDtos;
    }
}

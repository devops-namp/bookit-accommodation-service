package uns.ac.rs.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import uns.ac.rs.controlller.dto.AccommodationDto;
import uns.ac.rs.controlller.dto.AccommodationWithPrice;
import uns.ac.rs.entity.Accommodation;
import uns.ac.rs.entity.PriceAdjustmentDate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AccommodationRepository implements PanacheRepository<Accommodation> {
    public List<AccommodationWithPrice> search(String name, String location, List<String> filters, Integer numGuests,
                                               LocalDate fromDate, LocalDate toDate, Double fromPrice, Double toPrice, String priceType) {
        StringBuilder query = new StringBuilder("SELECT a FROM Accommodation a " +
                "LEFT JOIN a.priceAdjustments pa " +
                "LEFT JOIN pa.priceAdjustmentDate pad " +
                "LEFT JOIN a.reservations r " +
                "LEFT JOIN r.priceAdjustmentDate rad " +
                "WHERE 1=1");

        if (name != null) {
            query.append(" AND a.name LIKE CONCAT('%', :name, '%')");
        }
        if (location != null) {
            query.append(" AND a.location LIKE CONCAT('%', :location, '%')");
        }
        if (numGuests != null) {
            query.append(" AND a.maxGuests >= :numGuests AND a.minGuests <= :numGuests ");
        }
        if (priceType != null) {
            query.append(" AND a.priceType = :priceType");
        }
        if (filters != null && !filters.isEmpty()) {
            for (int i = 0; i < filters.size(); i++) {
                query.append(" AND a.filters LIKE CONCAT('%', :filter").append(i).append(", '%')");
            }
        }

        if (fromDate != null && toDate != null) {
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate) + 1;
            query.append(" AND NOT EXISTS (SELECT 1 FROM Reservation r WHERE r.accommodation.id = a.id AND r.priceAdjustmentDate.date BETWEEN :fromDate AND :toDate)");
            query.append(" AND (SELECT COUNT(*) FROM PriceAdjustment pa WHERE pa.accommodation.id = a.id AND pa.priceAdjustmentDate.date BETWEEN :fromDate AND :toDate)=").append(daysBetween);
        } else if (fromDate != null) {
            query.append(" AND NOT EXISTS (SELECT 1 FROM Reservation r WHERE r.accommodation.id = a.id AND r.priceAdjustmentDate.date >= :fromDate)");
        } else if (toDate != null) {
            query.append(" AND NOT EXISTS (SELECT 1 FROM Reservation r WHERE r.accommodation.id = a.id AND r.priceAdjustmentDate.date <= :toDate)");
        }

        query.append(" GROUP BY a.id");

        var queryBuilder = getEntityManager().createQuery(query.toString(), Object[].class);

        if (name != null) {
            queryBuilder.setParameter("name", name);
        }
        if (location != null) {
            queryBuilder.setParameter("location", location);
        }
        if (numGuests != null) {
            queryBuilder.setParameter("numGuests", numGuests);
        }
        if (priceType != null) {
            queryBuilder.setParameter("priceType", priceType);
        }
        if (filters != null && !filters.isEmpty()) {
            for (int i = 0; i < filters.size(); i++) {
                queryBuilder.setParameter("filter" + i, filters.get(i));
            }
        }
        if (fromDate != null) {
            queryBuilder.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            queryBuilder.setParameter("toDate", toDate);
        }

        List<Object[]> results = queryBuilder.getResultList();
        List<AccommodationWithPrice> accommodationsWithPrices = new ArrayList<>();
        for (Object[] result : results) {
            Accommodation accommodation = (Accommodation) result[0];
            double totalPrice = 0.0;

            if (fromDate != null && toDate != null) {
                List<PriceAdjustmentDate> priceAdjustments = getEntityManager().createQuery(
                        "SELECT pad FROM PriceAdjustmentDate pad WHERE pad.date BETWEEN :fromDate AND :toDate AND pad.priceAdjustment.accommodation.id = :accommodationId",
                        PriceAdjustmentDate.class)
                        .setParameter("fromDate", fromDate)
                        .setParameter("toDate", toDate)
                        .setParameter("accommodationId", accommodation.getId())
                        .getResultList();

                for (PriceAdjustmentDate pad : priceAdjustments) {
                    totalPrice += pad.getPrice();
                }
            }
            System.out.println(fromPrice);
            System.out.println(toPrice);
            System.out.println(totalPrice);
            if ((fromPrice == null || totalPrice >= fromPrice) && (toPrice == null || totalPrice <= toPrice)) {
                accommodationsWithPrices.add(new AccommodationWithPrice(new AccommodationDto(accommodation), totalPrice));
            }
        }

        return accommodationsWithPrices;
    }
}

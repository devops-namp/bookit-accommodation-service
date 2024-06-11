package uns.ac.rs.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import uns.ac.rs.controlller.dto.AccommodationDto;
import uns.ac.rs.controlller.dto.AccommodationWithPrice;
import uns.ac.rs.entity.Accommodation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AccommodationRepository implements PanacheRepository<Accommodation> {
    public List<AccommodationWithPrice> search(String name, String location, List<String> filters, Integer minGuests, Integer maxGuests,
                                               LocalDate fromDate, LocalDate toDate, Double fromPrice, Double toPrice, String priceType) {
        StringBuilder query = new StringBuilder("SELECT a, SUM(pad.price) as totalPrice FROM Accommodation a " +
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
        if (minGuests != null) {
            query.append(" AND a.minGuests <= :minGuests");
        }
        if (maxGuests != null) {
            query.append(" AND a.maxGuests >= :maxGuests");
        }
        if (priceType != null) {
            query.append(" AND a.priceType = :priceType");
        }
        if (filters != null && !filters.isEmpty()) {
            for (int i = 0; i < filters.size(); i++) {
                query.append(" AND a.filters LIKE CONCAT('%', :filter").append(i).append(", '%')");
            }
        }

        if (fromDate != null || toDate != null) {
            if (fromDate != null && toDate != null) {
                query.append(" AND pad.date BETWEEN :fromDate AND :toDate");
                query.append(" AND (r.id IS NULL OR rad.date NOT BETWEEN :fromDate AND :toDate)");
            } else if (fromDate != null) {
                query.append(" AND pad.date >= :fromDate");
                query.append(" AND (r.id IS NULL OR rad.date > :fromDate)");
            } else {
                query.append(" AND pad.date <= :toDate");
                query.append(" AND (r.id IS NULL OR rad.date < :toDate)");
            }
        }

        if (fromPrice != null || toPrice != null) {
            query.append(" GROUP BY a HAVING ");
            if (fromPrice != null && toPrice != null) {
                query.append("SUM(pad.price) BETWEEN :fromPrice AND :toPrice");
            } else if (fromPrice != null) {
                query.append("SUM(pad.price) >= :fromPrice");
            } else {
                query.append("SUM(pad.price) <= :toPrice");
            }
        } else {
            query.append(" GROUP BY a");
        }

        var queryBuilder = getEntityManager().createQuery(query.toString(), Object[].class);

        if (name != null) {
            queryBuilder.setParameter("name", name);
        }
        if (location != null) {
            queryBuilder.setParameter("location", location);
        }
        if (minGuests != null) {
            queryBuilder.setParameter("minGuests", minGuests);
        }
        if (maxGuests != null) {
            queryBuilder.setParameter("maxGuests", maxGuests);
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
        if (fromPrice != null) {
            queryBuilder.setParameter("fromPrice", fromPrice);
        }
        if (toPrice != null) {
            queryBuilder.setParameter("toPrice", toPrice);
        }

        List<Object[]> results = queryBuilder.getResultList();
        List<AccommodationWithPrice> accommodationsWithPrices = new ArrayList<>();
        for (Object[] result : results) {
            Accommodation accommodation = (Accommodation) result[0];
            Double totalPrice = (Double) result[1];
            accommodationsWithPrices.add(new AccommodationWithPrice( new AccommodationDto(accommodation), totalPrice));
        }

        return accommodationsWithPrices;
    }
}

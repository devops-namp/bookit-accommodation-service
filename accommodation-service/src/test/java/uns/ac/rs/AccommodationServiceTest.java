package uns.ac.rs;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uns.ac.rs.controlller.dto.AccommodationDto;
import uns.ac.rs.controlller.dto.AccommodationWithPrice;
import uns.ac.rs.entity.Accommodation;
import uns.ac.rs.entity.PriceAdjustment;
import uns.ac.rs.entity.PriceAdjustmentDate;
import uns.ac.rs.exceptions.AccommodationNotFoundException;
import uns.ac.rs.exceptions.ReservationExistsOnDateException;
import uns.ac.rs.repository.AccommodationRepository;
import uns.ac.rs.repository.PriceAdjustmentDateRepository;
import uns.ac.rs.repository.PriceAdjustmentRepository;
import uns.ac.rs.repository.ReservationRepository;
import uns.ac.rs.service.AccommodationService;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class AccommodationServiceTest {

    @Mock
    AccommodationRepository accommodationRepository;

    @Mock
    PriceAdjustmentRepository priceAdjustmentRepository;

    @Mock
    PriceAdjustmentDateRepository priceAdjustmentDateRepository;

    @Mock
    ReservationRepository reservationRepository;

    @InjectMocks
    AccommodationService accommodationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAll() {
        List<Accommodation> accommodations = List.of(new Accommodation());
        when(accommodationRepository.listAll()).thenReturn(accommodations);

        List<Accommodation> result = accommodationService.getAll();
        assertEquals(1, result.size());
        verify(accommodationRepository, times(1)).listAll();
    }

    @Test
    void testGetById() {
        Accommodation accommodation = new Accommodation();
        when(accommodationRepository.findByIdOptional(1L)).thenReturn(Optional.of(accommodation));

        Optional<Accommodation> result = accommodationService.getById(1L);
        assertTrue(result.isPresent());
        assertEquals(accommodation, result.get());
        verify(accommodationRepository, times(1)).findByIdOptional(1L);
    }

    @Test
    void testAddAccommodation() {
        Accommodation accommodation = new Accommodation();
        accommodationService.addAccommodation(accommodation);

        verify(accommodationRepository, times(1)).persist(accommodation);
    }

    @Test
    void testUpdateAccommodation() {
        Accommodation existingAccommodation = new Accommodation();
        existingAccommodation.setHostUsername("host");
        existingAccommodation.setId(1L);
        Accommodation updatedAccommodation = new Accommodation();

        updatedAccommodation.setName("Updated Name");

        when(accommodationRepository.findByIdOptional(1L)).thenReturn(Optional.of(existingAccommodation));

        AccommodationDto updatedAccommodationDto = new AccommodationDto(updatedAccommodation);
        accommodationService.updateAccommodation(1L, updatedAccommodationDto, "host");

        verify(accommodationRepository, times(1)).findByIdOptional(1L);
        verify(accommodationRepository, times(1)).persist(existingAccommodation);
        assertEquals("Updated Name", existingAccommodation.getName());
    }

    @Test
    void testDeleteAccommodation() {
        Accommodation accommodation = new Accommodation();
        when(accommodationRepository.findByIdOptional(1L)).thenReturn(Optional.of(accommodation));

        accommodationService.deleteAccommodation(1L);

        verify(accommodationRepository, times(1)).findByIdOptional(1L);
        verify(accommodationRepository, times(1)).delete(accommodation);
    }

    @Test
    public void testAdjustPrices_AccommodationNotFound() {
        Long accommodationId = 5L;
        Map<LocalDate, Double> newPrices = new HashMap<>();

        when(accommodationRepository.findByIdOptional(accommodationId)).thenReturn(Optional.empty());

        assertThrows(AccommodationNotFoundException.class, () -> accommodationService.adjustPrices(accommodationId, newPrices));

        verify(accommodationRepository, times(1)).findByIdOptional(accommodationId);
        verify(priceAdjustmentRepository, never()).delete(any());
        verifyNoInteractions(priceAdjustmentRepository);
        verify(priceAdjustmentDateRepository, never()).persist(any(PriceAdjustmentDate.class));
        verifyNoInteractions(priceAdjustmentDateRepository);
    }

    @Test
    public void testAdjustPrices_AccommodationFound_NoReservations() {
        Long accommodationId = 1L;
        Map<LocalDate, Double> newPrices = new HashMap<>();
        newPrices.put(LocalDate.of(2024, 4, 15), 500.0);
        newPrices.put(LocalDate.of(2024, 4, 17), 600.0);

        var accommodation = new Accommodation();
        accommodation.setId(accommodationId);
        accommodation.setPriceAdjustments(new ArrayList<>());

        when(accommodationRepository.findByIdOptional(accommodationId)).thenReturn(Optional.of(accommodation));
        when(reservationRepository.exists(any(), any())).thenReturn(false);

        var result = accommodationService.adjustPrices(accommodationId, newPrices);

        assertNotNull(result);
        assertEquals(2, result.getPriceAdjustments().size());
        assertEquals(500.0, result.getPriceAdjustments().get(0).getPriceAdjustmentDate().getPrice());
        assertEquals(LocalDate.of(2024, 4, 15), result.getPriceAdjustments().get(0).getPriceAdjustmentDate().getDate());
        assertEquals(600.0, result.getPriceAdjustments().get(1).getPriceAdjustmentDate().getPrice());
        assertEquals(LocalDate.of(2024, 4, 17), result.getPriceAdjustments().get(1).getPriceAdjustmentDate().getDate());

        verify(accommodationRepository, times(1)).findByIdOptional(accommodationId);
        verify(priceAdjustmentRepository, times(4)).persist(any(PriceAdjustment.class));
        verify(priceAdjustmentDateRepository, times(2)).persist(any(PriceAdjustmentDate.class));
        verify(accommodationRepository, times(1)).persist(accommodation);
    }

    @Test
    public void testAdjustPrices_ExistingPriceAdjustmentsDeleted() {
        Long accommodationId = 1L;
        Map<LocalDate, Double> newPrices = new HashMap<>();
        newPrices.put(LocalDate.of(2024, 4, 15), 500.0);

        var accommodation = new Accommodation();
        accommodation.setId(accommodationId);

        var existingPriceAdjustment = new PriceAdjustment();
        existingPriceAdjustment.setAccommodation(accommodation);
        existingPriceAdjustment.setPriceAdjustmentDate(new PriceAdjustmentDate(LocalDate.of(2024, 10, 10), 10));
        accommodation.setPriceAdjustments(new ArrayList<>(Arrays.asList(existingPriceAdjustment)));

        when(accommodationRepository.findByIdOptional(accommodationId)).thenReturn(Optional.of(accommodation));

        var result = accommodationService.adjustPrices(accommodationId, newPrices);

        assertNotNull(result);
        assertEquals(1, result.getPriceAdjustments().size());
        assertEquals(500.0, result.getPriceAdjustments().get(0).getPriceAdjustmentDate().getPrice());
        assertEquals(LocalDate.of(2024, 4, 15), result.getPriceAdjustments().get(0).getPriceAdjustmentDate().getDate());

        verify(accommodationRepository, times(1)).findByIdOptional(accommodationId);
        verify(priceAdjustmentRepository, times(1)).delete(existingPriceAdjustment);
        verify(priceAdjustmentRepository, times(2)).persist(any(PriceAdjustment.class));
        verify(priceAdjustmentDateRepository, times(1)).persist(any(PriceAdjustmentDate.class));
        verify(accommodationRepository, times(1)).persist(accommodation);
    }

    @Test
    void testSearch() {
        AccommodationWithPrice accommodation = new AccommodationWithPrice();
        List<AccommodationWithPrice> accommodations = List.of(accommodation);
        when(accommodationRepository.search(anyString(), anyString(), anyList(), anyInt(), any(LocalDate.class), any(LocalDate.class), anyDouble(), anyDouble(), anyString()))
                .thenReturn(accommodations);

        List<AccommodationWithPrice> result = accommodationService.searchAccommodations("Ocean View", "Miami Beach, FL", List.of("wifi", "parking", "kitchen"), 4, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 30), 150.00, 500.00, "price_per_unit");
        assertEquals(1, result.size());
        verify(accommodationRepository, times(1)).search(anyString(), anyString(), anyList(), anyInt(), any(LocalDate.class), any(LocalDate.class), anyDouble(),anyDouble(), anyString());
    }
    @Test
    void testSearchWithSpecificFilters() {
        AccommodationWithPrice accommodation = new AccommodationWithPrice();
        List<AccommodationWithPrice> accommodations = List.of(accommodation);
        when(accommodationRepository.search(anyString(), anyString(), anyList(), anyInt(), any(LocalDate.class), any(LocalDate.class), anyDouble(), anyDouble(), anyString()))
                .thenReturn(accommodations);

        List<AccommodationWithPrice> result = accommodationService.searchAccommodations(
                "", "Aspen",  new ArrayList<>(Arrays.asList("wifi", "parking", "fireplace", "bath")), 2,
                LocalDate.of(2024, 7, 10), LocalDate.of(2024, 7, 14), 0.0, 0.0, "price_per_person"
        );

        assertEquals(1, result.size());
        verify(accommodationRepository, times(1)).search(
                eq(""), eq("Aspen"), eq(List.of("wifi", "parking", "fireplace", "bath")),
                eq(2), eq(LocalDate.of(2024, 7, 10)), eq(LocalDate.of(2024, 7, 14)),
                eq(0.0), eq(0.0), eq("price_per_person")
        );
    }

    @Test
    void testSearchWithSpecificNumberOfGuests() {
        AccommodationWithPrice accommodation = new AccommodationWithPrice();
        List<AccommodationWithPrice> accommodations = List.of(accommodation);
        when(accommodationRepository.search(anyString(), anyString(), anyList(), anyInt(), any(LocalDate.class), any(LocalDate.class), anyDouble(), anyDouble(), anyString()))
                .thenReturn(accommodations);

        List<AccommodationWithPrice> result = accommodationService.searchAccommodations(
                "", "Malibu", List.of("wifi", "parking", "pool"), 3,
                LocalDate.of(2024, 7, 10), LocalDate.of(2024, 7, 14), 0.0, 0.0, "price_per_unit"
        );

        assertEquals(1, result.size());
        verify(accommodationRepository, times(1)).search(
                eq(""), eq("Malibu"), eq(List.of("wifi", "parking", "pool")),
                eq(3), eq(LocalDate.of(2024, 7, 10)), eq(LocalDate.of(2024, 7, 14)),
                eq(0.0), eq(0.0), eq("price_per_unit")
        );
    }

    @Test
    void testSearchWithDateRange() {
        AccommodationWithPrice accommodation = new AccommodationWithPrice();
        List<AccommodationWithPrice> accommodations = List.of(accommodation);
        when(accommodationRepository.search(anyString(), anyString(), anyList(), anyInt(), any(LocalDate.class), any(LocalDate.class), anyDouble(), anyDouble(), anyString()))
                .thenReturn(accommodations);

        List<AccommodationWithPrice> result = accommodationService.searchAccommodations(
                "", "New York, NY", List.of("wifi", "parking", "tv"), 1,
                LocalDate.of(2024, 7, 12), LocalDate.of(2024, 7, 16), 0.0, 0.0, "price_per_person"
        );

        assertEquals(1, result.size());
        verify(accommodationRepository, times(1)).search(
                eq(""), eq("New York, NY"), eq(List.of("wifi", "parking", "tv")),
                eq(1), eq(LocalDate.of(2024, 7, 12)), eq(LocalDate.of(2024, 7, 16)),
                eq(0.0), eq(0.0), eq("price_per_person")
        );
    }

    @Test
    void testSearchWithPriceRange() {
        AccommodationWithPrice accommodation = new AccommodationWithPrice();
        List<AccommodationWithPrice> accommodations = List.of(accommodation);
        when(accommodationRepository.search(anyString(), anyString(), anyList(), anyInt(), any(LocalDate.class), any(LocalDate.class), anyDouble(), anyDouble(), anyString()))
                .thenReturn(accommodations);

        List<AccommodationWithPrice> result = accommodationService.searchAccommodations(
                "", "Malibu", List.of("wifi", "parking", "pool"), 3,
                LocalDate.of(2024, 7, 10), LocalDate.of(2024, 7, 14), 200.0, 800.0, "price_per_unit"
        );

        assertEquals(1, result.size());
        verify(accommodationRepository, times(1)).search(
                eq(""), eq("Malibu"), eq(List.of("wifi", "parking", "pool")),
                eq(3), eq(LocalDate.of(2024, 7, 10)), eq(LocalDate.of(2024, 7, 14)),
                eq(200.0), eq(800.0), eq("price_per_unit")
        );
    }


    @Test
    void testSearchWithNoMatchingFilters() {
        when(accommodationRepository.search(anyString(), anyString(), anyList(), anyInt(), any(LocalDate.class), any(LocalDate.class), anyDouble(), anyDouble(), anyString()))
                .thenReturn(new ArrayList<>());

        List<AccommodationWithPrice> result = accommodationService.searchAccommodations(
                "", "Malibu", List.of("gym"), 2,
                LocalDate.of(2024, 7, 10), LocalDate.of(2024, 7, 14), 0.0, 0.0, "price_per_unit"
        );

        assertTrue(result.isEmpty());
        verify(accommodationRepository, times(1)).search(
                eq(""), eq("Malibu"), eq(List.of("gym")),
                eq(2), eq(LocalDate.of(2024, 7, 10)), eq(LocalDate.of(2024, 7, 14)),
                eq(0.0), eq(0.0), eq("price_per_unit")
        );
    }

    @Test
    void testSearchWithNoMatchingDateRange() {
        when(accommodationRepository.search(anyString(), anyString(), anyList(), anyInt(), any(LocalDate.class), any(LocalDate.class), anyDouble(), anyDouble(), anyString()))
                .thenReturn(new ArrayList<>());

        List<AccommodationWithPrice> result = accommodationService.searchAccommodations(
                "", "Malibu", List.of("wifi", "parking", "pool"), 3,
                LocalDate.of(2024, 8, 1), LocalDate.of(2024, 8, 15), 0.0, 0.0, "price_per_unit"
        );

        assertTrue(result.isEmpty());
        verify(accommodationRepository, times(1)).search(
                eq(""), eq("Malibu"), eq(List.of("wifi", "parking", "pool")),
                eq(3), eq(LocalDate.of(2024, 8, 1)), eq(LocalDate.of(2024, 8, 15)),
                eq(0.0), eq(0.0), eq("price_per_unit")
        );
    }

    @Test
    void testSearchWithNoMatchingPriceRange() {
        when(accommodationRepository.search(anyString(), anyString(), anyList(), anyInt(), any(LocalDate.class), any(LocalDate.class), anyDouble(), anyDouble(), anyString()))
                .thenReturn(new ArrayList<>());

        List<AccommodationWithPrice> result = accommodationService.searchAccommodations(
                "", "Malibu", List.of("wifi", "parking", "pool"), 3,
                LocalDate.of(2024, 7, 10), LocalDate.of(2024, 7, 14), 1000.0, 2000.0, "price_per_unit"
        );

        assertTrue(result.isEmpty());
        verify(accommodationRepository, times(1)).search(
                eq(""), eq("Malibu"), eq(List.of("wifi", "parking", "pool")),
                eq(3), eq(LocalDate.of(2024, 7, 10)), eq(LocalDate.of(2024, 7, 14)),
                eq(1000.0), eq(2000.0), eq("price_per_unit")
        );
    }

    @Test
    void testSearchWithExceedingNumberOfGuests() {
        when(accommodationRepository.search(anyString(), anyString(), anyList(), anyInt(), any(LocalDate.class), any(LocalDate.class), anyDouble(), anyDouble(), anyString()))
                .thenReturn(new ArrayList<>());

        List<AccommodationWithPrice> result = accommodationService.searchAccommodations(
                "", "Malibu", List.of("wifi", "parking", "pool"), 10,
                LocalDate.of(2024, 7, 10), LocalDate.of(2024, 7, 14), 0.0, 0.0, "price_per_unit"
        );

        assertTrue(result.isEmpty());
        verify(accommodationRepository, times(1)).search(
                eq(""), eq("Malibu"), eq(List.of("wifi", "parking", "pool")),
                eq(10), eq(LocalDate.of(2024, 7, 10)), eq(LocalDate.of(2024, 7, 14)),
                eq(0.0), eq(0.0), eq("price_per_unit")
        );
    }
}

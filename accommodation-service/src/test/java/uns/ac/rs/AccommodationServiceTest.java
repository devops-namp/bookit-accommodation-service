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
import uns.ac.rs.repository.AccommodationRepository;
import uns.ac.rs.repository.PriceAdjustmentDateRepository;
import uns.ac.rs.repository.PriceAdjustmentRepository;
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
        existingAccommodation.setId(1L);
        Accommodation updatedAccommodation = new Accommodation();
        updatedAccommodation.setName("Updated Name");

        when(accommodationRepository.findByIdOptional(1L)).thenReturn(Optional.of(existingAccommodation));

        accommodationService.updateAccommodation(1L, updatedAccommodation);

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

        var result = accommodationService.adjustPrices(accommodationId, newPrices);

        assertNull(result);
        verify(accommodationRepository, times(1)).findByIdOptional(accommodationId);
        verify(priceAdjustmentRepository, never()).delete(any());
        verifyNoInteractions(priceAdjustmentRepository);
        verify(priceAdjustmentDateRepository, never()).persist(any(PriceAdjustmentDate.class));
        verifyNoInteractions(priceAdjustmentDateRepository);
    }

    @Test
    public void testAdjustPrices_AccommodationFound() {
        Long accommodationId = 1L;
        Map<LocalDate, Double> newPrices = new HashMap<>();
        newPrices.put(LocalDate.of(2024, 4, 15), 500.0);
        newPrices.put(LocalDate.of(2024, 4, 17), 600.0);

        var accommodation = new Accommodation();
        accommodation.setId(accommodationId);
        accommodation.setPriceAdjustments(new ArrayList<>());

        when(accommodationRepository.findByIdOptional(accommodationId)).thenReturn(Optional.of(accommodation));

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

        List<AccommodationWithPrice> result = accommodationService.searchAccommodations("Ocean View", "Miami Beach, FL", List.of("wifi", "free parking", "kitchen"), 4, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 30), 150.00, 500.00, "price per unit");
        assertEquals(1, result.size());
        verify(accommodationRepository, times(1)).search(anyString(), anyString(), anyList(), anyInt(), any(LocalDate.class), any(LocalDate.class), anyDouble(),anyDouble(), anyString());
    }
}

package uns.ac.rs;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import uns.ac.rs.controlller.dto.AccommodationDto;
import uns.ac.rs.controlller.dto.AccommodationWithPrice;
import uns.ac.rs.entity.Accommodation;
import uns.ac.rs.entity.PriceAdjustment;
import uns.ac.rs.entity.PriceAdjustmentDate;
import uns.ac.rs.entity.events.AutoApproveEvent;
import uns.ac.rs.entity.events.NotificationEvent;
import uns.ac.rs.exceptions.AccommodationNotFoundException;
import uns.ac.rs.exceptions.ReservationExistsOnDateException;
import uns.ac.rs.repository.AccommodationRepository;
import uns.ac.rs.repository.PriceAdjustmentDateRepository;
import uns.ac.rs.repository.PriceAdjustmentRepository;
import uns.ac.rs.repository.ReservationRepository;
import uns.ac.rs.service.AccommodationService;
import uns.ac.rs.service.ReservationService;

import java.lang.reflect.Field;
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

    @InjectMocks
    ReservationService reservationService;

    @Mock
    Emitter<AutoApproveEvent> autoApproveEmmiter;

    @Mock
    Emitter<NotificationEvent> eventEmitter;



    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        setPrivateField(accommodationService, "autoApproveEmmiter", autoApproveEmmiter);
        setPrivateField(reservationService, "eventEmitter", eventEmitter);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
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
        verify(priceAdjustmentRepository, times(2)).persist(any(PriceAdjustment.class));
        verify(priceAdjustmentDateRepository, times(2)).persist(any(PriceAdjustmentDate.class));
        verify(accommodationRepository, times(1)).persist(accommodation);
    }

    @Test
    public void testAdjustPrices_WithExistingPrices_NoReservations() {
        Long accommodationId = 1L;
        Map<LocalDate, Double> newPrices = new HashMap<>();
        newPrices.put(LocalDate.of(2024, 4, 15), 500.0);
        newPrices.put(LocalDate.of(2024, 10, 11), 200.0);
        newPrices.put(LocalDate.of(2024, 3, 15), 1500.0);

        var accommodation = new Accommodation();
        accommodation.setId(accommodationId);

        var priceAdjustments = new ArrayList<PriceAdjustment>();
        var existingPriceAdjustment = new PriceAdjustment();
        existingPriceAdjustment.setAccommodation(accommodation);
        existingPriceAdjustment.setPriceAdjustmentDate(new PriceAdjustmentDate(LocalDate.of(2024, 10, 10), 10));
        priceAdjustments.add(existingPriceAdjustment);

        existingPriceAdjustment = new PriceAdjustment();
        existingPriceAdjustment.setAccommodation(accommodation);
        existingPriceAdjustment.setPriceAdjustmentDate(new PriceAdjustmentDate(LocalDate.of(2024, 10, 11), 11));
        priceAdjustments.add(existingPriceAdjustment);

        existingPriceAdjustment = new PriceAdjustment();
        existingPriceAdjustment.setAccommodation(accommodation);
        existingPriceAdjustment.setPriceAdjustmentDate(new PriceAdjustmentDate(LocalDate.of(2024, 10, 12), 12));
        priceAdjustments.add(existingPriceAdjustment);

        accommodation.setPriceAdjustments(priceAdjustments);

        when(accommodationRepository.findByIdOptional(accommodationId)).thenReturn(Optional.of(accommodation));
        when(reservationRepository.exists(any(), any())).thenReturn(false);

        var result = accommodationService.adjustPrices(accommodationId, newPrices);

        assertNotNull(result);
        assertEquals(5, result.getPriceAdjustments().size());
        assertEquals(1500.0, result.getPriceAdjustments().get(0).getPriceAdjustmentDate().getPrice());
        assertEquals(500.0, result.getPriceAdjustments().get(1).getPriceAdjustmentDate().getPrice());
        assertEquals(10.0, result.getPriceAdjustments().get(2).getPriceAdjustmentDate().getPrice());
        assertEquals(200.0, result.getPriceAdjustments().get(3).getPriceAdjustmentDate().getPrice());
        assertEquals(12.0, result.getPriceAdjustments().get(4).getPriceAdjustmentDate().getPrice());
        assertEquals(LocalDate.of(2024, 3, 15), result.getPriceAdjustments().get(0).getPriceAdjustmentDate().getDate());
        assertEquals(LocalDate.of(2024, 4, 15), result.getPriceAdjustments().get(1).getPriceAdjustmentDate().getDate());
        assertEquals(LocalDate.of(2024, 10, 10), result.getPriceAdjustments().get(2).getPriceAdjustmentDate().getDate());
        assertEquals(LocalDate.of(2024, 10, 11), result.getPriceAdjustments().get(3).getPriceAdjustmentDate().getDate());
        assertEquals(LocalDate.of(2024, 10, 12), result.getPriceAdjustments().get(4).getPriceAdjustmentDate().getDate());

        verify(accommodationRepository, times(1)).findByIdOptional(accommodationId);
        verify(priceAdjustmentRepository, times(2)).persist(any(PriceAdjustment.class));
        verify(priceAdjustmentDateRepository, times(3)).persist(any(PriceAdjustmentDate.class));
        verify(accommodationRepository, times(1)).persist(accommodation);
    }

    @Test
    public void testAdjustPrices_WithExistingPrices_WithReservations() {
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
        when(reservationRepository.exists(any(), any())).thenReturn(true);

        assertThrows(ReservationExistsOnDateException.class, () -> accommodationService.adjustPrices(accommodationId, newPrices));

        verify(accommodationRepository, times(1)).findByIdOptional(accommodationId);
        verifyNoInteractions(priceAdjustmentRepository);
        verifyNoInteractions(priceAdjustmentDateRepository);
        verify(accommodationRepository, times(0)).persist(accommodation);
    }

    @Test
    public void testRemovePrices_Success() {
        Long accommodationId = 1L;
        var toRemove = Set.of(LocalDate.of(2024, 6, 15));

        var accommodation = new Accommodation();
        var priceAdjustment = new PriceAdjustment();
        priceAdjustment.setPriceAdjustmentDate(new PriceAdjustmentDate(LocalDate.of(2024, 6, 15), 100));
        priceAdjustment.setId(100L);

        accommodation.setPriceAdjustments(List.of(priceAdjustment));

        when(accommodationRepository.findByIdOptional(accommodationId)).thenReturn(Optional.of(accommodation));
        when(reservationRepository.exists(accommodationId, LocalDate.of(2024, 6, 15))).thenReturn(false);

        var result = accommodationService.removePrices(accommodationId, toRemove);

        assertNotNull(result);
        assertTrue(result.getPriceAdjustments().isEmpty());

        verify(priceAdjustmentRepository, times(1)).deleteById(100L);
        verify(accommodationRepository, times(1)).persist(any(Accommodation.class));
    }

    @Test
    public void testRemovePrices_AccommodationNotFound() {
        var accommodationId = 1L;
        var toRemove = Set.of(LocalDate.of(2024, 6, 15));

        when(accommodationRepository.findByIdOptional(accommodationId)).thenReturn(Optional.empty());

        assertThrows(AccommodationNotFoundException.class, () -> accommodationService.removePrices(accommodationId, toRemove));
        verifyNoInteractions(priceAdjustmentRepository);
        verify(accommodationRepository, never()).persist(any(Accommodation.class));
    }

    @Test
    public void testRemovePrices_ReservationExistsOnDate() {
        Long accommodationId = 1L;
        Set<LocalDate> toRemove = Set.of(LocalDate.of(2024, 6, 15));

        Accommodation accommodation = new Accommodation();
        PriceAdjustment priceAdjustment = new PriceAdjustment();
        priceAdjustment.setPriceAdjustmentDate(new PriceAdjustmentDate(LocalDate.of(2024, 6, 15), 100));

        accommodation.setPriceAdjustments(List.of(priceAdjustment));

        when(accommodationRepository.findByIdOptional(accommodationId)).thenReturn(Optional.of(accommodation));
        when(reservationRepository.exists(accommodationId, LocalDate.of(2024, 6, 15))).thenReturn(true);

        assertThrows(ReservationExistsOnDateException.class, () -> accommodationService.removePrices(accommodationId, toRemove));
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

    @Test
    void testGetAutoApprove() {
        Long accommodationId = 1L;
        String username = "testUser";

        accommodationService.getAutoApprove(accommodationId, username);

        verify(autoApproveEmmiter).send((AutoApproveEvent) argThat(event ->
                event instanceof AutoApproveEvent &&
                        ((AutoApproveEvent) event).getUsername().equals(username) &&
                        ((AutoApproveEvent) event).getAccommodationId().equals(accommodationId) &&
                        ((AutoApproveEvent) event).getType() == AutoApproveEvent.AutoApproveEventType.GET_BY_USER
        ));
    }

    @Test
    void testSetAutoApprove_accommodationNotFound() {
        Long accommodationId = 1L;
        AutoApproveEvent event = new AutoApproveEvent("testUser", accommodationId, AutoApproveEvent.AutoApproveEventType.CHANGE);
        event.setAutoapprove(true);

        when(accommodationRepository.findById(accommodationId)).thenReturn(null);

        assertThrows(AccommodationNotFoundException.class, () -> accommodationService.setAutoApprove(event));

        verify(accommodationRepository).findById(accommodationId);
        verify(accommodationRepository, never()).persist(any(Accommodation.class));
    }


    @Test
    void testChangeAutoapproveInAccommodations() {
        AutoApproveEvent event = new AutoApproveEvent("testUser", 1L, AutoApproveEvent.AutoApproveEventType.CHANGE);
        event.setAutoapprove(true);

        doNothing().when(accommodationRepository).changeAutoapproveInAccommodations(any(AutoApproveEvent.class));

        accommodationService.changeAutoapproveInAccommodations(event);

        ArgumentCaptor<AutoApproveEvent> captor = ArgumentCaptor.forClass(AutoApproveEvent.class);
        verify(accommodationRepository).changeAutoapproveInAccommodations(captor.capture());

        AutoApproveEvent capturedEvent = captor.getValue();
        assertEquals("testUser", capturedEvent.getUsername());
        assertEquals(1L, capturedEvent.getAccommodationId().longValue());
        assertEquals(AutoApproveEvent.AutoApproveEventType.CHANGE, capturedEvent.getType());
        assertTrue(capturedEvent.isAutoapprove());
    }

    @Test
    void testChangeAutoapproveInAccommodations_doesNotApproveExistingReservations() {
        AutoApproveEvent event = new AutoApproveEvent("testUser", 1L, AutoApproveEvent.AutoApproveEventType.CHANGE);
        event.setAutoapprove(false);

        doNothing().when(accommodationRepository).changeAutoapproveInAccommodations(any(AutoApproveEvent.class));

        accommodationService.changeAutoapproveInAccommodations(event);

        ArgumentCaptor<AutoApproveEvent> captor = ArgumentCaptor.forClass(AutoApproveEvent.class);
        verify(accommodationRepository).changeAutoapproveInAccommodations(captor.capture());

        AutoApproveEvent capturedEvent = captor.getValue();
        assertEquals("testUser", capturedEvent.getUsername());
        assertEquals(1L, capturedEvent.getAccommodationId().longValue());
        assertEquals(AutoApproveEvent.AutoApproveEventType.CHANGE, capturedEvent.getType());
        assertFalse(capturedEvent.isAutoapprove());
    }


}

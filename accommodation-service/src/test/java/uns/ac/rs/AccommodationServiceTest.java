package uns.ac.rs;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uns.ac.rs.entity.Accommodation;
import uns.ac.rs.repository.AccommodationRepository;
import uns.ac.rs.service.AccommodationService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class AccommodationServiceTest {

    @Mock
    AccommodationRepository accommodationRepository;

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
}

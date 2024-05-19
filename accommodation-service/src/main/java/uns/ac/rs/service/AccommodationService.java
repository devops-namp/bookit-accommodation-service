package uns.ac.rs.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import uns.ac.rs.repository.AccommodationRepository;

@ApplicationScoped
public class AccommodationService {

    @Inject
    AccommodationRepository accommodationRepository;
}

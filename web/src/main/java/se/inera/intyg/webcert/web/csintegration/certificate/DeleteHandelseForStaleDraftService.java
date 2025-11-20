package se.inera.intyg.webcert.web.csintegration.certificate;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;

@Service
@RequiredArgsConstructor
public class DeleteHandelseForStaleDraftService {

    private final HandelseRepository handelseRepository;

    @Transactional
    public void delete(String certificateId) {
        handelseRepository.deleteByIntygsId(certificateId);
    }
}
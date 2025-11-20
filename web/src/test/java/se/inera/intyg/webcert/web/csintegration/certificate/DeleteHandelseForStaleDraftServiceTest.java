package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;

@ExtendWith(MockitoExtension.class)
class DeleteHandelseForStaleDraftServiceTest {

    private static final String CERTIFICATE_ID = "id";
    @Mock
    HandelseRepository handelseRepository;
    @InjectMocks
    DeleteHandelseForStaleDraftService deleteHandelseForStaleDraftService;

    @Test
    void shouldDeleteHandelse() {
        deleteHandelseForStaleDraftService.delete(CERTIFICATE_ID);
        verify(handelseRepository).deleteByIntygsId(CERTIFICATE_ID);
    }
}
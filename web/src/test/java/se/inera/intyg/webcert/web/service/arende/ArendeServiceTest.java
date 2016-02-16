package se.inera.intyg.webcert.web.service.arende;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.web.service.exception.WebcertServiceException;

@RunWith(MockitoJUnitRunner.class)
public class ArendeServiceTest {

    @Mock
    private ArendeRepository repo;

    @InjectMocks
    private ArendeServiceImpl service;

    private Arende arende = new Arende();

    @Test
    public void testProcessIncomingMessage() throws WebcertServiceException {
        when(repo.save(any(Arende.class))).thenReturn(arende);
        Arende res = service.processIncomingMessage(arende);
        assertNotNull(res);
    }
}

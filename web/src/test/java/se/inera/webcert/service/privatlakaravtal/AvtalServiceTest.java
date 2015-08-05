package se.inera.webcert.service.privatlakaravtal;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.webcert.persistence.privatlakaravtal.model.Avtal;
import se.inera.webcert.persistence.privatlakaravtal.repository.AvtalRepository;
import se.inera.webcert.persistence.privatlakaravtal.repository.GodkantAvtalRepository;

/**
 * Created by eriklupander on 2015-08-05.
 */
@RunWith(MockitoJUnitRunner.class)
public class AvtalServiceTest {

    private static final String USER_ID = "userId";
    private static final Integer AVTAL_VERSION_1 = 1;
    private static final Integer AVTAL_VERSION_2 = 2;

    @Mock
    AvtalRepository avtalRepository;

    @Mock
    GodkantAvtalRepository godkantAvtalRepository;

    @InjectMocks
    AvtalServiceImpl avtalService;

    @Test
    public void testGetLatestAvtal() {
        when(avtalRepository.getLatestAvtalVersion()).thenReturn(AVTAL_VERSION_1);
        when(avtalRepository.findOne(AVTAL_VERSION_1)).thenReturn(buildAvtal(AVTAL_VERSION_1));
        Avtal avtal = avtalService.getLatestAvtal();
        assertEquals(AVTAL_VERSION_1, avtal.getAvtalVersion());
        assertEquals("TEXT", avtal.getAvtalText());
    }


    @Test
    public void testUserHasApprovedLatestAvtal() {
        when(avtalRepository.getLatestAvtalVersion()).thenReturn(AVTAL_VERSION_1);
        when(godkantAvtalRepository.userHasApprovedAvtal(USER_ID, AVTAL_VERSION_1)).thenReturn(true);
        boolean approved = avtalService.userHasApprovedLatestAvtal(USER_ID);
        assertTrue(approved);
    }

    @Test
    public void testUserHasApprovedOldAvtal() {
        when(avtalRepository.getLatestAvtalVersion()).thenReturn(AVTAL_VERSION_2);
        when(godkantAvtalRepository.userHasApprovedAvtal(USER_ID, AVTAL_VERSION_1)).thenReturn(false);
        boolean approved = avtalService.userHasApprovedLatestAvtal(USER_ID);
        assertFalse(approved);
    }

    @Test
    public void testApproveAvtal() {
        when(avtalRepository.getLatestAvtalVersion()).thenReturn(AVTAL_VERSION_1);
        avtalService.approveLatestAvtal(USER_ID);
        verify(godkantAvtalRepository, times(1)).approveAvtal(anyString(), anyInt());
    }


    private Avtal buildAvtal(Integer avtalVersion) {
        Avtal avtal = new Avtal();
        avtal.setAvtalVersion(avtalVersion);
        avtal.setAvtalText("TEXT");
        return avtal;
    }

}

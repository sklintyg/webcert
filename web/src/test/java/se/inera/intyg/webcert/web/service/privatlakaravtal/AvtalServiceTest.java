/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.service.privatlakaravtal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.privatlakaravtal.model.Avtal;
import se.inera.intyg.webcert.persistence.privatlakaravtal.repository.AvtalRepository;
import se.inera.intyg.webcert.persistence.privatlakaravtal.repository.GodkantAvtalRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2015-08-05.
 */
@RunWith(MockitoJUnitRunner.class)
public class AvtalServiceTest {

    private static final String USER_ID = "userId";
    private static final String PERSON_ID = "personId";
    private static final Integer AVTAL_VERSION_1 = 1;
    private static final Integer AVTAL_VERSION_2 = 2;

    @Mock
    private AvtalRepository avtalRepository;

    @Mock
    private GodkantAvtalRepository godkantAvtalRepository;

    @Mock
    private MonitoringLogService monitoringLogService;

    @InjectMocks
    private AvtalServiceImpl avtalService;

    @Test
    public void testGetLatestAvtal() {
        when(avtalRepository.getLatestAvtalVersion()).thenReturn(AVTAL_VERSION_1);
        when(avtalRepository.findOne(AVTAL_VERSION_1)).thenReturn(buildAvtal(AVTAL_VERSION_1));
        Optional<Avtal> avtal = avtalService.getLatestAvtal();
        assertEquals(AVTAL_VERSION_1, avtal.get().getAvtalVersion());
        assertEquals("TEXT", avtal.get().getAvtalText());
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
        boolean approved = avtalService.userHasApprovedLatestAvtal(USER_ID);
        assertFalse(approved);
    }

    @Test
    public void testApproveAvtal() {
        when(avtalRepository.getLatestAvtalVersion()).thenReturn(AVTAL_VERSION_1);
        avtalService.approveLatestAvtal(USER_ID, PERSON_ID);
        verify(godkantAvtalRepository, times(1)).approveAvtal(anyString(), anyInt());
        verify(monitoringLogService, times(1)).logPrivatePractitionerTermsApproved(anyString(), isNull(), anyInt());
    }

    @Test(expected = IllegalStateException.class)
    public void testApproveAvtalNoAvtalInDB() {
        when(avtalRepository.getLatestAvtalVersion()).thenReturn(-1);
        try {
            avtalService.approveLatestAvtal(USER_ID, PERSON_ID);
        } catch (Exception e) {
            verify(godkantAvtalRepository, times(0)).approveAvtal(anyString(), anyInt());
            verify(monitoringLogService, times(0)).logPrivatePractitionerTermsApproved(anyString(), any(Personnummer.class), anyInt());
            throw e;
        }
    }

    private Avtal buildAvtal(Integer avtalVersion) {
        Avtal avtal = new Avtal();
        avtal.setAvtalVersion(avtalVersion);
        avtal.setAvtalText("TEXT");
        return avtal;
    }

}

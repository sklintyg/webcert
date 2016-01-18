/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.integration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.webcert.persistence.privatlakaravtal.model.Avtal;
import se.inera.intyg.webcert.web.service.privatlakaravtal.AvtalService;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionertermsresponder.v1.GetPrivatePractitionerTermsResponseType;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionertermsresponder.v1.GetPrivatePractitionerTermsType;
import se.riv.infrastructure.directory.privatepractitioner.terms.v1.ResultCodeEnum;

/**
 * Created by eriklupander on 2015-08-06.
 */
@RunWith(MockitoJUnitRunner.class)
public class GetPrivatePractitionerTermsResponderImplTest {

    private static final String AVTAL_TEXT = "Avtalstext";
    private static final String AVTAL_DATE = "2015-09-30T14:24:00.000";

    @Mock
    private AvtalService avtalService;

    @InjectMocks
    private GetPrivatePractitionerTermsResponderImpl testee;

    @Test
    public void testGetAvtal() {
        when(avtalService.getLatestAvtal()).thenReturn(buildAvtal(1));
        GetPrivatePractitionerTermsType request = new GetPrivatePractitionerTermsType();
        GetPrivatePractitionerTermsResponseType response = testee.getPrivatePractitionerTerms("", request);
        assertEquals(AVTAL_TEXT, response.getAvtal().getAvtalText());
        assertEquals(AVTAL_DATE, response.getAvtal().getAvtalVersionDatum().toString());
        assertEquals(1, response.getAvtal().getAvtalVersion());
    }

    @Test
    public void testGetAvtalNoAvtalExists() {
        when(avtalService.getLatestAvtal()).thenReturn(null);
        GetPrivatePractitionerTermsType request = new GetPrivatePractitionerTermsType();
        GetPrivatePractitionerTermsResponseType response = testee.getPrivatePractitionerTerms("", request);
        assertEquals(ResultCodeEnum.ERROR, response.getResultCode());
    }

    private Avtal buildAvtal(int version) {
        Avtal avtal = new Avtal();
        avtal.setAvtalVersion(version);
        avtal.setAvtalText(AVTAL_TEXT);
        avtal.setVersionDatum(LocalDateTime.parse(AVTAL_DATE));
        return avtal;
    }
}

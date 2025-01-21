/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.interactions.getcertificateadditions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.GetCertificateAdditionsResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateadditions.v1.GetCertificateAdditionsType;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.arende.ArendeService;

/**
 * @author Magnus Ekstrand on 2019-05-16.
 */
@RunWith(MockitoJUnitRunner.class)
public class GetCertificateAdditionsResponderImplTest {

    private static final List<Long> ARENDE_IDS = Arrays.asList(1234567L, 2345678L, 3456789L);

    private static final List<String> INTYG_IDS = Arrays.asList("ABC123", "DEF456", "GHI789");

    @Mock
    private ArendeService arendeService;

    @InjectMocks
    GetCertificateAdditionsResponderImpl testee;

    @Test
    public void whenGettingKompletteringarSuccessfully() {
        when(arendeService.getArendenExternal(INTYG_IDS)).thenReturn(mockKompetteringar());

        GetCertificateAdditionsResponseType additions = testee.getCertificateAdditions("", buildRequest());
        assertEquals(3, additions.getAdditions().size());
        assertEquals(1, additions.getAdditions().get(0).getAddition().size());
        assertEquals(1, additions.getAdditions().get(1).getAddition().size());
        assertEquals(1, additions.getAdditions().get(2).getAddition().size());
    }

    @Test
    public void whenThereAreNoKompletteringar() {
        when(arendeService.getArendenExternal(INTYG_IDS)).thenReturn(new ArrayList<>());

        GetCertificateAdditionsResponseType additions = testee.getCertificateAdditions("", buildRequest());
        assertEquals(3, additions.getAdditions().size());
        assertEquals(0, additions.getAdditions().get(0).getAddition().size());
        assertEquals(0, additions.getAdditions().get(1).getAddition().size());
        assertEquals(0, additions.getAdditions().get(2).getAddition().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenInvalidRequest() {
        testee.getCertificateAdditions("", buildEmptyRequest());
    }

    private GetCertificateAdditionsType buildEmptyRequest() {
        GetCertificateAdditionsType request = new GetCertificateAdditionsType();
        request.getIntygsId().addAll(new ArrayList<>());
        return request;
    }

    private GetCertificateAdditionsType buildRequest() {
        List<IntygId> identities = INTYG_IDS.stream()
            .map(s -> {
                IntygId intygId = new IntygId();
                intygId.setRoot("some-root-value");
                intygId.setExtension(s);
                return intygId;
            })
            .collect(Collectors.toList());

        GetCertificateAdditionsType request = new GetCertificateAdditionsType();
        request.getIntygsId().addAll(identities);

        return request;
    }

    private List<Arende> mockKompetteringar() {
        List<Arende> arenden = new ArrayList<>();

        for (int i = 0; i < ARENDE_IDS.size(); i++) {
            Arende arende = new Arende();
            arende.setId(ARENDE_IDS.get(i));
            arende.setStatus(Status.PENDING_INTERNAL_ACTION);
            arende.setTimestamp(LocalDateTime.now());
            arende.setIntygsId(INTYG_IDS.get(i));
            arende.setAmne(ArendeAmne.KOMPLT);

            arenden.add(arende);
        }

        return arenden;
    }

}

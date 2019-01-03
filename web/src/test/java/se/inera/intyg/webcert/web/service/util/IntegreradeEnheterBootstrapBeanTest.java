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
package se.inera.intyg.webcert.web.service.util;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import se.inera.intyg.webcert.persistence.integreradenhet.model.IntegreradEnhet;
import se.inera.intyg.webcert.persistence.integreradenhet.repository.IntegreradEnhetRepository;

@RunWith(MockitoJUnitRunner.class)
public class IntegreradeEnheterBootstrapBeanTest {

    @Mock
    private IntegreradEnhetRepository integreradEnhetRepository;

    @InjectMocks
    private IntegreradeEnheterBootstrapBean bootstrapBean;

    @Test
    public void testInitDataRequiredFields() {
        bootstrapBean.initData();
        ArgumentCaptor<IntegreradEnhet> enhetCaptor = ArgumentCaptor.forClass(IntegreradEnhet.class);
        verify(integreradEnhetRepository, atLeastOnce()).save(enhetCaptor.capture());

        for (IntegreradEnhet enhet : enhetCaptor.getAllValues()) {
            assertNotNull(enhet.getEnhetsId());
            assertNotNull(enhet.getEnhetsNamn());
            assertNotNull(enhet.getVardgivarId());
            assertNotNull(enhet.getVardgivarNamn());
       }
    }
}

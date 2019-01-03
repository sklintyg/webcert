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
package se.inera.intyg.webcert.notification_sender.notifications.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.notification_sender.notifications.helper.NotificationTestHelper;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2017-08-18.
 */
@RunWith(MockitoJUnitRunner.class)
public class NotificationPatientEnricherTest {

    @Mock
    private PUService puService;

    @InjectMocks
    private NotificationPatientEnricher testee;

    @Test
    public void testFk7263IsNotEnriched() throws TemporaryException {
        testee.enrichWithPatient(buildIntyg("fk7263"));
        verifyZeroInteractions(puService);
    }

    @Test(expected = TemporaryException.class)
    public void testExceptionIsThrownWhenPuInvocationFails() throws TemporaryException {
        when(puService.getPerson(any(Personnummer.class)))
                .thenReturn(PersonSvar.error());
        try {
            testee.enrichWithPatient(buildIntyg("lisjp"));
        } catch (Exception e) {
            verify(puService, times(1)).getPerson(any(Personnummer.class));
            throw e;
        }
    }

    @Test
    public void testLuaeFsIsEnriched() throws TemporaryException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar(false));
        Intyg intyg = buildIntyg("luae_fs");
        testee.enrichWithPatient(intyg);
        verify(puService, times(1)).getPerson(any(Personnummer.class));

        Patient p = intyg.getPatient();
        assertEquals("Tolvan", p.getFornamn());
        assertEquals("Mellis", p.getMellannamn());
        assertEquals("Tolvansson", p.getEfternamn());
        assertEquals("Tolvgatan 12", p.getPostadress());
        assertEquals("12121", p.getPostnummer());
        assertEquals("Tolvhult", p.getPostort());
    }

    @Test
    public void testSekretessmarkeradPatientIsNotUpdated() throws TemporaryException {
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar(true));
        Intyg intyg = buildIntyg("luae_fs");
        testee.enrichWithPatient(intyg);
        verify(puService, times(1)).getPerson(any(Personnummer.class));

        Patient p = intyg.getPatient();

        assertEquals("191212121212", p.getPersonId().getExtension());
        assertEquals("", p.getFornamn());
        assertEquals("", p.getMellannamn());
        assertEquals("Sekretessmarkering", p.getEfternamn());
        assertEquals("", p.getPostadress());
        assertEquals("", p.getPostnummer());
        assertEquals("", p.getPostort());
    }

    private Intyg buildIntyg(String intygsTyp) {
        Intyg intyg = new Intyg();
        TypAvIntyg typAvIntyg = new TypAvIntyg();
        typAvIntyg.setCode(intygsTyp);
        intyg.setTyp(typAvIntyg);
        intyg.setPatient(NotificationTestHelper.buildPatient());
        return intyg;
    }


    private PersonSvar buildPersonSvar(boolean sekretessmarkering) {
        return PersonSvar.found(NotificationTestHelper.buildPerson(sekretessmarkering));
    }

}

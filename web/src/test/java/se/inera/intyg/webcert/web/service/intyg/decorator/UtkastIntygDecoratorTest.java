/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.intyg.decorator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateMetaData;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2015-06-23.
 */
@RunWith(MockitoJUnitRunner.class)
public class UtkastIntygDecoratorTest {

    private static final String INTYG_JSON = "A bit of text representing json";
    private static final String INTYG_TYPE = "fk7263";

    private static final String INTYG_ID = "123";

    private Optional<Utkast> signedUtkast;

    @Mock
    private UtkastRepository utkastRepository;

    @InjectMocks
    private UtkastIntygDecoratorImpl testee;

    @Before
    public void setup() {
        HoSPersonal person = buildHosPerson();
        VardpersonReferens vardperson = buildVardpersonReferens(person);

        signedUtkast = buildUtkast(INTYG_ID, INTYG_TYPE, UtkastStatus.SIGNED, INTYG_JSON, vardperson);
    }

    @Test
    public void testNotAWebcertIntygDoesNotAddAnyStatuses() {
        when(utkastRepository.findById(any())).thenReturn(Optional.empty());

        CertificateResponse response = buildCertificateResponse();

        testee.decorateWithUtkastStatus(response);
        assertEquals(1, response.getMetaData().getStatus().size());
    }

    @Test
    public void testRevokedStatusOnIntygDoesNotAddAnyStatuses() {

        CertificateResponse response = buildCertificateResponse();
        response.getMetaData().getStatus().add(new Status(CertificateState.CANCELLED, "FKASSA", LocalDateTime.now()));

        testee.decorateWithUtkastStatus(response);
        assertEquals(2, response.getMetaData().getStatus().size());
    }

    @Test
    public void testRevokedIntygDoesNotAddAnyStatuses() {

        CertificateResponse response = buildCertificateResponse();
        CertificateResponse revokedResponse = new CertificateResponse(response.getInternalModel(), response.getUtlatande(), response.getMetaData(),
                true);

        testee.decorateWithUtkastStatus(revokedResponse);
        assertEquals(1, response.getMetaData().getStatus().size());
    }

    @Test
    public void testSentIntygDoesNotAddAnySentStatus() {

        CertificateResponse response = buildCertificateResponse();
        response.getMetaData().getStatus().add(new Status(CertificateState.SENT, "FKASSA", LocalDateTime.now()));

        testee.decorateWithUtkastStatus(response);
        assertEquals(2, response.getMetaData().getStatus().size());
    }

    @Test
    public void testSentIntygWithRevokedUtkastDoesAddsRevokedStatus() {
        signedUtkast.get().setSkickadTillMottagareDatum(LocalDateTime.now());
        signedUtkast.get().setAterkalladDatum(LocalDateTime.now());
        when(utkastRepository.findById(isNull())).thenReturn(signedUtkast);
        CertificateResponse response = buildCertificateResponse();
        response.getMetaData().getStatus().add(new Status(CertificateState.SENT, "FKASSA", LocalDateTime.now()));

        testee.decorateWithUtkastStatus(response);
        assertEquals(3, response.getMetaData().getStatus().size());
    }

    @Test
    public void testSentStatusIsAddedFromUtkast() {
        signedUtkast.get().setSkickadTillMottagareDatum(LocalDateTime.now());
        when(utkastRepository.findById(isNull())).thenReturn(signedUtkast);

        CertificateResponse response = buildCertificateResponse();

        testee.decorateWithUtkastStatus(response);

        assertEquals(2, response.getMetaData().getStatus().size());
        assertEquals(CertificateState.RECEIVED, response.getMetaData().getStatus().get(0).getType());
        assertEquals(CertificateState.SENT, response.getMetaData().getStatus().get(1).getType());
    }

    @Test
    public void testRevokedStatusIsAddedFromUtkast() {
        signedUtkast.get().setSkickadTillMottagareDatum(LocalDateTime.now());
        signedUtkast.get().setAterkalladDatum(LocalDateTime.now());
        when(utkastRepository.findById(isNull())).thenReturn(signedUtkast);

        CertificateResponse response = buildCertificateResponse();

        testee.decorateWithUtkastStatus(response);

        assertEquals(3, response.getMetaData().getStatus().size());
        assertEquals(CertificateState.RECEIVED, response.getMetaData().getStatus().get(0).getType());
        assertEquals(CertificateState.SENT, response.getMetaData().getStatus().get(1).getType());
        assertEquals(CertificateState.CANCELLED, response.getMetaData().getStatus().get(2).getType());
    }

    private CertificateResponse buildCertificateResponse() {
        CertificateResponse response = new CertificateResponse("{}", null, buildCertificateMetaData(), false);
        return response;
    }

    private CertificateMetaData buildCertificateMetaData() {
        CertificateMetaData metaData = new CertificateMetaData();
        metaData.setStatus(new ArrayList<Status>());
        Status statusSigned = new Status(CertificateState.RECEIVED, "FKASSA", LocalDateTime.now());
        metaData.getStatus().add(statusSigned);
        return metaData;
    }

    private HoSPersonal buildHosPerson() {
        HoSPersonal person = new HoSPersonal();
        person.setPersonId("AAA");
        person.setFullstandigtNamn("Dr Dengroth");
        return person;
    }

    private Optional<Utkast> buildUtkast(String intygId, String type, UtkastStatus status, String model, VardpersonReferens vardperson) {

        Utkast intyg = new Utkast();
        intyg.setIntygsId(intygId);
        intyg.setIntygsTyp(type);
        intyg.setStatus(status);
        intyg.setModel(model);
        intyg.setSkapadAv(vardperson);
        intyg.setSenastSparadAv(vardperson);

        return Optional.of(intyg);
    }

    private VardpersonReferens buildVardpersonReferens(HoSPersonal person) {
        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId(person.getPersonId());
        vardperson.setNamn(person.getFullstandigtNamn());
        return vardperson;
    }

}

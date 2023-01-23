/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.converter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.model.common.internal.*;
import se.inera.intyg.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.infra.integration.hsatk.services.HsatkEmployeeService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderException;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType.Komplettering;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType.SkickatAv;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Part;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.v3.MeddelandeReferens;

import javax.xml.ws.WebServiceException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ArendeConverterTest {

    private static final String FRAGESTALLARKOD_FORSAKRINGSKASSA = "FK";
    private static final String PARTKOD_FKASSA = "FKASSA";

    @Mock
    HsatkEmployeeService hsaEmployeeService;

    @Test
    public void testConvertArende() {
        final ArendeAmne amneskod = ArendeAmne.AVSTMN;
        final String intygId = "intygId";
        final String kontaktInfo = "kontaktInfo";
        final String skickatAv = PARTKOD_FKASSA;
        final String frageId = "frageId";
        final Integer instans = 1;
        final String kompletteringsText = "kompletteringsText";
        final String meddelande = "meddelande";
        final String meddelandeId = "meddelandeId";
        final String paminnelseMeddelandeId = "paminnelseMeddelandeId";
        final String personId = "personId";
        final String referensId = "referensId";
        final String rubrik = "rubrik";
        final LocalDate sistaDatum = LocalDate.now();
        final LocalDateTime skickatTidpunkt = LocalDateTime.now();
        final String svarPa = "svarPa";
        final String svarReferensId = "svarReferensId";
        SendMessageToCareType input = createSendMessageToCare(amneskod.name(), intygId, kontaktInfo, skickatAv, frageId, instans,
            kompletteringsText,
            meddelande, meddelandeId, paminnelseMeddelandeId, personId, referensId, rubrik, sistaDatum, skickatTidpunkt, svarPa,
            svarReferensId);
        Arende res = ArendeConverter.convert(input);
        assertEquals(amneskod, res.getAmne());
        assertEquals(intygId, res.getIntygsId());
        assertEquals(kontaktInfo, res.getKontaktInfo().get(0));
        assertEquals(FRAGESTALLARKOD_FORSAKRINGSKASSA, res.getSkickatAv());
        assertEquals(frageId, res.getKomplettering().get(0).getFrageId());
        assertEquals(instans, res.getKomplettering().get(0).getInstans());
        assertEquals(kompletteringsText, res.getKomplettering().get(0).getText());
        assertEquals(meddelande, res.getMeddelande());
        assertEquals(meddelandeId, res.getMeddelandeId());
        assertEquals(paminnelseMeddelandeId, res.getPaminnelseMeddelandeId());
        assertEquals(personId, res.getPatientPersonId());
        assertEquals(referensId, res.getReferensId());
        assertEquals(rubrik, res.getRubrik());
        assertEquals(sistaDatum, res.getSistaDatumForSvar());
        assertEquals(skickatTidpunkt, res.getSkickatTidpunkt());
        assertEquals(svarPa, res.getSvarPaId());
        assertEquals(svarReferensId, res.getSvarPaReferens());
    }

    @Test
    public void testDecorateArendeFromUtkast() throws WebCertServiceException {
        final String intygTyp = "intygTyp";
        final String signeratAv = "signeratAv";
        final String enhetId = "enhetId";
        final String enhetName = "enhetName";
        final String vardgivareName = "vardgivareName";
        final LocalDateTime now = LocalDateTime.now();
        final String givenName = "Test";
        final String surname = "Testorsson Svensson";

        Utkast utkast = new Utkast();
        utkast.setIntygsTyp(intygTyp);
        utkast.setEnhetsId(enhetId);
        utkast.setEnhetsNamn(enhetName);
        utkast.setVardgivarNamn(vardgivareName);
        utkast.setSignatur(mock(Signatur.class));
        when(utkast.getSignatur().getSigneradAv()).thenReturn(signeratAv);
        when(hsaEmployeeService.getEmployee(eq(null), eq(signeratAv))).thenReturn(createHsaResponse(givenName, surname));

        Arende res = new Arende();
        ArendeConverter.decorateArendeFromUtkast(res, utkast, now, hsaEmployeeService);

        assertNotNull(res);
        assertEquals(now, res.getTimestamp());
        assertEquals(now, res.getSenasteHandelse());
        assertEquals(Boolean.FALSE, res.getVidarebefordrad());
        assertEquals(Status.PENDING_INTERNAL_ACTION, res.getStatus());
        assertEquals(intygTyp, res.getIntygTyp());
        assertEquals(signeratAv, res.getSigneratAv());
        assertEquals(enhetId, res.getEnhetId());
        assertEquals(enhetName, res.getEnhetName());
        assertEquals(vardgivareName, res.getVardgivareName());
        assertEquals("Test Testorsson Svensson", res.getSigneratAvName());
    }

    @Test
    public void testDecorateArendeFromUtkastNoGivenName() throws WebCertServiceException {
        final String intygId = "intygsid";
        final String intygTyp = "intygTyp";
        final String signeratAv = "signeratAv";
        final String givenName = null;
        final String surname = "Testorsson Svensson";

        Arende arende = new Arende();
        arende.setIntygsId(intygId);

        Utkast utkast = new Utkast();
        utkast.setIntygsTyp(intygTyp);
        utkast.setSignatur(mock(Signatur.class));
        when(utkast.getSignatur().getSigneradAv()).thenReturn(signeratAv);
        when(hsaEmployeeService.getEmployee(eq(null), eq(signeratAv))).thenReturn(createHsaResponse(givenName, surname));

        Arende res = new Arende();
        ArendeConverter.decorateArendeFromUtkast(res, utkast, LocalDateTime.now(), hsaEmployeeService);

        assertNotNull(res);
        assertEquals("Testorsson Svensson", res.getSigneratAvName());
    }

    @Test
    public void testDecorateArendeFromUtkastHsaNotResponding() {
        Utkast utkast = new Utkast();
        utkast.setIntygsTyp("intygstyp");
        utkast.setEnhetsId("enhetsid");
        utkast.setSignatur(mock(Signatur.class));
        when(utkast.getSignatur().getSigneradAv()).thenReturn("signeratav");
        when(hsaEmployeeService.getEmployee(eq(null), anyString())).thenThrow(new WebServiceException());
        try {
            ArendeConverter.decorateArendeFromUtkast(new Arende(), utkast, LocalDateTime.now(), hsaEmployeeService);
            fail("Should throw");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM, e.getErrorCode());
        }
    }

    @Test
    public void testDecorateArendeFromUtkastHsaNotGivingName() {
        Utkast utkast = new Utkast();
        utkast.setIntygsTyp("intygstyp");
        utkast.setEnhetsId("enhetsid");
        utkast.setSignatur(mock(Signatur.class));
        when(utkast.getSignatur().getSigneradAv()).thenReturn("signeratav");
        when(hsaEmployeeService.getEmployee(eq(null), anyString())).thenReturn(createHsaResponse(null, null));
        try {
            ArendeConverter.decorateArendeFromUtkast(new Arende(), utkast, LocalDateTime.now(), hsaEmployeeService);
            fail("Should throw");
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, e.getErrorCode());
        }
    }

    @Test
    public void testDecorateMessageFromCertificatet() throws WebCertServiceException {
        final String certificateType = "certificateType";
        final String signedBy = "signedBy";
        final String unitId = "unitId";
        final String unitName = "unitName";
        final String careProviderName = "careProviderName";
        final LocalDateTime now = LocalDateTime.now();
        final String fullName = "Test Testorsson Svensson";

        final var certificate = mock(Utlatande.class);
        doReturn(certificateType).when(certificate).getTyp();
        final var basicData = mock(GrundData.class);
        doReturn(basicData).when(certificate).getGrundData();
        final var createdBy = mock(HoSPersonal.class);
        doReturn(createdBy).when(basicData).getSkapadAv();
        doReturn(signedBy).when(createdBy).getPersonId();
        doReturn(fullName).when(createdBy).getFullstandigtNamn();
        final var unit = mock(Vardenhet.class);
        doReturn(unit).when(createdBy).getVardenhet();
        doReturn(unitId).when(unit).getEnhetsid();
        doReturn(unitName).when(unit).getEnhetsnamn();
        final var careProvider = mock(Vardgivare.class);
        doReturn(careProvider).when(unit).getVardgivare();
        doReturn(careProviderName).when(careProvider).getVardgivarnamn();

        Arende actualMessage = new Arende();
        ArendeConverter.decorateMessageFromCertificate(actualMessage, certificate, now);

        assertNotNull(actualMessage);
        assertEquals(now, actualMessage.getTimestamp());
        assertEquals(now, actualMessage.getSenasteHandelse());
        assertEquals(Boolean.FALSE, actualMessage.getVidarebefordrad());
        assertEquals(Status.PENDING_INTERNAL_ACTION, actualMessage.getStatus());
        assertEquals(certificateType, actualMessage.getIntygTyp());
        assertEquals(signedBy, actualMessage.getSigneratAv());
        assertEquals(unitId, actualMessage.getEnhetId());
        assertEquals(unitName, actualMessage.getEnhetName());
        assertEquals(careProviderName, actualMessage.getVardgivareName());
        assertEquals(fullName, actualMessage.getSigneratAvName());
    }

    @Test
    public void testCreateArendeQuestionFromUtkast() throws CertificateSenderException {
        final ArendeAmne amne = ArendeAmne.OVRIGT;
        final String enhetsId = "enhetsId";
        final String intygsId = "intygsId";
        final String intygsTyp = "luse";
        final String meddelande = "meddelande";
        final String patientPersonId = "191212121212";
        final String rubrik = "rubrik";
        final String signeratAv = "hsa123";
        final String givenName = "givenname";
        final String surname = "surname";
        final String vardaktorName = "vardaktor namn";
        final String enhetName = "enhet namn";
        final String vardgivareName = "vardgivare namn";
        final LocalDateTime now = LocalDateTime.now();
        Utkast utkast = new Utkast();
        utkast.setEnhetsId(enhetsId);
        utkast.setEnhetsNamn(enhetName);
        utkast.setVardgivarNamn(vardgivareName);
        utkast.setIntygsId(intygsId);
        utkast.setIntygsTyp(intygsTyp);
        utkast.setPatientPersonnummer(Personnummer.createPersonnummer(patientPersonId).get());
        utkast.setSignatur(mock(Signatur.class));
        when(utkast.getSignatur().getSigneradAv()).thenReturn(signeratAv);
        when(hsaEmployeeService.getEmployee(null, signeratAv)).thenReturn(createHsaResponse(givenName, surname));

        Arende res = ArendeConverter.createArendeFromUtkast(amne, rubrik, meddelande, utkast, now, vardaktorName, hsaEmployeeService);

        assertNotNull(res);
        assertEquals(amne, res.getAmne());
        assertEquals(enhetsId, res.getEnhetId());
        assertEquals(enhetName, res.getEnhetName());
        assertEquals(vardgivareName, res.getVardgivareName());
        assertEquals(intygsId, res.getIntygsId());
        assertEquals(intygsTyp, res.getIntygTyp());
        assertEquals(meddelande, res.getMeddelande());
        assertNotNull(res.getMeddelandeId());
        assertNull(res.getPaminnelseMeddelandeId());
        assertEquals(patientPersonId, res.getPatientPersonId());
        assertNull(res.getReferensId());
        assertEquals(rubrik, res.getRubrik());
        assertEquals(now, res.getSenasteHandelse());
        assertEquals(now, res.getSkickatTidpunkt());
        assertEquals(now, res.getTimestamp());
        assertEquals(signeratAv, res.getSigneratAv());
        assertEquals(givenName + " " + surname, res.getSigneratAvName());
        assertNull(res.getSistaDatumForSvar());
        assertEquals(FrageStallare.WEBCERT.getKod(), res.getSkickatAv());
        assertEquals(Status.PENDING_EXTERNAL_ACTION, res.getStatus());
        assertNull(res.getSvarPaId());
        assertNull(res.getSvarPaReferens());
        assertEquals(Boolean.FALSE, res.getVidarebefordrad());
        assertEquals(vardaktorName, res.getVardaktorName());
    }

    @Test
    public void testCreateMessageFromCertificate() throws CertificateSenderException {
        final var subject = ArendeAmne.OVRIGT;
        final var unitId = "unitId";
        final var certificateId = "certificateId";
        final var certificateType = "certificateType";
        final var messageText = "messageText";
        final var patientPersonId = "191212121212";
        final var header = "header";
        final var signedBy = "signedBy";
        final var givenName = "Test";
        final var surname = "Testorsson Svensson";
        final var careGiverName = "Test Testorsson Svensson";
        final var unitName = "unitName";
        final var careProviderName = "careProviderName";
        final var now = LocalDateTime.now();

        Utkast utkast = new Utkast();
        utkast.setPatientPersonnummer(Personnummer.createPersonnummer(patientPersonId).get());

        final var certificate = mock(Utlatande.class);
        doReturn(certificateType).when(certificate).getTyp();
        doReturn(certificateId).when(certificate).getId();
        final var basicData = mock(GrundData.class);
        doReturn(basicData).when(certificate).getGrundData();
        final var patient = mock(Patient.class);
        doReturn(patient).when(basicData).getPatient();
        doReturn(Personnummer.createPersonnummer(patientPersonId).get()).when(patient).getPersonId();
        final var createdBy = mock(HoSPersonal.class);
        doReturn(createdBy).when(basicData).getSkapadAv();
        doReturn(signedBy).when(createdBy).getPersonId();
        final var unit = mock(Vardenhet.class);
        doReturn(unit).when(createdBy).getVardenhet();
        doReturn(unitId).when(unit).getEnhetsid();
        doReturn(unitName).when(unit).getEnhetsnamn();
        final var careProvider = mock(Vardgivare.class);
        doReturn(careProvider).when(unit).getVardgivare();
        doReturn(careProviderName).when(careProvider).getVardgivarnamn();

        when(hsaEmployeeService.getEmployee(null, signedBy)).thenReturn(createHsaResponse(givenName, surname));

        Arende res = ArendeConverter.createMessageFromCertificate(subject, header, messageText, certificate, now, careGiverName, hsaEmployeeService);

        assertNotNull(res);
        assertEquals(subject, res.getAmne());
        assertEquals(unitId, res.getEnhetId());
        assertEquals(unitName, res.getEnhetName());
        assertEquals(careProviderName, res.getVardgivareName());
        assertEquals(certificateId, res.getIntygsId());
        assertEquals(certificateType, res.getIntygTyp());
        assertEquals(messageText, res.getMeddelande());
        assertNotNull(res.getMeddelandeId());
        assertNull(res.getPaminnelseMeddelandeId());
        assertEquals(patientPersonId, res.getPatientPersonId());
        assertNull(res.getReferensId());
        assertEquals(header, res.getRubrik());
        assertEquals(now, res.getSenasteHandelse());
        assertEquals(now, res.getSkickatTidpunkt());
        assertEquals(now, res.getTimestamp());
        assertEquals(signedBy, res.getSigneratAv());
        assertEquals(givenName + " " + surname, res.getSigneratAvName());
        assertNull(res.getSistaDatumForSvar());
        assertEquals(FrageStallare.WEBCERT.getKod(), res.getSkickatAv());
        assertEquals(Status.PENDING_EXTERNAL_ACTION, res.getStatus());
        assertNull(res.getSvarPaId());
        assertNull(res.getSvarPaReferens());
        assertEquals(Boolean.FALSE, res.getVidarebefordrad());
        assertEquals(careGiverName, res.getVardaktorName());
    }

    @Test
    public void testCreateArendeAnswerFromQuestion() throws CertificateSenderException {
        final String nyttMeddelande = "nytt meddelande";
        final String meddelandeId = "meddelandeId";
        final ArendeAmne amne = ArendeAmne.KONTKT;
        final String enhetsId = "enhetsId";
        final String intygsId = "intygsId";
        final String intygsTyp = "luse";
        final String meddelande = "meddelande";
        final String patientPersonId = "191212121212";
        final String rubrik = "rubrik";
        final String signeratAv = "hsa123";
        final String signeratAvName = "givenname surname";
        final String referensId = "referensId";
        final String vardaktorName = "vardaktor namn";
        final String enhetName = "enhet namn";
        final String vardgivareName = "vardgivare namn";
        final LocalDateTime now = LocalDateTime.now();
        Arende arende = new Arende();
        arende.setMeddelandeId(meddelandeId);
        arende.setEnhetId(enhetsId);
        arende.setEnhetName(enhetName);
        arende.setVardgivareName(vardgivareName);
        arende.setIntygsId(intygsId);
        arende.setIntygTyp(intygsTyp);
        arende.setPatientPersonId(patientPersonId);
        arende.setSigneratAv(signeratAv);
        arende.setSigneratAvName(signeratAvName);
        arende.setRubrik(rubrik);
        arende.setMeddelande(meddelande);
        arende.setAmne(amne);
        arende.setReferensId(referensId);
        arende.setStatus(Status.PENDING_INTERNAL_ACTION);

        Arende res = ArendeConverter.createAnswerFromArende(nyttMeddelande, arende, now, vardaktorName);

        assertNotNull(res);
        assertEquals(amne, res.getAmne());
        assertEquals(enhetsId, res.getEnhetId());
        assertEquals(enhetName, res.getEnhetName());
        assertEquals(vardgivareName, res.getVardgivareName());
        assertEquals(intygsId, res.getIntygsId());
        assertEquals(intygsTyp, res.getIntygTyp());
        assertEquals(nyttMeddelande, res.getMeddelande());
        assertNotNull(res.getMeddelandeId());
        assertNull(res.getPaminnelseMeddelandeId());
        assertEquals(patientPersonId, res.getPatientPersonId());
        assertNull(res.getReferensId());
        assertEquals(rubrik, res.getRubrik());
        assertEquals(now, res.getSenasteHandelse());
        assertEquals(now, res.getSkickatTidpunkt());
        assertEquals(now, res.getTimestamp());
        assertEquals(signeratAv, res.getSigneratAv());
        assertEquals(signeratAvName, res.getSigneratAvName());
        assertNull(res.getSistaDatumForSvar());
        assertEquals(FrageStallare.WEBCERT.getKod(), res.getSkickatAv());
        assertEquals(Status.CLOSED, res.getStatus());
        assertEquals(meddelandeId, res.getSvarPaId());
        assertEquals(referensId, res.getSvarPaReferens());
        assertEquals(Boolean.FALSE, res.getVidarebefordrad());
        assertNotEquals(meddelandeId, res.getMeddelandeId());
        assertEquals(vardaktorName, res.getVardaktorName());
    }

    @Test
    public void getNamesByHsaIds() {
        String id1 = "not_found";
        String id2 = "foundId";
        final var givenName = "Test";
        final var surname = "Testorsson Svensson";

        List<String> hsaIds = Arrays.asList(id1, id2);

        when(hsaEmployeeService.getEmployee(eq(null), eq(id1))).thenThrow(WebServiceException.class);

        when(hsaEmployeeService.getEmployee(eq(null), eq(id2))).thenReturn(createHsaResponse(givenName, surname));

        Map<String, String> map = ArendeConverter.getNamesByHsaIds(hsaIds, hsaEmployeeService);

        assertNotNull(map);
        assertEquals(1, map.size());
        assertTrue(map.containsKey(id2));
        assertFalse(map.containsKey(id1));
    }

    private SendMessageToCareType createSendMessageToCare(String amneskod, String intygId, String kontaktInfo, String skickatAv,
        String frageId,
        Integer instans, String kompletteringsText, String meddelande, String meddelandeId, String paminnelseMeddelandeId,
        String personId,
        String referensId, String rubrik, LocalDate sistaDatum, LocalDateTime skickatTidpunkt, String svarPa, String svarReferensId) {
        SendMessageToCareType res = new SendMessageToCareType();

        Amneskod amne = new Amneskod();
        amne.setCode(amneskod);
        res.setAmne(amne);

        SkickatAv sa = new SkickatAv();
        sa.getKontaktInfo().add(kontaktInfo);
        Part part = new Part();
        part.setCode(skickatAv);
        sa.setPart(part);
        res.setSkickatAv(sa);

        Komplettering komplettering = new Komplettering();
        komplettering.setFrageId(frageId);
        komplettering.setInstans(instans);
        komplettering.setText(kompletteringsText);
        res.getKomplettering().add(komplettering);

        PersonId pid = new PersonId();
        pid.setExtension(personId);
        res.setPatientPersonId(pid);

        MeddelandeReferens mr = new MeddelandeReferens();
        mr.setMeddelandeId(svarPa);
        mr.setReferensId(svarReferensId);
        res.setSvarPa(mr);

        IntygId ii = new IntygId();
        ii.setExtension(intygId);
        res.setIntygsId(ii);

        res.setMeddelande(meddelande);
        res.setMeddelandeId(meddelandeId);
        res.setPaminnelseMeddelandeId(paminnelseMeddelandeId);
        res.setReferensId(referensId);
        res.setRubrik(rubrik);
        res.setSistaDatumForSvar(sistaDatum);
        res.setSkickatTidpunkt(skickatTidpunkt);

        return res;
    }

    private List<PersonInformation> createHsaResponse(String givenName, String middleAndSurname) {
        PersonInformation pit = new PersonInformation();
        pit.setGivenName(givenName);
        pit.setMiddleAndSurName(middleAndSurname);
        return Arrays.asList(new PersonInformation(), pit, new PersonInformation(), new PersonInformation());
    }
}

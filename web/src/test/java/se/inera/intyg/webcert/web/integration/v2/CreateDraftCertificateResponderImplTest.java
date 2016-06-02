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

package se.inera.intyg.webcert.web.integration.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.integration.hsa.services.HsaPersonService;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.support.api.exception.ExternalServiceCallException;
import se.inera.intyg.webcert.persistence.utkast.model.*;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;
import se.inera.intyg.webcert.web.integration.v2.builder.CreateNewDraftRequestBuilder;
import se.inera.intyg.webcert.web.integration.v2.validator.CreateDraftCertificateValidator;
import se.inera.intyg.webcert.web.integration.validator.ResultValidator;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v2.CreateDraftCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v2.CreateDraftCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v2.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v2.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v2.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.*;
import se.riv.clinicalprocess.healthcond.certificate.v2.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v2.Patient;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType;
import se.riv.infrastructure.directory.v1.CommissionType;

@RunWith(MockitoJUnitRunner.class)
public class CreateDraftCertificateResponderImplTest {

    private static final String LOGICAL_ADDR = "1234567890";

    private static final String USER_HSAID = "SE1234567890";
    private static final String UNIT_HSAID = "SE0987654321";
    private static final String CAREGIVER_HSAID = "SE0000112233";

    private static final String UTKAST_ID = "abc123";
    private static final String UTKAST_VERSION = "1";
    private static final String UTKAST_TYPE = "fk7263";
    private static final String UTKAST_JSON = "A bit of text representing json";

    @Mock
    private UtkastService mockUtkastService;

    @Mock
    private HsaPersonService mockHsaPersonService;

    @Mock
    private CreateNewDraftRequestBuilder mockRequestBuilder;

    @Mock
    private CreateDraftCertificateValidator mockValidator;

    @Mock
    private IntegreradeEnheterRegistry mockIntegreradeEnheterService;

    @Mock
    private MonitoringLogService mockMonitoringLogService;

    @InjectMocks
    private CreateDraftCertificateResponderImpl responder;

    @Test
    public void testCreateDraftCertificateSuccess() throws ExternalServiceCallException {
        List<CommissionType> miuList = Collections.singletonList(createMIU(USER_HSAID, UNIT_HSAID, LocalDateTime.now().plusYears(2)));
        CreateNewDraftRequest draftRequest = createCreateNewDraftRequest(createVardenhet(createVardgivare()));
        CreateDraftCertificateType certificateType = createCertificateType();

        VardpersonReferens vardperson = createVardpersonReferens(
                certificateType.getIntyg().getSkapadAv().getPersonalId().getRoot(),
                certificateType.getIntyg().getSkapadAv().getFullstandigtNamn());

        Utkast utkast = createUtkast(UTKAST_ID, Long.parseLong(UTKAST_VERSION), UTKAST_TYPE, UtkastStatus.DRAFT_INCOMPLETE, UTKAST_JSON, vardperson);

        when(mockValidator.validate(any(Intyg.class))).thenReturn(new ResultValidator());
        when(mockHsaPersonService.checkIfPersonHasMIUsOnUnit(USER_HSAID, UNIT_HSAID)).thenReturn(miuList);
        when(mockRequestBuilder.buildCreateNewDraftRequest(any(Intyg.class), any(CommissionType.class))).thenReturn(draftRequest);
        when(mockUtkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(utkast);

        CreateDraftCertificateResponseType response = responder.createDraftCertificate(LOGICAL_ADDR, certificateType);

        verify(mockUtkastService).createNewDraft(any(CreateNewDraftRequest.class));
        verify(mockIntegreradeEnheterService).putIntegreradEnhet(any(IntegreradEnhetEntry.class), eq(false), eq(true));

        assertNotNull(response);
        assertEquals(response.getResult().getResultCode(), ResultCodeType.OK);
        assertEquals(response.getIntygsId().getRoot(), UNIT_HSAID);
        assertEquals(response.getIntygsId().getExtension(), UTKAST_ID);
    }

    @Test
    public void testCreateDraftCertificateValidationError() {
        ResultValidator resultValidator = mock(ResultValidator.class);
        CreateDraftCertificateType certificateType = createCertificateType();

        when(resultValidator.hasErrors()).thenReturn(true);
        when(mockValidator.validate(any(Intyg.class))).thenReturn(resultValidator);

        CreateDraftCertificateResponseType response = responder.createDraftCertificate(LOGICAL_ADDR, certificateType);

        assertNotNull(response);
        assertEquals(response.getResult().getResultCode(), ResultCodeType.ERROR);
        assertEquals(response.getResult().getErrorId(), ErrorIdType.VALIDATION_ERROR);
    }

    @Test
    public void testCreateDraftCertificateNoMIUs() throws ExternalServiceCallException {
        List<CommissionType> miuList = new ArrayList<CommissionType>();
        CreateDraftCertificateType certificateType = createCertificateType();

        when(mockValidator.validate(any(Intyg.class))).thenReturn(new ResultValidator());
        when(mockHsaPersonService.checkIfPersonHasMIUsOnUnit(USER_HSAID, UNIT_HSAID)).thenReturn(miuList);

        CreateDraftCertificateResponseType response = responder.createDraftCertificate(LOGICAL_ADDR, certificateType);

        verify(mockMonitoringLogService).logMissingMedarbetarUppdrag(USER_HSAID, UNIT_HSAID);

        assertNotNull(response);
        assertEquals(response.getResult().getResultCode(), ResultCodeType.ERROR);
        assertEquals(response.getResult().getErrorId(), ErrorIdType.VALIDATION_ERROR);
    }

    @Test
    public void testCreateDraftCertificateMultipleMIUs() throws ExternalServiceCallException {
        List<CommissionType> miuList = Arrays.asList(createMIU(USER_HSAID, UNIT_HSAID, LocalDateTime.now().plusYears(2)),
                createMIU(USER_HSAID, UNIT_HSAID, LocalDateTime.now().plusYears(2)));
        CreateNewDraftRequest draftRequest = createCreateNewDraftRequest(createVardenhet(createVardgivare()));
        CreateDraftCertificateType certificateType = createCertificateType();

        VardpersonReferens vardperson = createVardpersonReferens(
                certificateType.getIntyg().getSkapadAv().getPersonalId().getRoot(),
                certificateType.getIntyg().getSkapadAv().getFullstandigtNamn());

        Utkast utkast = createUtkast(UTKAST_ID, Long.parseLong(UTKAST_VERSION), UTKAST_TYPE, UtkastStatus.DRAFT_INCOMPLETE, UTKAST_JSON, vardperson);

        when(mockValidator.validate(any(Intyg.class))).thenReturn(new ResultValidator());
        when(mockHsaPersonService.checkIfPersonHasMIUsOnUnit(USER_HSAID, UNIT_HSAID)).thenReturn(miuList);
        when(mockRequestBuilder.buildCreateNewDraftRequest(any(Intyg.class), any(CommissionType.class))).thenReturn(draftRequest);
        when(mockUtkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(utkast);

        CreateDraftCertificateResponseType response = responder.createDraftCertificate(LOGICAL_ADDR, certificateType);

        verify(mockUtkastService).createNewDraft(any(CreateNewDraftRequest.class));
        verify(mockIntegreradeEnheterService).putIntegreradEnhet(any(IntegreradEnhetEntry.class), eq(false), eq(true));

        assertNotNull(response);
        assertEquals(response.getResult().getResultCode(), ResultCodeType.OK);
        assertEquals(response.getIntygsId().getExtension(), UTKAST_ID);
    }

    @Test
    public void testCreateDraftCertificateVardenhetAlredyExistsInRegistry() throws ExternalServiceCallException {
        List<CommissionType> miuList = Arrays.asList(createMIU(USER_HSAID, UNIT_HSAID, LocalDateTime.now().plusYears(2)),
                createMIU(USER_HSAID, UNIT_HSAID, LocalDateTime.now().plusYears(2)));
        CreateNewDraftRequest draftRequest = createCreateNewDraftRequest(createVardenhet(createVardgivare()));
        CreateDraftCertificateType certificateType = createCertificateType();

        VardpersonReferens vardperson = createVardpersonReferens(
                certificateType.getIntyg().getSkapadAv().getPersonalId().getRoot(),
                certificateType.getIntyg().getSkapadAv().getFullstandigtNamn());

        Utkast utkast = createUtkast(UTKAST_ID, Long.parseLong(UTKAST_VERSION), UTKAST_TYPE, UtkastStatus.DRAFT_INCOMPLETE, UTKAST_JSON, vardperson);

        when(mockValidator.validate(any(Intyg.class))).thenReturn(new ResultValidator());
        when(mockHsaPersonService.checkIfPersonHasMIUsOnUnit(USER_HSAID, UNIT_HSAID)).thenReturn(miuList);
        when(mockRequestBuilder.buildCreateNewDraftRequest(any(Intyg.class), any(CommissionType.class))).thenReturn(draftRequest);
        when(mockUtkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(utkast);

        CreateDraftCertificateResponseType response = responder.createDraftCertificate(LOGICAL_ADDR, certificateType);

        verify(mockUtkastService).createNewDraft(any(CreateNewDraftRequest.class));
        verify(mockIntegreradeEnheterService).putIntegreradEnhet(any(IntegreradEnhetEntry.class), eq(false), eq(true));

        assertNotNull(response);
        assertEquals(response.getResult().getResultCode(), ResultCodeType.OK);
        assertEquals(response.getIntygsId().getExtension(), UTKAST_ID);
    }

    private VardpersonReferens createVardpersonReferens(String hsaId, String name) {
        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId(hsaId);
        vardperson.setNamn(name);
        return vardperson;
    }

    private CreateNewDraftRequest createCreateNewDraftRequest(Vardenhet vardenhet) {
        CreateNewDraftRequest draftRequest = new CreateNewDraftRequest();
        draftRequest.setIntygId(UTKAST_ID);
        draftRequest.setHosPerson(new HoSPersonal());
        draftRequest.getHosPerson().setVardenhet(vardenhet);
        return draftRequest;
    }

    private Vardenhet createVardenhet(Vardgivare vardgivare) {
        Vardenhet vardenhet = new Vardenhet();
        vardenhet.setEnhetsid("SE1234567890-1A01");
        vardenhet.setEnhetsnamn("Vardenheten");
        vardenhet.setVardgivare(vardgivare);
        return vardenhet;
    }

    private Vardgivare createVardgivare() {
        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setVardgivarid("SE1234567890-2B01");
        vardgivare.setVardgivarnamn("Vardgivaren");
        return vardgivare;
    }

    private CreateDraftCertificateType createCertificateType() {

        // Type
        TypAvIntyg utlTyp = new TypAvIntyg();
        utlTyp.setCode("fk7263");

        // HoSPerson
        HsaId userHsaId = new HsaId();
        userHsaId.setExtension(USER_HSAID);
        userHsaId.setRoot("USERHSAID");

        HsaId unitHsaId = new HsaId();
        unitHsaId.setExtension(UNIT_HSAID);
        unitHsaId.setRoot("UNITHSAID");

        Enhet hosEnhet = new Enhet();
        hosEnhet.setEnhetsId(unitHsaId);

        HosPersonal hosPerson = new HosPersonal();
        hosPerson.setFullstandigtNamn("Abel Baker");
        hosPerson.setPersonalId(userHsaId);
        hosPerson.setEnhet(hosEnhet);

        // Patient
        PersonId personId = new PersonId();
        personId.setRoot("PERSNR");
        personId.setExtension("19121212-1212");

        Patient patType = new Patient();
        patType.setPersonId(personId);
        patType.setFornamn("Adam");
        patType.setMellannamn("Cesarsson");
        patType.setEfternamn("Eriksson");

        Intyg utlatande = new Intyg();
        utlatande.setTypAvIntyg(utlTyp);
        utlatande.setSkapadAv(hosPerson);
        utlatande.setPatient(patType);

        CreateDraftCertificateType certificateType = new CreateDraftCertificateType();
        certificateType.setIntyg(utlatande);

        return certificateType;
    }

    private CommissionType createMIU(String personHsaId, String unitHsaId, LocalDateTime miuEndDate) {

        CommissionType miu = new CommissionType();
        miu.setHealthCareProviderHsaId(CAREGIVER_HSAID);
        miu.setHealthCareProviderName("Landstinget");
        miu.setHealthCareUnitName("Sjukhuset");
        miu.setHealthCareUnitHsaId(unitHsaId);
        miu.setHealthCareUnitEndDate(miuEndDate);
        miu.setCommissionHsaId(personHsaId);

        return miu;
    }

    private Utkast createUtkast(String intygId, long version, String type, UtkastStatus status, String model, VardpersonReferens vardperson) {

        Utkast utkast = new Utkast();
        utkast.setIntygsId(intygId);
        utkast.setVersion(version);
        utkast.setIntygsTyp(type);
        utkast.setStatus(status);
        utkast.setModel(model);
        utkast.setSkapadAv(vardperson);
        utkast.setSenastSparadAv(vardperson);

        return utkast;
    }

}

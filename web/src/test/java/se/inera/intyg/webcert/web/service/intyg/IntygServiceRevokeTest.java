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
package se.inera.intyg.webcert.web.service.intyg;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateMetaData;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessage;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacadeException;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations.FrontendRelations;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@RunWith(MockitoJUnitRunner.Silent.class)
public class IntygServiceRevokeTest extends AbstractIntygServiceTest {

    private static final String REVOKE_MSG = "This is revoked";
    private static final String REVOKE_REASON = "FELAKTIGT_INTYG";
    private static final String INTYG_JSON = "A bit of text representing json";
    private static final String INTYG_TYPE = "fk7263";
    private static final String INTYG_TYPE_VERSION = "1.0";
    private static final String HSA_ID = "AAA";

    private static final String INTYG_ID = "123";
    private static final String PARENT_INTYG_ID = "1234";

    private static final String USER_REFERENCE = "some-ref";

    private Utkast signedUtkast;
    private Relations childRelations;
    private Relations parentRelations;

    @Before
    public void setup() {
        HoSPersonal person = buildHosPerson();
        VardpersonReferens vardperson = buildVardpersonReferens(person);
        WebCertUser user = buildWebCertUser(person);

        signedUtkast = buildUtkast(INTYG_ID, INTYG_TYPE, INTYG_TYPE_VERSION, UtkastStatus.SIGNED, INTYG_JSON, vardperson);
        childRelations = buildRelations(false);
        parentRelations = buildRelations(true);

        when(webCertUserService.getUser()).thenReturn(user);
    }

    @Override
    @Before
    public void setupMocks() throws Exception {
        json = Files.readString(Path.of(ClassLoader.getSystemResource("IntygServiceTest/utlatande.json").toURI()));
        utlatande = objectMapper.readValue(json, Fk7263Utlatande.class);
        CertificateMetaData metaData = buildCertificateMetaData();
        certificateResponse = new CertificateResponse(json, utlatande, metaData, false);
        when(moduleFacade.getCertificate(any(String.class), any(String.class), anyString())).thenReturn(certificateResponse);
        when(intygRelationHelper.getRelationsForIntyg(anyString())).thenReturn(new Relations());

        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString(), anyString()))
            .thenReturn(buildPatient(false, false));
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);
        when(moduleApi.updateBeforeViewing(anyString(), any(Patient.class))).thenReturn("MODEL");
    }

    @Before
    public void setupDefaultAuthorization() {
        when(webCertUserService.isAuthorizedForUnit(anyString(), anyString(), eq(true))).thenReturn(true);
    }

    @Test
    public void testRevokeIntyg() throws Exception {

        when(logRequestFactory.createLogRequestFromUtlatande(any(Utlatande.class))).thenReturn(LogRequest.builder().build());
        when(intygRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(signedUtkast));

        final var analyticsMessage = CertificateAnalyticsMessage.builder().build();
        when(certificateAnalyticsMessageFactory.revoked(any(Utlatande.class))).thenReturn(analyticsMessage);

        // do the call
        IntygServiceResult res = intygService.revokeIntyg(INTYG_ID, INTYG_TYP_FK, REVOKE_MSG, REVOKE_REASON);

        // verify that services were called
        verify(arendeService).closeAllNonClosedQuestions(INTYG_ID);
        verify(notificationService, times(1)).sendNotificationForIntygRevoked(INTYG_ID);
        verify(logService).logRevokeIntyg(any(LogRequest.class));
        verify(intygRepository).save(any(Utkast.class));
        verify(certificateSenderService, times(1)).revokeCertificate(eq(INTYG_ID), any(), eq(INTYG_TYP_FK), eq(INTYG_TYPE_VERSION));
        verify(moduleFacade, times(1)).getRevokeCertificateRequest(eq(INTYG_TYP_FK), any(), any(), eq(REVOKE_MSG));
        verify(monitoringService).logIntygRevoked(INTYG_ID, INTYG_TYP_FK, HSA_ID, REVOKE_REASON);
        verify(publishCertificateAnalyticsMessage).publishEvent(analyticsMessage);

        assertEquals(IntygServiceResult.OK, res);
    }

    @Test(expected = WebCertServiceException.class)
    public void testRevokeIntygThatHasAlreadyBeenRevokedFails() throws IntygModuleFacadeException {
        when(intygRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(signedUtkast));
        when(moduleFacade.getCertificate(anyString(), anyString(), anyString())).thenThrow(new IntygModuleFacadeException(""));
        // Do the call
        try {
            intygService.revokeIntyg(INTYG_ID, INTYG_TYP_FK, REVOKE_MSG, REVOKE_REASON);
        } finally {
            verifyNoInteractions(certificateSenderService);
            verify(intygRepository, times(0)).save(any(Utkast.class));
            verifyNoInteractions(notificationService);
            verifyNoInteractions(logService);
        }
    }

    @Test
    public void testRevokeCompletedIntyg() throws Exception {

        when(logRequestFactory.createLogRequestFromUtlatande(any(Utlatande.class))).thenReturn(LogRequest.builder().build());
        when(intygRepository.findById(INTYG_ID)).thenReturn(Optional.ofNullable(signedUtkast));
        when(intygRelationHelper.getRelationsForIntyg(INTYG_ID)).thenReturn(childRelations);
        when(intygRelationHelper.getRelationsForIntyg(PARENT_INTYG_ID)).thenReturn(parentRelations);
        when(csIntegrationService.placeholderCertificateExists(INTYG_ID)).thenReturn(true);

        final var analyticsMessage = CertificateAnalyticsMessage.builder().build();
        when(certificateAnalyticsMessageFactory.revoked(any(Utlatande.class))).thenReturn(analyticsMessage);

        IntygServiceResult res = intygService.revokeIntyg(INTYG_ID, INTYG_TYP_FK, REVOKE_MSG, REVOKE_REASON);

        verify(arendeService).closeAllNonClosedQuestions(INTYG_ID);
        verify(arendeService).reopenClosedCompletions(PARENT_INTYG_ID);
        verify(notificationService, times(1)).sendNotificationForIntygRevoked(INTYG_ID);
        verify(logService).logRevokeIntyg(any(LogRequest.class));
        verify(intygRepository).save(any(Utkast.class));
        verify(certificateSenderService, times(1)).revokeCertificate(eq(INTYG_ID), any(), eq(INTYG_TYP_FK), eq(INTYG_TYPE_VERSION));
        verify(moduleFacade, times(1)).getRevokeCertificateRequest(eq(INTYG_TYP_FK), any(), any(), eq(REVOKE_MSG));
        verify(monitoringService).logIntygRevoked(INTYG_ID, INTYG_TYP_FK, HSA_ID, REVOKE_REASON);
        verify(csIntegrationService).placeholderCertificateExists(INTYG_ID);
        verify(csIntegrationService).revokePlaceholderCertificate(INTYG_ID);
        verify(publishCertificateAnalyticsMessage).publishEvent(analyticsMessage);

        assertEquals(IntygServiceResult.OK, res);
    }

    private HoSPersonal buildHosPerson() {
        HoSPersonal person = new HoSPersonal();
        person.setPersonId(HSA_ID);
        person.setFullstandigtNamn("Dr Dengroth");
        return person;
    }

    private Utkast buildUtkast(String intygId, String type, String intygTypeVersion, UtkastStatus status, String model,
        VardpersonReferens vardperson) {

        Utkast intyg = new Utkast();
        intyg.setIntygsId(intygId);
        intyg.setIntygsTyp(type);
        intyg.setIntygTypeVersion(intygTypeVersion);
        intyg.setStatus(status);
        intyg.setModel(model);
        intyg.setSkapadAv(vardperson);
        intyg.setSenastSparadAv(vardperson);

        return intyg;
    }

    private Relations buildRelations(boolean buildParent) {
        if (buildParent) {
            Relations relations = new Relations();
            FrontendRelations frontendRelations = new FrontendRelations();
            WebcertCertificateRelation webcertCertificateRelation = new WebcertCertificateRelation(INTYG_ID, RelationKod.KOMPLT,
                LocalDateTime.now(), UtkastStatus.SIGNED, false);
            frontendRelations.setComplementedByIntyg(webcertCertificateRelation);
            relations.setLatestChildRelations(frontendRelations);
            return relations;
        } else {
            Relations relations = new Relations();
            WebcertCertificateRelation webcertCertificateRelation = new WebcertCertificateRelation(PARENT_INTYG_ID, RelationKod.KOMPLT,
                LocalDateTime.now(), UtkastStatus.SIGNED, false);
            relations.setParent(webcertCertificateRelation);
            return relations;
        }
    }

    private VardpersonReferens buildVardpersonReferens(HoSPersonal person) {
        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId(person.getPersonId());
        vardperson.setNamn(person.getFullstandigtNamn());
        return vardperson;
    }

    private WebCertUser buildWebCertUser(HoSPersonal person) {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setOrigin(UserOriginType.DJUPINTEGRATION.name());
        user.setParameters(new IntegrationParameters(USER_REFERENCE, "", "", "", "", "", "", "", "", false, false, false, true, null));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));
        user.setNamn(person.getFullstandigtNamn());
        user.setHsaId(person.getPersonId());

        return user;
    }

}
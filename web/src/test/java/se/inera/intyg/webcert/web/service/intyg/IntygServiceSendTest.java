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
package se.inera.intyg.webcert.web.service.intyg;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.helpers.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientResponseType;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.integration.converter.util.ResultTypeUtil;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Relation;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateMetaData;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@RunWith(MockitoJUnitRunner.class)
public class IntygServiceSendTest extends AbstractIntygServiceTest {

    private static final String INTYG_TYPE_VERSION_1_0 = "1.0";

    @Before
    public void setupIntyg() throws Exception {
        json = FileUtils.getStringFromFile(new ClassPathResource("IntygServiceTest/utlatande.json").getFile());
        utlatande = objectMapper.readValue(json, Fk7263Utlatande.class);

        ReflectionTestUtils.setField(intygService, "sekretessmarkeringStartDatum", LocalDateTime.of(2016, 11, 30, 23, 0, 0, 0));
        when(intygRepository.findOne(eq(INTYG_ID))).thenReturn(getUtkast(INTYG_ID));
    }

    @Test
    public void testSendIntyg() throws Exception {
        final String completionMeddelandeId = "meddelandeId";

        WebCertUser webCertUser = createUser();

        Utlatande completionUtlatande = utlatande;
        completionUtlatande.getGrundData().setRelation(new Relation());
        completionUtlatande.getGrundData().getRelation().setRelationKod(RelationKod.KOMPLT);
        completionUtlatande.getGrundData().getRelation().setMeddelandeId(completionMeddelandeId);

        when(webCertUserService.isAuthorizedForUnit(anyString(), anyString(), anyBoolean())).thenReturn(true);

        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);
        when(webCertUserService.getUser()).thenReturn(webCertUser);
        when(intygRepository.findOne(INTYG_ID)).thenReturn(getUtkast(INTYG_ID));

        IntygServiceResult res = intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FKASSA", false);
        assertEquals(IntygServiceResult.OK, res);

        verify(logService).logSendIntygToRecipient(any(LogRequest.class));
        verify(certificateSenderService).sendCertificate(anyString(), any(Personnummer.class), anyString(), anyString(), eq(false));

        verify(intygRepository, times(2)).findOne(INTYG_ID);
        verify(intygRepository).save(any(Utkast.class));
    }

    @Test
    public void testSendIntygFailsForRevokedCertificate() throws Exception {

        final Utkast utkast = getUtkast(INTYG_ID);
        utkast.setAterkalladDatum(LocalDateTime.of(2018, 5, 5, 5, 5, 5, 5));

        when(intygRepository.findOne(eq(INTYG_ID))).thenReturn(utkast);

        json = FileUtils.getStringFromFile(new ClassPathResource("IntygServiceTest/utlatande.json").getFile());
        utlatande = objectMapper.readValue(json, Fk7263Utlatande.class);
        CertificateMetaData metaData = buildCertificateMetaData();
        certificateResponse = new CertificateResponse(json, utlatande, metaData, true);
        when(moduleFacade.getCertificate(any(String.class), any(String.class), anyString())).thenReturn(certificateResponse);

        WebCertUser webCertUser = createUser();
        when(webCertUserService.getUser()).thenReturn(webCertUser);
        when(webCertUserService.isAuthorizedForUnit(anyString(), anyString(), anyBoolean())).thenReturn(true);
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);

        Assertions.assertThatThrownBy(() -> intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FKASSA", false))
                .isExactlyInstanceOf(WebCertServiceException.class)
                .hasMessageEndingWith("cannot send a revoked certificate");
    }

    @Test
    public void testSendIntygFailsForReplacedCertificate() throws Exception {
        final String completionMeddelandeId = "meddelandeId";

        WebCertUser webCertUser = createUser();

        json = FileUtils.getStringFromFile(new ClassPathResource("IntygServiceTest/utlatande.json").getFile());
        utlatande = objectMapper.readValue(json, Fk7263Utlatande.class);
        utlatande.getGrundData().setRelation(new Relation());
        utlatande.getGrundData().getRelation().setRelationKod(RelationKod.ERSATT);
        CertificateMetaData metaData = buildCertificateMetaData();
        certificateResponse = new CertificateResponse(json, utlatande, metaData, false);

        WebcertCertificateRelation ersattRelation = new WebcertCertificateRelation(INTYG_ID, RelationKod.ERSATT, LocalDateTime.now(),
                UtkastStatus.SIGNED, false);

        when(intygRepository.findOne(INTYG_ID)).thenReturn(getUtkast(INTYG_ID));
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);
        when(webCertUserService.isAuthorizedForUnit(anyString(), anyString(), anyBoolean())).thenReturn(true);
        when(webCertUserService.getUser()).thenReturn(webCertUser);
        when(certificateRelationService.getNewestRelationOfType(anyString(), any(RelationKod.class), anyList()))
                .thenReturn(Optional.of(ersattRelation));

        CertificateResponse revokedCertificateResponse = new CertificateResponse(json, utlatande, metaData, false);
        when(moduleFacade.getCertificate(any(String.class), any(String.class), anyString())).thenReturn(revokedCertificateResponse);

        Assertions.assertThatThrownBy(() -> intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FKASSA", false))
                .isExactlyInstanceOf(WebCertServiceException.class)
                .hasFieldOrPropertyWithValue("errorCode", WebCertServiceErrorCodeEnum.INVALID_STATE)
                .hasMessageContaining("the certificate is replaced by certificate");
    }

    @Test
    public void testSendIntygOkForReplacedCertificateWithRevokedReplacingCertificate() throws Exception {
        final String completionMeddelandeId = "meddelandeId";

        WebCertUser webCertUser = createUser();

        json = FileUtils.getStringFromFile(new ClassPathResource("IntygServiceTest/utlatande.json").getFile());
        utlatande = objectMapper.readValue(json, Fk7263Utlatande.class);
        utlatande.getGrundData().setRelation(new Relation());
        utlatande.getGrundData().getRelation().setRelationKod(RelationKod.ERSATT);
        CertificateMetaData metaData = buildCertificateMetaData();
        certificateResponse = new CertificateResponse(json, utlatande, metaData, false);

        WebcertCertificateRelation ersattRelation = new WebcertCertificateRelation(INTYG_ID, RelationKod.ERSATT, LocalDateTime.now(),
                UtkastStatus.SIGNED, true);

        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);
        when(webCertUserService.isAuthorizedForUnit(anyString(), anyString(), anyBoolean())).thenReturn(true);
        when(webCertUserService.getUser()).thenReturn(webCertUser);
        when(certificateRelationService.getNewestRelationOfType(anyString(), any(RelationKod.class), anyList()))
                .thenReturn(Optional.of(ersattRelation));

        CertificateResponse revokedCertificateResponse = new CertificateResponse(json, utlatande, metaData, false);
        when(moduleFacade.getCertificate(any(String.class), any(String.class), anyString())).thenReturn(revokedCertificateResponse);

        intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FKASSA", false);

        verify(logService).logSendIntygToRecipient(any(LogRequest.class));
        verify(certificateSenderService).sendCertificate(anyString(), any(Personnummer.class), anyString(), anyString(), eq(false));

        verify(intygRepository, times(2)).findOne(INTYG_ID);
        verify(intygRepository).save(any(Utkast.class));
    }

    @Test
    public void testSendIntygCompletion() throws Exception {
        final String completionMeddelandeId = "meddelandeId";

        WebCertUser webCertUser = createUser();
        when(webCertUserService.isAuthorizedForUnit(anyString(), anyString(), anyBoolean())).thenReturn(true);
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);
        when(webCertUserService.getUser()).thenReturn(webCertUser);
        when(intygRepository.findOne(INTYG_ID)).thenReturn(getUtkast(INTYG_ID));
        Utlatande completionUtlatande = utlatande;
        completionUtlatande.getGrundData().setRelation(new Relation());
        completionUtlatande.getGrundData().getRelation().setRelationKod(RelationKod.KOMPLT);
        completionUtlatande.getGrundData().getRelation().setMeddelandeId(completionMeddelandeId);

        when(certificateRelationService.getNewestRelationOfType(eq(INTYG_ID), eq(RelationKod.ERSATT),
                eq(Arrays.asList(UtkastStatus.SIGNED)))).thenReturn(Optional.empty());

        IntygServiceResult res = intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FKASSA", false);
        assertEquals(IntygServiceResult.OK, res);

        verify(logService).logSendIntygToRecipient(any(LogRequest.class));
        verify(certificateSenderService).sendCertificate(anyString(), any(Personnummer.class), anyString(), anyString(), eq(false));

        verify(intygRepository, times(2)).findOne(INTYG_ID);
        verify(intygRepository).save(any(Utkast.class));
    }

    @Test
    public void testSendIntygReturnsInfo() throws Exception {
        final String completionMeddelandeId = "meddelandeId";

        SendCertificateToRecipientResponseType response = new SendCertificateToRecipientResponseType();
        response.setResult(ResultTypeUtil.infoResult("Info text"));

        WebCertUser webCertUser = createUser();

        Utlatande completionUtlatande = utlatande;
        completionUtlatande.getGrundData().setRelation(new Relation());
        completionUtlatande.getGrundData().getRelation().setRelationKod(RelationKod.KOMPLT);
        completionUtlatande.getGrundData().getRelation().setMeddelandeId(completionMeddelandeId);

        when(webCertUserService.isAuthorizedForUnit(anyString(), anyString(), anyBoolean())).thenReturn(true);
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);
        when(webCertUserService.getUser()).thenReturn(webCertUser);
        when(intygRepository.findOne(INTYG_ID)).thenReturn(getUtkast(INTYG_ID));

        IntygServiceResult res = intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FKASSA", false);
        assertEquals(IntygServiceResult.OK, res);

        verify(logService).logSendIntygToRecipient(any(LogRequest.class));
        verify(certificateSenderService).sendCertificate(anyString(), any(Personnummer.class), anyString(), anyString(), eq(false));
        verify(intygRepository, times(2)).findOne(INTYG_ID);
        verify(intygRepository).save(any(Utkast.class));
    }

    @Test
    public void testSendIntygThrowsExceptionWhenPUServiceIsUnavailable() throws IOException {
        final String completionMeddelandeId = "meddelandeId";

        Utlatande completionUtlatande = utlatande;
        completionUtlatande.getGrundData().setRelation(new Relation());
        completionUtlatande.getGrundData().getRelation().setRelationKod(RelationKod.KOMPLT);
        completionUtlatande.getGrundData().getRelation().setMeddelandeId(completionMeddelandeId);

        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.UNDEFINED);

        Assertions.assertThatThrownBy(() -> intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FKASSA", false))
                .isExactlyInstanceOf(WebCertServiceException.class);

    }

    @Test
    public void testSendIntygThrowsExceptionForOldFk7263WithSekretessmarkeradPatient() throws Exception {
        final String completionMeddelandeId = "meddelandeId";
        intygService.setSekretessmarkeringStartDatum(LocalDateTime.now().plusMonths(1L));

        WebCertUser webCertUser = createUser();

        Utlatande completionUtlatande = utlatande;
        completionUtlatande.getGrundData().setRelation(new Relation());
        completionUtlatande.getGrundData().getRelation().setRelationKod(RelationKod.KOMPLT);
        completionUtlatande.getGrundData().getRelation().setMeddelandeId(completionMeddelandeId);

        when(webCertUserService.isAuthorizedForUnit(anyString(), anyString(), anyBoolean())).thenReturn(true);
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.TRUE);
        when(webCertUserService.getUser()).thenReturn(webCertUser);

        Assertions.assertThatThrownBy(() -> intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FKASSA", false))
                .isExactlyInstanceOf(WebCertServiceException.class);

    }

    private WebCertUser createUser() {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        WebCertUser user = new WebCertUser();
        user.setOrigin(UserOriginType.DJUPINTEGRATION.name());
        user.setParameters(new IntegrationParameters("", "", "", "", "", "", "", "", "", false, false, false, true));
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));

        return user;
    }

    private Utkast getUtkast(String intygId) throws IOException {
        Utkast utkast = new Utkast();
        String json = IOUtils.toString(new ClassPathResource(
                "FragaSvarServiceImplTest/utlatande.json").getInputStream(), "UTF-8");
        utkast.setModel(json);
        utkast.setIntygsId(intygId);
        utkast.setIntygTypeVersion(INTYG_TYPE_VERSION_1_0);
        utkast.setStatus(UtkastStatus.SIGNED);
        utkast.setSignatur(new Signatur(LocalDateTime.of(2011, 11, 11, 11, 11, 11, 11), "Signe Signatur", INTYG_ID, "data", "hash", "signatur"));
        return utkast;
    }
}

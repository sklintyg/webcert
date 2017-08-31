/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

import org.apache.commons.io.IOUtils;
import org.apache.cxf.helpers.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.integration.converter.util.ResultTypeUtil;
import se.inera.intyg.common.support.model.common.internal.Relation;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateMetaData;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.UtkastStatus;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.patient.SekretessStatus;
import se.inera.intyg.webcert.web.service.user.dto.IntegrationParameters;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientResponseType;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IntygServiceSendTest extends AbstractIntygServiceTest {

    @Override
    @Before
    public void setupDefaultAuthorization() {
        when(webCertUserService.isAuthorizedForUnit(anyString(), anyString(), eq(true))).thenReturn(true);
    }

    @Before
    public void setupIntyg() throws Exception {
        json = FileUtils.getStringFromFile(new ClassPathResource("IntygServiceTest/utlatande.json").getFile());
        utlatande = objectMapper.readValue(json, Fk7263Utlatande.class);
    }

    @Test
    public void testSendIntyg() throws Exception {
        WebCertUser webCertUser = createUser();

        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);
        when(webCertUserService.getUser()).thenReturn(webCertUser);
        when(intygRepository.findOne(INTYG_ID)).thenReturn(getUtkast(INTYG_ID));

        IntygServiceResult res = intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FKASSA");
        assertEquals(IntygServiceResult.OK, res);

        verify(logService).logSendIntygToRecipient(any(LogRequest.class));
        verify(certificateSenderService).sendCertificate(anyString(), any(Personnummer.class), anyString(), anyString());

        verify(intygRepository, times(2)).findOne(INTYG_ID);
        verify(intygRepository).save(any(Utkast.class));
    }

    @Test(expected = WebCertServiceException.class)
    public void testSendIntygFailsForRevokedCertificate() throws Exception {
        WebCertUser webCertUser = createUser();

        when(webCertUserService.getUser()).thenReturn(webCertUser);
        when(intygRepository.findOne(INTYG_ID)).thenReturn(getUtkast(INTYG_ID));

        CertificateMetaData metaData = new CertificateMetaData();
        metaData.setStatus(new ArrayList<>());

        CertificateResponse revokedCertificateResponse = new CertificateResponse(json, utlatande, metaData, true);
        when(moduleFacade.getCertificate(any(String.class), any(String.class))).thenReturn(revokedCertificateResponse);
        when(moduleFacade.getUtlatandeFromInternalModel(anyString(), anyString())).thenReturn(utlatande);

        intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FKASSA");
        verifyZeroInteractions(logService);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSendIntygFailsForReplacedCertificate() throws Exception {
        WebCertUser webCertUser = createUser();

        when(webCertUserService.getUser()).thenReturn(webCertUser);
        when(intygRepository.findOne(INTYG_ID)).thenReturn(getUtkast(INTYG_ID));
        WebcertCertificateRelation ersattRelation = new WebcertCertificateRelation(INTYG_ID, RelationKod.ERSATT, LocalDateTime.now(),
                UtkastStatus.SIGNED);
        when(certificateRelationService.getNewestRelationOfType(eq(INTYG_ID), eq(RelationKod.ERSATT),
                eq(Arrays.asList(UtkastStatus.SIGNED)))).thenReturn(Optional.of(ersattRelation));

        CertificateMetaData metaData = new CertificateMetaData();
        metaData.setStatus(new ArrayList<>());

        CertificateResponse revokedCertificateResponse = new CertificateResponse(json, utlatande, metaData, false);
        when(moduleFacade.getCertificate(any(String.class), any(String.class))).thenReturn(revokedCertificateResponse);
        when(moduleFacade.getUtlatandeFromInternalModel(anyString(), anyString())).thenReturn(utlatande);

        intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FKASSA");
        verifyZeroInteractions(logService);
    }

    @Test
    public void testSendIntygCompletion() throws Exception {
        final String completionMeddelandeId = "meddelandeId";

        WebCertUser webCertUser = createUser();

        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);
        when(webCertUserService.getUser()).thenReturn(webCertUser);
        when(intygRepository.findOne(INTYG_ID)).thenReturn(getUtkast(INTYG_ID));
        Utlatande completionUtlatande = utlatande;
        completionUtlatande.getGrundData().setRelation(new Relation());
        completionUtlatande.getGrundData().getRelation().setRelationKod(RelationKod.KOMPLT);
        completionUtlatande.getGrundData().getRelation().setMeddelandeId(completionMeddelandeId);
        when(moduleFacade.getUtlatandeFromInternalModel(anyString(), anyString())).thenReturn(completionUtlatande);

        when(certificateRelationService.getNewestRelationOfType(eq(INTYG_ID), eq(RelationKod.ERSATT),
                eq(Arrays.asList(UtkastStatus.SIGNED)))).thenReturn(Optional.empty());

        IntygServiceResult res = intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FKASSA");
        assertEquals(IntygServiceResult.OK, res);

        verify(logService).logSendIntygToRecipient(any(LogRequest.class));
        verify(certificateSenderService).sendCertificate(anyString(), any(Personnummer.class), anyString(), anyString());

        verify(intygRepository, times(2)).findOne(INTYG_ID);
        verify(intygRepository).save(any(Utkast.class));
    }

    @Test
    public void testSendIntygReturnsInfo() throws Exception {
        SendCertificateToRecipientResponseType response = new SendCertificateToRecipientResponseType();
        response.setResult(ResultTypeUtil.infoResult("Info text"));

        WebCertUser webCertUser = createUser();

        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);
        when(webCertUserService.getUser()).thenReturn(webCertUser);
        when(intygRepository.findOne(INTYG_ID)).thenReturn(getUtkast(INTYG_ID));

        IntygServiceResult res = intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FKASSA");
        assertEquals(IntygServiceResult.OK, res);

        verify(logService).logSendIntygToRecipient(any(LogRequest.class));
        verify(certificateSenderService).sendCertificate(anyString(), any(Personnummer.class), anyString(), anyString());
        verify(intygRepository, times(2)).findOne(INTYG_ID);
        verify(intygRepository).save(any(Utkast.class));
    }

    @Test
    public void testSendIntygPDLLogServiceFailingWithRuntimeException() throws Exception {

        doThrow(new RuntimeException("")).when(logService).logSendIntygToRecipient(any(LogRequest.class));

        try {
            intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FKASSA");
            fail("RuntimeException expected");
        } catch (RuntimeException e) {
            // Expected
        }
        verify(intygRepository, times(0)).save(any(Utkast.class));
    }

    @Test(expected = WebCertServiceException.class)
    public void testSendIntygThrowsExceptionWhenPUServiceIsUnavailable() throws IOException {
        WebCertUser webCertUser = createUser();

        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.UNDEFINED);
        when(webCertUserService.getUser()).thenReturn(webCertUser);
        when(intygRepository.findOne(INTYG_ID)).thenReturn(getUtkast(INTYG_ID));

        try {
            intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FKASSA");
        } catch (Exception e) {
            verifyZeroInteractions(logService);
            throw e;
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSendIntygThrowsExceptionForOldFk7263WithSekretessmarkeradPatient() throws Exception {
        intygService.setSekretessmarkeringStartDatum(LocalDateTime.now().plusMonths(1L));

        WebCertUser webCertUser = createUser();

        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.TRUE);
        when(webCertUserService.getUser()).thenReturn(webCertUser);
        when(intygRepository.findOne(INTYG_ID)).thenReturn(getUtkast(INTYG_ID));

        try {
            intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FKASSA");
        } catch (Exception e) {
            verifyZeroInteractions(logService);
            throw e;
        }

    }

    private WebCertUser createUser() {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        WebCertUser user = new WebCertUser();
        user.setOrigin(WebCertUserOriginType.DJUPINTEGRATION.name());
        user.setParameters(new IntegrationParameters("", "", "", "", "", "", "", "", "", false, false, false, true));
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges()));

        return user;
    }

    private Utkast getUtkast(String intygId) throws IOException {
        Utkast utkast = new Utkast();
        String json = IOUtils.toString(new ClassPathResource(
                "FragaSvarServiceImplTest/utlatande.json").getInputStream(), "UTF-8");
        utkast.setModel(json);
        utkast.setIntygsId(intygId);
        return utkast;
    }



}

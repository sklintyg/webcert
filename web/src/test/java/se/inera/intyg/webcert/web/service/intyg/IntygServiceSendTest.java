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

package se.inera.intyg.webcert.web.service.intyg;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.utils.v2.ResultTypeUtil;
import se.inera.intyg.common.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.common.security.common.model.AuthoritiesConstants;
import se.inera.intyg.common.security.common.model.Role;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.common.internal.Relation;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v1.SendCertificateToRecipientResponseType;

@RunWith(MockitoJUnitRunner.class)
public class IntygServiceSendTest extends AbstractIntygServiceTest {

    @Override
    @Before
    public void setupDefaultAuthorization() {
        when(webCertUserService.isAuthorizedForUnit(anyString(), anyString(), eq(true))).thenReturn(true);
    }

    @Test
    public void testSendIntyg() throws Exception {
        WebCertUser webCertUser = createUser();

        when(webCertUserService.getUser()).thenReturn(webCertUser);
        when(intygRepository.findOne(INTYG_ID)).thenReturn(getUtkast(INTYG_ID));

        IntygServiceResult res = intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FK", true);
        assertEquals(IntygServiceResult.OK, res);

        verify(logService).logSendIntygToRecipient(any(LogRequest.class));
        verify(certificateSenderService).sendCertificate(anyString(), any(Personnummer.class), anyString(), anyString());

        verify(intygRepository, times(2)).findOne(INTYG_ID);
        verify(intygRepository).save(any(Utkast.class));
    }

    @Test
    public void testSendIntygCompletion() throws Exception {
        final String completionMeddelandeId = "meddelandeId";

        WebCertUser webCertUser = createUser();

        when(webCertUserService.getUser()).thenReturn(webCertUser);
        when(intygRepository.findOne(INTYG_ID)).thenReturn(getUtkast(INTYG_ID));
        Utlatande completionUtlatande = utlatande;
        completionUtlatande.getGrundData().setRelation(new Relation());
        completionUtlatande.getGrundData().getRelation().setRelationKod(RelationKod.KOMPLT);
        completionUtlatande.getGrundData().getRelation().setMeddelandeId(completionMeddelandeId);

        when(moduleFacade.getUtlatandeFromInternalModel(anyString(), anyString())).thenReturn(completionUtlatande);

        IntygServiceResult res = intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FK", true);
        assertEquals(IntygServiceResult.OK, res);

        verify(logService).logSendIntygToRecipient(any(LogRequest.class));
        verify(certificateSenderService).sendCertificate(anyString(), any(Personnummer.class), anyString(), anyString());

        verify(intygRepository, times(2)).findOne(INTYG_ID);
        verify(intygRepository).save(any(Utkast.class));
        verify(arendeService).closeArendeAsHandled(completionMeddelandeId, completionUtlatande.getTyp());
    }

    @Test
    public void testSendIntygReturnsInfo() throws Exception {
        SendCertificateToRecipientResponseType response = new SendCertificateToRecipientResponseType();
        response.setResult(ResultTypeUtil.infoResult("Info text"));

        WebCertUser webCertUser = createUser();

        when(webCertUserService.getUser()).thenReturn(webCertUser);
        when(intygRepository.findOne(INTYG_ID)).thenReturn(getUtkast(INTYG_ID));

        IntygServiceResult res = intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FK", true);
        assertEquals(IntygServiceResult.OK, res);

        verify(logService).logSendIntygToRecipient(any(LogRequest.class));
        verify(certificateSenderService).sendCertificate(anyString(), any(Personnummer.class), anyString(), anyString());
        verify(intygRepository, times(2)).findOne(INTYG_ID);
        verify(intygRepository).save(any(Utkast.class));
    }

    private Utkast getUtkast(String intygId) throws IOException {
        Utkast utkast = new Utkast();
        String json = IOUtils.toString(new ClassPathResource(
                "FragaSvarServiceImplTest/utlatande.json").getInputStream(), "UTF-8");
        utkast.setModel(json);
        utkast.setIntygsId(intygId);
        return utkast;
    }

    @Test
    public void testSendIntygPDLLogServiceFailingWithRuntimeException() throws Exception {

        doThrow(new RuntimeException("")).when(logService).logSendIntygToRecipient(any(LogRequest.class));

        try {
            intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FK", true);
            Assert.fail("RuntimeException expected");
        } catch (RuntimeException e) {
            // Expected
        }
        verify(intygRepository, times(0)).save(any(Utkast.class));
    }

    private WebCertUser createUser() {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges()));

        return user;
    }

}

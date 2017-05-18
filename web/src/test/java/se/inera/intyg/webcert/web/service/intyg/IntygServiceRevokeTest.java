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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.webcert.common.model.UtkastStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacadeException;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.user.dto.IntegrationParameters;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IntygServiceRevokeTest extends AbstractIntygServiceTest {

    private static final String REVOKE_MSG = "This is revoked";
    private static final String REVOKE_REASON = "FELAKTIGT_INTYG";
    private static final String INTYG_JSON = "A bit of text representing json";
    private static final String INTYG_TYPE = "fk7263";
    private static final String HSA_ID = "AAA";

    private static final String INTYG_ID = "123";

    private Utkast signedUtkast;
    private Utkast revokedUtkast;

    @Before
    public void setup() throws Exception {
        HoSPersonal person = buildHosPerson();
        VardpersonReferens vardperson = buildVardpersonReferens(person);
        WebCertUser user = buildWebCertUser(person);

        signedUtkast = buildUtkast(INTYG_ID, INTYG_TYPE, UtkastStatus.SIGNED, INTYG_JSON, vardperson);
        revokedUtkast = buildUtkast(INTYG_ID, INTYG_TYPE, UtkastStatus.SIGNED, json, vardperson);
        revokedUtkast.setAterkalladDatum(LocalDateTime.now());

        when(webCertUserService.getUser()).thenReturn(user);
    }

    @Override
    @Before
    public void setupDefaultAuthorization() {
        when(webCertUserService.isAuthorizedForUnit(anyString(), anyString(), eq(true))).thenReturn(true);
    }

    @Test
    public void testRevokeIntyg() throws Exception {

        when(intygRepository.findOne(INTYG_ID)).thenReturn(signedUtkast);
        when(webCertUserService.isAuthorizedForUnit(anyString(), eq(false))).thenReturn(true);

        // do the call
        IntygServiceResult res = intygService.revokeIntyg(INTYG_ID, INTYG_TYP_FK, REVOKE_MSG, REVOKE_REASON);

        // verify that services were called
        verify(arendeService).closeAllNonClosed(INTYG_ID);
        verify(notificationService, times(1)).sendNotificationForIntygRevoked(INTYG_ID);
        verify(logService).logRevokeIntyg(any(LogRequest.class));
        verify(intygRepository).save(any(Utkast.class));
        verify(certificateSenderService, times(1)).revokeCertificate(eq(INTYG_ID), any(), eq(INTYG_TYP_FK));
        verify(moduleFacade, times(1)).getRevokeCertificateRequest(eq(INTYG_TYP_FK), any(), any(), eq(REVOKE_MSG));
        verify(monitoringService).logIntygRevoked(INTYG_ID, HSA_ID, REVOKE_REASON);

        assertEquals(IntygServiceResult.OK, res);
    }

    @Test(expected = WebCertServiceException.class)
    public void testRevokeIntygThatHasAlreadyBeenRevokedFails() throws IntygModuleFacadeException {
        when(intygRepository.findOne(INTYG_ID)).thenReturn(revokedUtkast);
        when(moduleFacade.getCertificate(anyString(), anyString())).thenThrow(new IntygModuleFacadeException(""));
        // Do the call
        try {
            intygService.revokeIntyg(INTYG_ID, INTYG_TYP_FK, REVOKE_MSG, REVOKE_REASON);
        } finally {
            verifyZeroInteractions(certificateSenderService);
            verify(intygRepository, times(0)).save(any(Utkast.class));
            verifyZeroInteractions(notificationService);
            verifyZeroInteractions(logService);
        }
    }

    private HoSPersonal buildHosPerson() {
        HoSPersonal person = new HoSPersonal();
        person.setPersonId(HSA_ID);
        person.setFullstandigtNamn("Dr Dengroth");
        return person;
    }

    private Utkast buildUtkast(String intygId, String type, UtkastStatus status, String model, VardpersonReferens vardperson) {

        Utkast intyg = new Utkast();
        intyg.setIntygsId(intygId);
        intyg.setIntygsTyp(type);
        intyg.setStatus(status);
        intyg.setModel(model);
        intyg.setSkapadAv(vardperson);
        intyg.setSenastSparadAv(vardperson);

        return intyg;
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
        user.setOrigin(WebCertUserOriginType.DJUPINTEGRATION.name());
        user.setParameters(new IntegrationParameters("", "", "", "", "", "", "", "", "", false, false, false, true));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges()));
        user.setNamn(person.getFullstandigtNamn());
        user.setHsaId(person.getPersonId());

        return user;
    }

}

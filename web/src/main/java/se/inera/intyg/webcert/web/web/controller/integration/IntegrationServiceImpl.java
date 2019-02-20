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
package se.inera.intyg.webcert.web.web.controller.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.IntygServiceImpl;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastServiceImpl;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygTypeInfo;
import se.inera.intyg.webcert.web.web.controller.integration.dto.PrepareRedirectToIntyg;

/**
 * @author Magnus Ekstrand on 2017-10-24.
 */
@Service
public abstract class IntegrationServiceImpl implements IntegrationService {

    private static final Logger LOG = LoggerFactory.getLogger(IntygServiceImpl.class);

    protected AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();

    @Autowired
    private IntygService intygService;

    @Autowired
    private PatientDetailsResolver patientDetailsResolver;

    @Autowired
    private UtkastRepository utkastRepository;

    // api

    @Override
    public PrepareRedirectToIntyg prepareRedirectToIntyg(String intygTyp, String intygId, WebCertUser user) {
        return prepareRedirectToIntyg(intygTyp, intygId, user, null);
    }

    @Override
    public PrepareRedirectToIntyg prepareRedirectToIntyg(
            final String intygTyp, final String intygId,
            final WebCertUser user, final Personnummer prepareBeforeAlternateSsn) {

        Utkast utkast = utkastRepository.findOne(intygId);

        // INTYG-7088: since we now always need intygTypeVersion we always fetch intygTypeInfo for the intyg,
        // either from WC or IT.
        final IntygTypeInfo intygTypeInfo = intygService.getIntygTypeInfo(intygId, utkast);

        ensurePreparation(intygTypeInfo.getIntygType(), intygId, utkast, user, prepareBeforeAlternateSsn);

        return createPrepareRedirectToIntyg(intygTypeInfo, UtkastServiceImpl.isUtkast(utkast));
    }

    // protected scope

    abstract void ensurePreparation(
            String intygTyp, String intygId, Utkast utkast, WebCertUser user, Personnummer prepareBeforeAlternateSsn);

    // default scope

    void verifySekretessmarkering(Utkast utkast, WebCertUser user) {
        SekretessStatus sekretessStatus = patientDetailsResolver.getSekretessStatus(utkast.getPatientPersonnummer());
        if (SekretessStatus.UNDEFINED.equals(sekretessStatus)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                    "Could not fetch sekretesstatus for patient from PU service");
        }

        authoritiesValidator.given(user, utkast.getIntygsTyp())
                .privilegeIf(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
                        sekretessStatus == SekretessStatus.TRUE)
                .orThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM_SEKRETESSMARKERING,
                        "User missing required privilege or cannot handle sekretessmarkerad patient"));
    }

    // private scope

    private PrepareRedirectToIntyg createPrepareRedirectToIntyg(IntygTypeInfo intygTypeInfo, boolean isUtkast) {
        PrepareRedirectToIntyg prepareRedirectToIntyg = new PrepareRedirectToIntyg();
        prepareRedirectToIntyg.setIntygTyp(intygTypeInfo.getIntygType());
        prepareRedirectToIntyg.setIntygTypeVersion(intygTypeInfo.getIntygTypeVersion());
        prepareRedirectToIntyg.setIntygId(intygTypeInfo.getIntygId());
        prepareRedirectToIntyg.setUtkast(isUtkast);
        return prepareRedirectToIntyg;
    }

}

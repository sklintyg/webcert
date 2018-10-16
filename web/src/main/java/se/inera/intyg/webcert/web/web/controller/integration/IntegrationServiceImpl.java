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
package se.inera.intyg.webcert.web.web.controller.integration;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
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
        Utkast utkast = utkastRepository.findOne(intygId);

        // INTYG-4336: If intygTyp can't be established,
        // fetch certificate from IT and then get the type
        String typ = resolveIntygsTyp(intygTyp, intygId, utkast);
        if (StringUtils.isEmpty(typ)) {
            String msg = "Failed resolving type of certificate with id '" + intygId + "'";
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, msg);
        }

        ensurePreparation(typ, intygId, utkast, user);

        return createPrepareRedirectToIntyg(typ, intygId, UtkastServiceImpl.isUtkast(utkast));
    }

    // protected scope

    abstract void ensurePreparation(String intygTyp, String intygId, Utkast utkast, WebCertUser user);

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

    private PrepareRedirectToIntyg createPrepareRedirectToIntyg(String intygTyp, String intygId, boolean isUtkast) {
        PrepareRedirectToIntyg prepareRedirectToIntyg = new PrepareRedirectToIntyg();
        prepareRedirectToIntyg.setIntygTyp(intygTyp);
        prepareRedirectToIntyg.setIntygId(intygId);
        prepareRedirectToIntyg.setUtkast(isUtkast);
        return prepareRedirectToIntyg;
    }

    /*
     * Resolve type of certificate. Method will return null if both
     * intygTyp and intygId is null or empty. Method might return null
     * if call to intygService.getIntygsTyp(intygId) is made.
     */
    private String resolveIntygsTyp(final String intygTyp, final String intygId, final Utkast utkast) {
        if (StringUtils.isEmpty(intygTyp) && StringUtils.isEmpty(intygId)) {
            return null;
        } else if (StringUtils.isEmpty(intygTyp)) {
            return getIntygsTyp(intygId, utkast);
        }
        return intygTyp;
    }

    private String getIntygsTyp(String intygId, Utkast utkast) {
        try {
            return utkast != null ? utkast.getIntygsTyp() : intygService.getIntygsTyp(intygId);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return null;
    }

}

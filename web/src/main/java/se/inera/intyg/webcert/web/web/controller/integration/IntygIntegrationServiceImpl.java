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
package se.inera.intyg.webcert.web.web.controller.integration;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.model.UtkastStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.UpdatePatientOnDraftRequest;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

import static se.inera.intyg.webcert.web.web.controller.integration.IntygIntegrationController.*;

/**
 * @author Magnus Ekstrand on 2017-10-09.
 */
@Service
public class IntygIntegrationServiceImpl implements IntegrationService {

    protected AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();

    @Autowired
    private MonitoringLogService monitoringLog;

    @Autowired
    private PatientDetailsResolver patientDetailsResolver;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private UtkastService utkastService;


    // api

    @Override
    public PrepareRedirectToIntyg prepareRedirectToIntyg(
            final String intygTyp,
            final String intygId,
            final WebCertUser user) {

        String intygsTyp = intygTyp;
        String intygsId = intygId;

        Utkast utkast = utkastRepository.findOne(intygsId);

        // If intygTyp can't be established, default to FK7263 to be backwards compatible
        if (intygTyp == null) {
            intygsTyp = utkast != null ? utkast.getIntygsTyp() : Fk7263EntryPoint.MODULE_ID;
        }

        // If the type doesn't equals to FK7263 then verify the required query-parameters
        if (!intygsTyp.equals(Fk7263EntryPoint.MODULE_ID)) {
            verifyParameters(user.getParameters());
        }

        if (utkast != null) {
            // INTYG-4086: If the intyg / utkast is authored in webcert, we can check for sekretessmarkering here.
            // If the intyg was authored elsewhere, the check has to be performed after the redirect when the actual intyg
            // is loaded from Intygstjänsten.
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

            // INTYG-3212: ArendeDraft patient info should always be up-to-date with the patient info supplied by the
            // integrating journaling system
            if (isUtkast(utkast)) {
                ensureDraftPatientInfoUpdated(intygsTyp, intygId, utkast.getVersion(), user);
            }

            // Monitoring log the usage of coherentJournaling
            if (user.getParameters().isSjf()) {
                if (!utkast.getVardgivarId().equals(user.getValdVardgivare().getId())) {
                    monitoringLog.logIntegratedOtherCaregiver(intygId, intygsTyp, utkast.getVardgivarId(), utkast.getEnhetsId());
                } else if (!user.getValdVardenhet().getHsaIds().contains(utkast.getEnhetsId())) {
                    monitoringLog.logIntegratedOtherUnit(intygId, intygsTyp, utkast.getEnhetsId());
                }
            }
        }

        return createPrepareRedirectToIntyg(intygsTyp, intygsId, utkast);
    }


    // default scope

    /**
     * Updates Patient section of a draft with updated patient details for selected types.
     */
    void ensureDraftPatientInfoUpdated(String intygsType, String draftId, long draftVersion, WebCertUser user) {

        // To be allowed to update utkast, we need to have the same authority as when saving a draft..
        authoritiesValidator.given(user, intygsType)
                .features(WebcertFeature.HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
                .orThrow();

        String alternatePatientSSn = user.getParameters().getAlternateSsn();
        Personnummer personnummer = new Personnummer(alternatePatientSSn);
        UpdatePatientOnDraftRequest request = new UpdatePatientOnDraftRequest(personnummer, draftId, draftVersion);
        utkastService.updatePatientOnDraft(request);
    }

    private void verifyParameters(IntegrationParameters parameters) {
        if (parameters == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER,
                    "Missing required integration parameters");
        }

        verifyQueryString(PARAM_PATIENT_FORNAMN, parameters.getFornamn());
        verifyQueryString(PARAM_PATIENT_EFTERNAMN, parameters.getEfternamn());
        verifyQueryString(PARAM_PATIENT_POSTADRESS, parameters.getPostadress());
        verifyQueryString(PARAM_PATIENT_POSTNUMMER, parameters.getPostnummer());
        verifyQueryString(PARAM_PATIENT_POSTORT, parameters.getPostort());
    }

    private void verifyQueryString(String queryStringName, String queryStringValue) {
        if (Strings.nullToEmpty(queryStringValue).trim().isEmpty()) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER,
                    "Missing required parameter '" + queryStringName + "'");
        }
    }


    // private stuff

    private PrepareRedirectToIntyg createPrepareRedirectToIntyg(String intygTyp, String intygId, Utkast utkast) {
        PrepareRedirectToIntyg prepareRedirectToIntyg = new PrepareRedirectToIntyg();
        prepareRedirectToIntyg.setIntygTyp(intygTyp);
        prepareRedirectToIntyg.setIntygId(intygId);
        prepareRedirectToIntyg.setUtkast(isUtkast(utkast));
        return prepareRedirectToIntyg;
    }

    private boolean isUtkast(Utkast utkast) {
        return utkast != null && !utkast.getStatus().equals(UtkastStatus.SIGNED);
    }

}

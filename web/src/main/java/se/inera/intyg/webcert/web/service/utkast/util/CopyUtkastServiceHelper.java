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
package se.inera.intyg.webcert.web.service.utkast.util;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.validate.SamordningsnummerValidator;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.dto.AbstractCreateCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCompletionCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateRenewalCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateReplacementCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@Service
public class CopyUtkastServiceHelper {

    private static final Logger LOG = LoggerFactory.getLogger(CopyUtkastServiceHelper.class);

    private WebCertUserService webCertUserService;

    private AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();

    @Autowired
    public void setWebCertUserService(WebCertUserService webCertUserService) {
        this.webCertUserService = webCertUserService;
    }

    public CreateUtkastFromTemplateRequest createUtkastFromUtkast(String orgIntygsId, String intygsTyp,
                                                                  CopyIntygRequest request) {
        HoSPersonal hosPerson = createHoSPersonFromUser();
        Patient patient = createPatientFromCopyIntygRequest(request);

        CreateUtkastFromTemplateRequest req = new CreateUtkastFromTemplateRequest(orgIntygsId, intygsTyp, patient,
                hosPerson, intygsTyp);

        // Add new personnummer to request
        addPersonnummerToRequest(req, webCertUserService.getUser().getParameters());

        // Set djupintegrerad flag on request to true if origin is DJUPINTEGRATION
        setDeepIntegrationFlagOnRequest(req);

        return req;
    }

    public CreateUtkastFromTemplateRequest createUtkastFromDifferentIntygTypeRequest(String orgIntygsId, String newIntygsTyp,
                                                                                            String orgIntygsTyp,
                                                                                            CopyIntygRequest request) {
        HoSPersonal hosPerson = createHoSPersonFromUser();
        Patient patient = createPatientFromCopyIntygRequest(request);

        CreateUtkastFromTemplateRequest req = new CreateUtkastFromTemplateRequest(orgIntygsId, newIntygsTyp, patient,
                hosPerson, orgIntygsTyp);

        // Add new personnummer to request
        addPersonnummerToRequest(req, webCertUserService.getUser().getParameters());

        // Set djupintegrerad flag on request to true if origin is DJUPINTEGRATION
        setDeepIntegrationFlagOnRequest(req);

        return req;
    }

    public CreateReplacementCopyRequest createReplacementCopyRequest(String orgIntygsId, String intygsTyp, CopyIntygRequest request) {
        HoSPersonal hosPerson = createHoSPersonFromUser();
        Patient patient = createPatientFromCopyIntygRequest(request);
        final WebCertUser user = webCertUserService.getUser();
        IntegrationParameters parameters = user.getParameters();

        boolean coherentJournaling = parameters != null && parameters.isSjf();

        CreateReplacementCopyRequest req = new CreateReplacementCopyRequest(orgIntygsId, intygsTyp, patient, hosPerson, coherentJournaling);

        // Add new personnummer to request
        addPersonnummerToRequest(req, parameters);

        // Set djupintegrerad flag on request to true if origin is DJUPINTEGRATION
        setDeepIntegrationFlagOnRequest(req);

        return req;
    }

    public CreateRenewalCopyRequest createRenewalCopyRequest(String orgIntygsId, String intygsTyp, CopyIntygRequest request) {
        HoSPersonal hosPerson = createHoSPersonFromUser();
        Patient patient = createPatientFromCopyIntygRequest(request);

        CreateRenewalCopyRequest req = new CreateRenewalCopyRequest(orgIntygsId, intygsTyp, patient, hosPerson);

        // Add new personnummer to request
        addPersonnummerToRequest(req, webCertUserService.getUser().getParameters());

        // Set djupintegrerad flag on request to true if origin is DJUPINTEGRATION
        setDeepIntegrationFlagOnRequest(req);

        return req;
    }

    public CreateCompletionCopyRequest createCompletionCopyRequest(String orgIntygsId, String intygsTyp, String meddelandeId,
                                                                    CopyIntygRequest copyRequest) {
        HoSPersonal hosPerson = createHoSPersonFromUser();
        Patient patient = createPatientFromCopyIntygRequest(copyRequest);

        CreateCompletionCopyRequest req = new CreateCompletionCopyRequest(orgIntygsId, intygsTyp, meddelandeId,
                patient, hosPerson, copyRequest.getKommentar());

        // Add new personnummer to request
        addPersonnummerToRequest(req, webCertUserService.getUser().getParameters());

        // Set djupintegrerad flag on request to true if origin is DJUPINTEGRATION
        setDeepIntegrationFlagOnRequest(req);

        return req;
    }

    private Patient createPatientFromCopyIntygRequest(CopyIntygRequest copyRequest) {
        WebCertUser user = webCertUserService.getUser();
        IntegrationParameters parameters = user.getParameters();

        Patient patient = new Patient();
        patient.setPersonId(copyRequest.getPatientPersonnummer());
        if (parameters != null) {
            if (!Strings.nullToEmpty(parameters.getFornamn()).trim().isEmpty()) {
                patient.setFornamn(parameters.getFornamn());
            }
            if (!Strings.nullToEmpty(parameters.getEfternamn()).trim().isEmpty()) {
                patient.setEfternamn(parameters.getEfternamn());
            }
            if (!Strings.nullToEmpty(parameters.getMellannamn()).trim().isEmpty()) {
                patient.setMellannamn(parameters.getMellannamn());
            }
            if (!Strings.nullToEmpty(parameters.getPostadress()).trim().isEmpty()) {
                patient.setPostadress(parameters.getPostadress());
            }
            if (!Strings.nullToEmpty(parameters.getPostnummer()).trim().isEmpty()) {
                patient.setPostnummer(parameters.getPostnummer());
            }
            if (!Strings.nullToEmpty(parameters.getPostort()).trim().isEmpty()) {
                patient.setPostort(parameters.getPostort());
            }
        }
        return patient;

    }

    private void setDeepIntegrationFlagOnRequest(AbstractCreateCopyRequest req) {
        if (authoritiesValidator.given(webCertUserService.getUser()).origins(UserOriginType.DJUPINTEGRATION).isVerified()) {
            LOG.debug("Setting djupintegrerad flag on request to true");
            req.setDjupintegrerad(true);
        }
    }

    private void addPersonnummerToRequest(AbstractCreateCopyRequest req, IntegrationParameters parameters) {
        if (parameters != null) {
            Optional<Personnummer> optionalPnr = createOptPnr(parameters.getAlternateSsn());
            if (isNewValidPatientPersonId(optionalPnr)) {
                LOG.debug("Adding new personnummer to request");
                req.setNyttPatientPersonnummer(optionalPnr.get());
            }
        }
    }

    private HoSPersonal createHoSPersonFromUser() {
        WebCertUser user = webCertUserService.getUser();
        return IntygConverterUtil.buildHosPersonalFromWebCertUser(user, null);
    }

    private boolean isNewValidPatientPersonId(Optional<Personnummer> newPersonnummer) {
        return (newPersonnummer != null
                && (newPersonnummer.isPresent() || SamordningsnummerValidator.isSamordningsNummer(newPersonnummer)));
    }

    private Optional<Personnummer> createOptPnr(String personId) {
        return Personnummer.createPersonnummer(personId);
    }
}

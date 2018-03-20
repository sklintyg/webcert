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
package se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.integration.converters.IntygsTypToInternal;
import se.inera.intyg.webcert.web.integration.validators.PersonnummerChecksumValidator;
import se.inera.intyg.webcert.web.integration.validators.ResultValidator;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by eriklupander on 2017-09-19.
 */
public abstract class BaseCreateDraftCertificateValidator {

    private static final Logger LOG = LoggerFactory.getLogger(BaseCreateDraftCertificateValidator.class);

    @Autowired
    protected IntygModuleRegistry moduleRegistry;

    @Autowired
    private PatientDetailsResolver patientDetailsResolver;

    @Autowired
    private AuthoritiesHelper authoritiesHelper;

    protected Optional<Personnummer> createPersonnummer(ResultValidator errors, String personId) {
        Optional<Personnummer> personnummer = Personnummer.createPersonnummer(personId);
        if (!personnummer.isPresent()) {
            errors.addError("Cannot create Personnummer object with invalid personId {0}", personId);
        }
        return personnummer;
    }

    protected void validatePUServiceResponse(ResultValidator errors, Personnummer personnummer) {

        PersonSvar personSvar = patientDetailsResolver.getPersonFromPUService(personnummer);

        switch (personSvar.getStatus()) {
        case ERROR:
            errors.addError("Cannot issue intyg. The PU-service was unreachable. Please try again later.");
            break;
        case NOT_FOUND:
            String msg = "Personnumret du har angivit finns inte i folkbokföringsregistret."
                    + " Observera att det inte går att ange reservnummer."
                    + " Webcert hanterar enbart person- och samordningsnummer.";
            errors.addError(msg);
            break;
        default:
            break; // Do nothing
        }
    }

    protected void validateBusinessRulesForSekretessmarkeradPatient(ResultValidator errors,
            Personnummer personnummer,
            String intygsTyp,
            IntygUser user) {
        if (personnummer != null) {
            final SekretessStatus sekretessStatus = patientDetailsResolver.getSekretessStatus(personnummer);
            if (sekretessStatus != SekretessStatus.UNDEFINED) {
                validateSekretess(errors, intygsTyp, sekretessStatus);
                validateHsaUserMayCreateDraft(errors, user, sekretessStatus);
            }
        }
    }

    protected void validateCreateForAvlidenPatientAllowed(
            ResultValidator errors,
            Personnummer personnummer,
            String typAvIntyg) {

        String intygsTyp = IntygsTypToInternal.convertToInternalIntygsTyp(typAvIntyg);

        if (personnummer == null) {
            errors.addError("Cannot issue intyg type {0} for personnummer that is null", intygsTyp);
            return;
        }

        if (patientDetailsResolver.isAvliden(personnummer) && !authoritiesHelper.getIntygstyperAllowedForAvliden().contains(intygsTyp)) {
            errors.addError("Cannot issue intyg type {0} for deceased patient {1}", intygsTyp, personnummer.getPersonnummerWithDash());
        }
    }

    protected void validateModuleSupport(ResultValidator errors, String moduleId) {
        if (!moduleRegistry.moduleExists(moduleId)) {
            errors.addError("Intyg {0} is not supported", moduleId);
        }

        try {
            if (moduleRegistry.getIntygModule(moduleId).isDeprecated()) {
                errors.addError("Intyg of type {0} has been deprecated and is no longer possible to issue.", moduleId);
                return;
            }
        } catch (ModuleNotFoundException n) {
            LOG.error("Module {} not found while validating module support", moduleId);
            errors.addError("Intyg {0} is not supported", moduleId);
            return;
        }
        if (!authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST, moduleId)) {
            errors.addError("Intyg {0} is not supported", moduleId);
        }
    }

    protected void validatePatient(ResultValidator errors, List<String> fornamn, String efternamn, String personId) {
        if (Strings.nullToEmpty(efternamn).trim().isEmpty()) {
            errors.addError("efternamn is required");
        }
        if (isNullOrEmpty(fornamn)) {
            errors.addError("At least one fornamn is required");
        }
        if (Strings.nullToEmpty(personId).trim().isEmpty()) {
            errors.addError("personId is required");
        } else {
            validatePersonnummer(errors, personId);
        }
    }

    protected void validateSkapadAv(ResultValidator errors, String fullstandigtNamn, String personId) {
        if (Strings.nullToEmpty(fullstandigtNamn).trim().isEmpty()) {
            errors.addError("Physicians full name is required");
        }
        if (Strings.nullToEmpty(personId).trim().isEmpty()) {
            errors.addError("Physicians hsaId is required");
        }
    }

    protected void validateEnhet(ResultValidator errors, String enhetsnamn, String enhetsId) {
        if (Strings.nullToEmpty(enhetsnamn).trim().isEmpty()) {
            errors.addError("enhetsnamn is required");
        }
        if (Strings.nullToEmpty(enhetsId).trim().isEmpty()) {
            errors.addError("enhetsId is required");
        }
    }

    private void validatePersonnummer(ResultValidator errors, String personId) {
        Personnummer pnr = createPersonnummer(errors, personId).orElse(null);
        PersonnummerChecksumValidator.validate(pnr, errors);
    }

    private void validateHsaUserMayCreateDraft(ResultValidator errors, IntygUser user, SekretessStatus sekretessStatus) {
        if (sekretessStatus == SekretessStatus.TRUE) {
            AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
            if (!authoritiesValidator.given(user)
                    .privilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT)
                    .isVerified()) {
                errors.addError(
                        "Du saknar behörighet. För att hantera intyg för patienter med sekretessmarkering krävs "
                                + "att du har befattningen läkare eller tandläkare");
            }
        }
    }

    private void validateSekretess(ResultValidator errors, String intygsTyp, SekretessStatus sekretessStatus) {
        if (!authoritiesHelper.getIntygstyperAllowedForSekretessmarkering().contains(intygsTyp)) {
            switch (sekretessStatus) {
            case TRUE:
                errors.addError("Cannot issue intyg type {0} for patient having "
                        + "sekretessmarkering.", intygsTyp);
                break;
            case UNDEFINED:
                errors.addError("Cannot issue intyg type {0} for unknown patient. Might be due "
                        + "to a problem in the PU service.", intygsTyp);
                break;
            case FALSE:
                break; // Do nothing
            }
        }
    }

    private boolean isNullOrEmpty(List<?> list) {
        if (list == null || list.isEmpty()) {
            return true;
        }
        return list.parallelStream().allMatch(Objects::isNull);
    }

}

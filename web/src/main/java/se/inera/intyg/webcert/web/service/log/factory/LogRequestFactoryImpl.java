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
package se.inera.intyg.webcert.web.service.log.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;

import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

/**
 * Service / Factory that produces PDL log requests.
 *
 * Note the hard-coded business rule where intygstyper having FKASSA as default recipient will have patient
 * first, middle and last names blanked out.
 *
 * @author eriklupander
 */
@Service
public class LogRequestFactoryImpl  implements LogRequestFactory {

    private static final String COHERENT_JOURNALING_LOG_POST = "Läsning i enlighet med sammanhållen journalföring";
    private static final String FKASSA = "FKASSA";

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Override
    public LogRequest createLogRequestFromUtkast(Utkast utkast) {
        return createLogRequestFromUtkast(utkast, false);
    }

    @Override
    public LogRequest createLogRequestFromUtkast(Utkast utkast, boolean coherentJournaling) {
        LogRequest logRequest = new LogRequest();

        logRequest.setIntygId(utkast.getIntygsId());
        logRequest.setPatientId(utkast.getPatientPersonnummer());

        addPatientNameIfNotFK(
                Joiner.on(" ").skipNulls().join(utkast.getPatientFornamn(), utkast.getPatientMellannamn(), utkast.getPatientEfternamn()),
                logRequest, utkast.getIntygsTyp());

        logRequest.setIntygCareUnitId(utkast.getEnhetsId());
        logRequest.setIntygCareUnitName(utkast.getEnhetsNamn());

        logRequest.setIntygCareGiverId(utkast.getVardgivarId());
        logRequest.setIntygCareGiverName(utkast.getVardgivarNamn());

        if (coherentJournaling) {
            logRequest.setAdditionalInfo(COHERENT_JOURNALING_LOG_POST);
        }

        return logRequest;
    }

    @Override
    public LogRequest createLogRequestFromUtlatande(Utlatande utlatande) {
        return createLogRequestFromUtlatande(utlatande, false);
    }

    @Override
    public LogRequest createLogRequestFromUtlatande(Utlatande utlatande, boolean coherentJournaling) {
        LogRequest logRequest = new LogRequest();
        logRequest.setIntygId(utlatande.getId());

        logRequest.setPatientId(utlatande.getGrundData().getPatient().getPersonId());
        addPatientNameIfNotFK(utlatande.getGrundData().getPatient().getFullstandigtNamn(), logRequest, utlatande.getTyp());

        logRequest.setIntygCareUnitId(utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsid());
        logRequest.setIntygCareUnitName(utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsnamn());

        logRequest.setIntygCareGiverId(utlatande.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarid());
        logRequest.setIntygCareGiverName(utlatande.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarnamn());

        if (coherentJournaling) {
            logRequest.setAdditionalInfo(COHERENT_JOURNALING_LOG_POST);
        }

        return logRequest;
    }

    @Override
    public LogRequest createLogRequestFromUser(WebCertUser user, String patientId) {
        LogRequest request = new LogRequest();

        request.setPatientId(Personnummer.createPersonnummer(patientId).get());
        request.setPatientName("");

        request.setIntygCareUnitId(user.getValdVardenhet().getId());
        request.setIntygCareUnitName(user.getValdVardenhet().getNamn());

        request.setIntygCareGiverId(user.getValdVardgivare().getId());
        request.setIntygCareGiverName(user.getValdVardgivare().getNamn());

        if (user.getParameters() != null && user.getParameters().isSjf()) {
            request.setAdditionalInfo(COHERENT_JOURNALING_LOG_POST);
        }
        return request;
    }

    /**
     * INTYG-4234: PDL-log statements for FK-intyg must _not_ include the patientName.
     */
    private void addPatientNameIfNotFK(String patientName, LogRequest logRequest, String intygsTyp) {
        try {
            if (FKASSA.equals(moduleRegistry.getModuleEntryPoint(intygsTyp).getDefaultRecipient())) {
                logRequest.setPatientName("");
            } else {
                logRequest.setPatientName(patientName);
            }
        } catch (ModuleNotFoundException e) {
            throw new IllegalArgumentException("Unknown intyg-typ '" + intygsTyp + "', cannot determine default recipient");
        }
    }
}

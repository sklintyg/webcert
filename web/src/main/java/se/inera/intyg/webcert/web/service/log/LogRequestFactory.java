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

package se.inera.intyg.webcert.web.service.log;

import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;

public final class LogRequestFactory {

    private LogRequestFactory() {
    }

    public static LogRequest createLogRequestFromUtkast(Utkast utkast) {

        LogRequest logRequest = new LogRequest();

        logRequest.setIntygId(utkast.getIntygsId());
        logRequest.setPatientId(utkast.getPatientPersonnummer());
        logRequest.setPatientName(utkast.getPatientFornamn(), utkast.getPatientMellannamn(), utkast.getPatientEfternamn());

        logRequest.setIntygCareUnitId(utkast.getEnhetsId());
        logRequest.setIntygCareUnitName(utkast.getEnhetsNamn());

        logRequest.setIntygCareGiverId(utkast.getVardgivarId());
        logRequest.setIntygCareGiverName(utkast.getVardgivarNamn());

        return logRequest;
    }

    public static LogRequest createLogRequestFromUtlatande(Utlatande utlatande) {

        LogRequest logRequest = new LogRequest();
        logRequest.setIntygId(utlatande.getId());

        logRequest.setPatientId(utlatande.getGrundData().getPatient().getPersonId());
        logRequest.setPatientName(utlatande.getGrundData().getPatient().getFullstandigtNamn());

        logRequest.setIntygCareUnitId(utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsid());
        logRequest.setIntygCareUnitName(utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsnamn());

        logRequest.setIntygCareGiverId(utlatande.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarid());
        logRequest.setIntygCareGiverName(utlatande.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarnamn());

        return logRequest;
    }
}

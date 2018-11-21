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
package se.inera.intyg.webcert.web.service.patient;

import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;

import java.util.List;
import java.util.Map;

/**
 * Created by eriklupander on 2017-08-14.
 */
public interface PatientDetailsResolver {
    PersonSvar getPersonFromPUService(Personnummer personnummer);

    Patient resolvePatient(Personnummer personnummer, String intygsTyp, String intygsTypVersion);

    SekretessStatus getSekretessStatus(Personnummer personNummer);

    boolean isAvliden(Personnummer personnummer);

    boolean isPatientNamedChanged(Patient oldPatient, Patient newPatient);

    boolean isPatientAddressChanged(Patient oldPatient, Patient newPatient);

    Map<Personnummer, SekretessStatus> getSekretessStatusForList(List<Personnummer> personnummerList);
}

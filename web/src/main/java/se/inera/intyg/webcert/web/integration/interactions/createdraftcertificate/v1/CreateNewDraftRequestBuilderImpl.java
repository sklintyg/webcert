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
package se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v1;

import com.google.common.base.Joiner;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.integration.util.HoSPersonHelper;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Utlatande;

import java.util.List;

@Component
public class CreateNewDraftRequestBuilderImpl implements CreateNewDraftRequestBuilder {

    private static final String SPACE = " ";

    @Override
    public CreateNewDraftRequest buildCreateNewDraftRequest(Utlatande utlatande, IntygUser user) {
        HoSPersonal hosPerson = createHoSPerson(utlatande.getSkapadAv(),
                HoSPersonHelper.createVardenhetFromIntygUser(utlatande.getSkapadAv().getEnhet().getEnhetsId().getExtension(), user));
        HoSPersonHelper.enrichHoSPerson(hosPerson, user);
        return new CreateNewDraftRequest(null, utlatande.getTypAvUtlatande().getCode(), null, hosPerson,
                createPatient(utlatande.getPatient()));
    }


    private HoSPersonal createHoSPerson(HosPersonal hoSPersonType, Vardenhet vardenhet) {
        HoSPersonal hoSPerson = new HoSPersonal();
        hoSPerson.setFullstandigtNamn(hoSPersonType.getFullstandigtNamn());
        hoSPerson.setPersonId(hoSPersonType.getPersonalId().getExtension());
        hoSPerson.setVardenhet(vardenhet);
        return hoSPerson;
    }

    private Patient createPatient(se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Patient patientType) {
        Patient patient = new Patient();
        patient.setPersonId(new Personnummer(patientType.getPersonId().getExtension()));
        patient.setFornamn(joinNames(patientType.getFornamn()));
        patient.setMellannamn(joinNames(patientType.getMellannamn()));
        patient.setEfternamn(patientType.getEfternamn());
        patient.setFullstandigtNamn(
                IntygConverterUtil.concatPatientName(patient.getFornamn(), patient.getMellannamn(), patient.getEfternamn()));
        return patient;
    }

    private static String joinNames(List<String> names) {
        return Joiner.on(SPACE).join(names);
    }
}

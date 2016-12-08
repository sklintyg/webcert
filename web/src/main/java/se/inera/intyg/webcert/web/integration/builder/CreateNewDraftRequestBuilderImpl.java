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

package se.inera.intyg.webcert.web.integration.builder;

import java.util.List;

import com.google.common.base.Joiner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.infra.integration.hsa.services.HsaOrganizationsService;
import se.inera.intyg.infra.integration.hsa.services.HsaPersonService;
import se.inera.intyg.infra.integration.hsa.util.HsaAttributeExtractor;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Utlatande;
import se.riv.infrastructure.directory.v1.CommissionType;
import se.riv.infrastructure.directory.v1.PersonInformationType;

@Component
public class CreateNewDraftRequestBuilderImpl implements CreateNewDraftRequestBuilder {

    private static final String SPACE = " ";

    @Autowired
    private HsaOrganizationsService hsaOrganizationsService;

    @Autowired
    private HsaPersonService hsaPersonService;

    private HsaAttributeExtractor hsaAttributeExtractor = new HsaAttributeExtractor();

    @Override
    public CreateNewDraftRequest buildCreateNewDraftRequest(Utlatande utlatande, CommissionType miuOnUnit) {
        HoSPersonal hosPerson = createHoSPerson(utlatande.getSkapadAv(), createVardenhetFromMIU(miuOnUnit));
        enrichHoSPerson(hosPerson);
        return new CreateNewDraftRequest(null, utlatande.getTypAvUtlatande().getCode(), null, hosPerson, createPatient(utlatande.getPatient()));
    }

    private void enrichHoSPerson(HoSPersonal hosPerson) {
        List<PersonInformationType> hsaPersonResponse = hsaPersonService.getHsaPersonInfo(hosPerson.getPersonId());
        if (hsaPersonResponse != null && hsaPersonResponse.size() > 0) {
            // set befattningar and specialiteter from hsa response
            hosPerson.getBefattningar().addAll(hsaAttributeExtractor.extractBefattningar(hsaPersonResponse));
            hosPerson.getSpecialiteter().addAll(hsaAttributeExtractor.extractSpecialiseringar(hsaPersonResponse));
        }
    }

    private Vardenhet createVardenhetFromMIU(CommissionType miu) {

        se.inera.intyg.infra.integration.hsa.model.Vardenhet hsaVardenhet = hsaOrganizationsService.getVardenhet(miu.getHealthCareUnitHsaId());

        Vardenhet vardenhet = new Vardenhet();
        vardenhet.setEnhetsnamn(hsaVardenhet.getNamn());
        vardenhet.setEnhetsid(hsaVardenhet.getId());
        vardenhet.setArbetsplatsKod(hsaVardenhet.getArbetsplatskod());
        vardenhet.setPostadress(hsaVardenhet.getPostadress());
        vardenhet.setPostnummer(hsaVardenhet.getPostnummer());
        vardenhet.setPostort(hsaVardenhet.getPostort());
        vardenhet.setTelefonnummer(hsaVardenhet.getTelefonnummer());

        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setVardgivarid(miu.getHealthCareProviderHsaId());
        vardgivare.setVardgivarnamn(miu.getHealthCareProviderName());
        vardenhet.setVardgivare(vardgivare);

        return vardenhet;
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
        patient.setFullstandigtNamn(IntygConverterUtil.concatPatientName(patient.getFornamn(), patient.getMellannamn(), patient.getEfternamn()));
        return patient;
    }

    private static String joinNames(List<String> names) {
        return Joiner.on(SPACE).join(names);
    }
}

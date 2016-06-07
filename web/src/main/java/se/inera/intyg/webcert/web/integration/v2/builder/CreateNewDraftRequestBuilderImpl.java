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

package se.inera.intyg.webcert.web.integration.v2.builder;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.intyg.common.integration.hsa.services.HsaOrganizationsService;
import se.inera.intyg.common.integration.hsa.services.HsaPersonService;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.web.service.dto.*;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v2.Intyg;
import se.riv.infrastructure.directory.v1.*;

@Component(value = "createNewDraftRequestBuilderImplV2")
public class CreateNewDraftRequestBuilderImpl implements CreateNewDraftRequestBuilder {

    @Autowired
    private HsaOrganizationsService hsaOrganizationsService;

    @Autowired
    private HsaPersonService hsaPersonService;

    @Override
    public CreateNewDraftRequest buildCreateNewDraftRequest(Intyg intyg, CommissionType miuOnUnit) {
        CreateNewDraftRequest utkastsRequest = new CreateNewDraftRequest();

        utkastsRequest.setIntygType(intyg.getTypAvIntyg().getCode().toLowerCase());

        se.inera.intyg.webcert.web.service.dto.Patient patient = createPatient(intyg.getPatient());
        utkastsRequest.setPatient(patient);

        Vardenhet vardenhet = createVardenhetFromMIU(miuOnUnit);
        utkastsRequest.setVardenhet(vardenhet);

        HoSPerson hosPerson = createHoSPerson(intyg.getSkapadAv());
        enrichHoSPerson(hosPerson);
        utkastsRequest.setHosPerson(hosPerson);
        return utkastsRequest;
    }

    private HoSPerson createHoSPerson(se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v2.HosPersonal hoSPersonType) {
        HoSPerson hoSPerson = new HoSPerson();
        hoSPerson.setNamn(hoSPersonType.getFullstandigtNamn());
        hoSPerson.setHsaId(hoSPersonType.getPersonalId().getExtension());
        hoSPerson.setForskrivarkod(hoSPerson.getForskrivarkod());
        return hoSPerson;
    }

    private se.inera.intyg.webcert.web.service.dto.Patient createPatient(
            se.riv.clinicalprocess.healthcond.certificate.v2.Patient patientType) {
        se.inera.intyg.webcert.web.service.dto.Patient patient = new se.inera.intyg.webcert.web.service.dto.Patient();
        patient.setPersonnummer(new Personnummer(patientType.getPersonId().getExtension()));
        patient.setFornamn(patientType.getFornamn());
        patient.setMellannamn(patientType.getMellannamn());
        patient.setEfternamn(patientType.getEfternamn());
        patient.setPostadress(patientType.getPostadress());
        patient.setPostnummer(patientType.getPostnummer());
        patient.setPostort(patientType.getPostort());
        return patient;
    }

    private void enrichHoSPerson(HoSPerson hosPerson) {
        List<PersonInformationType> hsaPersonResponse = hsaPersonService.getHsaPersonInfo(hosPerson.getHsaId());
        if (hsaPersonResponse != null && hsaPersonResponse.size() > 0) {
            PersonInformationType personInfo = hsaPersonResponse.get(0);

            // Use first non null PaTitleName to set befattning
            if (personInfo.getPaTitle() != null && personInfo.getPaTitle().size() > 0) {
                hosPerson.setBefattning(
                        personInfo.getPaTitle().stream()
                                .map(PaTitleType::getPaTitleName)
                                .filter(Objects::nonNull)
                                .findFirst()
                                .orElse(null));
            }

            // Use specialityNames
            if (personInfo.getSpecialityName() != null) {
                for (String specialityName : personInfo.getSpecialityName()) {
                    hosPerson.getSpecialiseringar().add(specialityName);
                }

                // Sort
                List<String> sortedSpecialiseringar = hosPerson.getSpecialiseringar().stream()
                        .sorted()
                        .collect(Collectors.toList());
                hosPerson.getSpecialiseringar().clear();
                hosPerson.getSpecialiseringar().addAll(sortedSpecialiseringar);
            }

        }
    }

    private Vardenhet createVardenhetFromMIU(CommissionType miu) {

        se.inera.intyg.common.integration.hsa.model.Vardenhet hsaVardenhet = hsaOrganizationsService.getVardenhet(miu.getHealthCareUnitHsaId());

        Vardenhet vardenhet = new Vardenhet();
        vardenhet.setNamn(hsaVardenhet.getNamn());
        vardenhet.setHsaId(hsaVardenhet.getId());
        vardenhet.setArbetsplatskod(hsaVardenhet.getArbetsplatskod());
        vardenhet.setPostadress(hsaVardenhet.getPostadress());
        vardenhet.setPostnummer(hsaVardenhet.getPostnummer());
        vardenhet.setPostort(hsaVardenhet.getPostort());
        vardenhet.setTelefonnummer(hsaVardenhet.getTelefonnummer());

        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setHsaId(miu.getHealthCareProviderHsaId());
        vardgivare.setNamn(miu.getHealthCareProviderName());
        vardenhet.setVardgivare(vardgivare);

        return vardenhet;
    }

}

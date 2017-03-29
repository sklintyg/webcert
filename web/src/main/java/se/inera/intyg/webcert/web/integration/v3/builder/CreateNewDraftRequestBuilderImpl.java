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
package se.inera.intyg.webcert.web.integration.v3.builder;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.converter.TransportConverterUtil;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.infra.integration.hsa.services.HsaOrganizationsService;
import se.inera.intyg.infra.integration.hsa.services.HsaPersonService;
import se.inera.intyg.infra.integration.hsa.util.HsaAttributeExtractor;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v2.Intyg;
import se.riv.infrastructure.directory.v1.CommissionType;
import se.riv.infrastructure.directory.v1.PersonInformationType;

@Component(value = "createNewDraftRequestBuilderImplV2")
public class CreateNewDraftRequestBuilderImpl implements CreateNewDraftRequestBuilder {

    @Autowired
    private HsaOrganizationsService hsaOrganizationsService;

    @Autowired
    private HsaPersonService hsaPersonService;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    private HsaAttributeExtractor hsaAttributeExtractor = new HsaAttributeExtractor();

    @Override
    public CreateNewDraftRequest buildCreateNewDraftRequest(Intyg intyg, CommissionType miuOnUnit) {
        HoSPersonal hosPerson = createHoSPerson(intyg.getSkapadAv(), createVardenhetFromMIU(miuOnUnit));
        enrichHoSPerson(hosPerson);
        return new CreateNewDraftRequest(null, moduleRegistry.getModuleIdFromExternalId(intyg.getTypAvIntyg().getCode()), null, hosPerson,
                TransportConverterUtil.getPatient(intyg.getPatient()));
    }

    private HoSPersonal createHoSPerson(
            se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v2.HosPersonal hoSPersonType,
            Vardenhet vardenhet) {
        HoSPersonal hoSPerson = new HoSPersonal();
        hoSPerson.setFullstandigtNamn(hoSPersonType.getFullstandigtNamn());
        hoSPerson.setPersonId(hoSPersonType.getPersonalId().getExtension());
        hoSPerson.setVardenhet(vardenhet);
        return hoSPerson;
    }

    private void enrichHoSPerson(HoSPersonal hosPerson) {
        List<PersonInformationType> hsaPersonResponse = hsaPersonService.getHsaPersonInfo(hosPerson.getPersonId());
        if (hsaPersonResponse != null && !hsaPersonResponse.isEmpty()) {
            // set befattningar and specialiteter from hsa response
            hosPerson.getBefattningar().addAll(hsaAttributeExtractor.extractBefattningar(hsaPersonResponse));
            hosPerson.getSpecialiteter().addAll(hsaAttributeExtractor.extractSpecialiseringar(hsaPersonResponse));
        }
    }

    private Vardenhet createVardenhetFromMIU(CommissionType miu) {

        se.inera.intyg.infra.integration.hsa.model.Vardenhet hsaVardenhet = hsaOrganizationsService
                .getVardenhet(miu.getHealthCareUnitHsaId());

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

}

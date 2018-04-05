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
package se.inera.intyg.webcert.web.integration.v3.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.modules.converter.TransportConverterUtil;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.web.integration.util.HoSPersonHelper;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;

@Component(value = "createNewDraftRequestBuilderImplV2")
public class CreateNewDraftRequestBuilderImpl implements CreateNewDraftRequestBuilder {

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Override
    public CreateNewDraftRequest buildCreateNewDraftRequest(Intyg intyg, IntygUser user) {
        HoSPersonal hosPerson = createHoSPerson(intyg.getSkapadAv(),
                HoSPersonHelper.createVardenhetFromIntygUser(intyg.getSkapadAv().getEnhet().getEnhetsId().getExtension(), user));
        HoSPersonHelper.enrichHoSPerson(hosPerson, user);
        return new CreateNewDraftRequest(null, moduleRegistry.getModuleIdFromExternalId(intyg.getTypAvIntyg().getCode()), null, hosPerson,
                TransportConverterUtil.getPatient(intyg.getPatient(), true));
    }

    private HoSPersonal createHoSPerson(
            se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.HosPersonal hoSPersonType,
            Vardenhet vardenhet) {
        HoSPersonal hoSPerson = new HoSPersonal();
        hoSPerson.setFullstandigtNamn(hoSPersonType.getFullstandigtNamn());
        hoSPerson.setPersonId(hoSPersonType.getPersonalId().getExtension());
        hoSPerson.setVardenhet(vardenhet);
        return hoSPerson;
    }
}

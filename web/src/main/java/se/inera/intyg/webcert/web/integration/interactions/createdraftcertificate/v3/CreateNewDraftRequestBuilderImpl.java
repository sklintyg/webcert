/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v3;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.integration.util.HoSPersonHelper;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v33.Forifyllnad;

@Component(value = "createNewDraftRequestBuilderImplV2")
public class CreateNewDraftRequestBuilderImpl implements CreateNewDraftRequestBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(CreateNewDraftRequestBuilderImpl.class);
    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private PatientDetailsResolver patientDetailsResolver;

    private AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();

    @Override
    public CreateNewDraftRequest buildCreateNewDraftRequest(Intyg intyg, String intygTypeVersion, IntygUser user) {
        HoSPersonal hosPerson = createHoSPerson(intyg.getSkapadAv(),
            HoSPersonHelper.createVardenhetFromIntygUser(intyg.getSkapadAv().getEnhet().getEnhetsId().getExtension(), user));
        HoSPersonHelper.enrichHoSPerson(hosPerson, user);
        String intygsType = moduleRegistry.getModuleIdFromExternalId(intyg.getTypAvIntyg().getCode());
        Optional<Forifyllnad> forifyllnad = getOptionalForifyllnadIfApplicable(intygsType, intyg.getForifyllnad(), user);

        final var patient = getPatient(intyg);

        return new CreateNewDraftRequest(null, intygsType, intygTypeVersion,
            null, hosPerson, patient, intyg.getRef(), forifyllnad);
    }

    private Patient getPatient(Intyg intyg) {
        final var pnr = intyg.getPatient().getPersonId().getExtension();
        final var personnummer = Personnummer.createPersonnummer(pnr).orElseThrow();
        return getPatientFromPU(personnummer);
    }

    private Patient getPatientFromPU(Personnummer personnummer) {
        final var personFromPUService = patientDetailsResolver.getPersonFromPUService(personnummer).getPerson();
        final var patient = new Patient();
        patient.setPersonId(personFromPUService.personnummer());
        patient.setEfternamn(personFromPUService.efternamn());
        patient.setFornamn(personFromPUService.fornamn());
        patient.setMellannamn(personFromPUService.mellannamn());
        patient.setPostort(personFromPUService.postort());
        patient.setPostnummer(personFromPUService.postnummer());
        patient.setPostadress(personFromPUService.postadress());
        patient.setFullstandigtNamn(IntygConverterUtil.concatPatientName(personFromPUService.fornamn(),
            personFromPUService.mellannamn(), personFromPUService.efternamn()));
        patient.setTestIndicator(personFromPUService.testIndicator());
        return patient;
    }

    private Optional<Forifyllnad> getOptionalForifyllnadIfApplicable(String intygsType, Forifyllnad forifyllnad, IntygUser user) {
        if (forifyllnad != null && forifyllnad.getSvar().size() > 0) {
            if (authoritiesValidator.given(user, intygsType).features(AuthoritiesConstants.FEATURE_ENABLE_CREATE_DRAFT_PREFILL)
                .isVerified()) {
                return Optional.of(forifyllnad);
            } else {
                LOG.info(
                    AuthoritiesConstants.FEATURE_ENABLE_CREATE_DRAFT_PREFILL
                        + " feature NOT enabled for " + intygsType + " but forifyllnad info was present in request.");
                return Optional.empty();
            }
        }
        return Optional.empty();
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

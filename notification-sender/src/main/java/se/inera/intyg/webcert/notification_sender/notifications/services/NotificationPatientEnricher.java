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
package se.inera.intyg.webcert.notification_sender.notifications.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.annotations.VisibleForTesting;

import se.inera.intyg.common.support.Constants;
import se.inera.intyg.common.support.validate.SamordningsnummerValidator;
import se.inera.intyg.infra.integration.pu.model.Person;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

/**
 * For SMI-intyg, use the PU-service to fetch patient details and add them to the Utlatande.
 */
public class NotificationPatientEnricher {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationPatientEnricher.class);
    private static final String SEKRETESSMARKERING = "Sekretessmarkering";
    private static final String EMPTY_STRING = "";

    @Autowired
    private PUService puService;

    public void enrichWithPatient(Intyg intyg) {
        // INTYG-4190, hämta patientens uppgifter från PU-tjänsten och klistra in i utlåtandet.
        String intygsTyp = intyg.getTyp().getCode();
        switch (intygsTyp.toLowerCase()) {
        case "luse":
        case "luae_na":
        case "luae_fs":
        case "lisjp":
            Personnummer personnummer = Personnummer.createValidatedPersonnummerWithDash(
                    intyg.getPatient().getPersonId().getExtension())
                    .orElseThrow(() -> new IllegalArgumentException("Cannot parse personnummer"));

            PersonSvar personSvar = puService.getPerson(personnummer);
            if (personSvar.getStatus() == PersonSvar.Status.FOUND) {
                if (!personSvar.getPerson().isSekretessmarkering()) {
                    intyg.setPatient(buildPatientFromPersonSvar(personSvar.getPerson()));
                } else {
                    intyg.getPatient().setEfternamn(SEKRETESSMARKERING);

                    intyg.getPatient().setFornamn(EMPTY_STRING);
                    intyg.getPatient().setMellannamn(EMPTY_STRING);
                    intyg.getPatient().setPostadress(EMPTY_STRING);
                    intyg.getPatient().setPostnummer(EMPTY_STRING);
                    intyg.getPatient().setPostort(EMPTY_STRING);
                }
            } else if (personSvar.getStatus() == PersonSvar.Status.ERROR) {
                throw new IllegalStateException("Could not query PU-service for enriching notification with patient data.");
            } else {
                LOG.warn("PU-service returned NOT_FOUND for personnummer: {}, not enriching notification.",
                        personnummer.getPnrHash());
            }
            break;
        default:
            // Do nothing
            break;
        }
    }

    private se.riv.clinicalprocess.healthcond.certificate.v3.Patient buildPatientFromPersonSvar(Person person) {
        se.riv.clinicalprocess.healthcond.certificate.v3.Patient patient = new se.riv.clinicalprocess.healthcond.certificate.v3.Patient();
        PersonId personId = new PersonId();
        personId
                .setRoot(SamordningsnummerValidator.isSamordningsNummer(person.getPersonnummer()) ? Constants.SAMORDNING_ID_OID
                        : Constants.PERSON_ID_OID);
        personId.setExtension(person.getPersonnummer().getPersonnummerWithoutDash());
        patient.setPersonId(personId);
        patient.setFornamn(person.getFornamn());
        patient.setMellannamn(person.getMellannamn());
        patient.setEfternamn(person.getEfternamn());

        patient.setPostadress(person.getPostadress());
        patient.setPostnummer(person.getPostnummer());
        patient.setPostort(person.getPostort());
        return patient;
    }

    @VisibleForTesting
    public void setPuService(PUService puService) {
        this.puService = puService;
    }
}

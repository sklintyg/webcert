package se.inera.intyg.webcert.web.service.patient;

import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.schemas.contract.Personnummer;

/**
 * Created by eriklupander on 2017-08-14.
 */
public interface PatientDetailsResolver {
    PersonSvar getPersonFromPUService(Personnummer personnummer);

    Patient resolvePatient(Personnummer personnummer, String intygsTyp);

    boolean isSekretessmarkering(Personnummer personNummer);

    SekretessStatus getSekretessStatus(Personnummer personNummer);

    boolean isAvliden(Personnummer personnummer);

    Patient updatePatientForSaving(Patient patient, String intygsTyp);
}

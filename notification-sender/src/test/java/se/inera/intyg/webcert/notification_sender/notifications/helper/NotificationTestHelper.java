package se.inera.intyg.webcert.notification_sender.notifications.helper;

import se.inera.intyg.common.support.modules.converter.InternalConverterUtil;
import se.inera.intyg.infra.integration.pu.model.Person;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.ArbetsplatsKod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PartialDateTypeFormatEnum;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;
import se.riv.clinicalprocess.healthcond.certificate.v3.Vardgivare;

import java.time.LocalDate;
import java.time.Year;

/**
 * Created by eriklupander on 2017-08-18.
 */
public class NotificationTestHelper {

    private NotificationTestHelper() {
    }

    public static Intyg createIntyg(String intygsTyp) {
        Intyg intyg = new Intyg();
        IntygId intygId = new IntygId();
        intygId.setExtension("intyg123");
        intyg.setIntygsId(intygId);

        TypAvIntyg typAvIntyg = new TypAvIntyg();
        typAvIntyg.setCode(intygsTyp);
        intyg.setTyp(typAvIntyg);

        intyg.setPatient(buildPatient());

        HosPersonal hosPersonal = new HosPersonal();
        Enhet enhet = new Enhet();
        enhet.setVardgivare(new Vardgivare());
        enhet.setArbetsplatskod(new ArbetsplatsKod());
        hosPersonal.setEnhet(enhet);
        intyg.setSkapadAv(hosPersonal);
        // DatePeriodType and PartialDateType must be allowed
        intyg.getSvar().add(InternalConverterUtil.aSvar("")
                .withDelsvar("", InternalConverterUtil.aDatePeriod(LocalDate.now(), LocalDate.now().plusDays(1)))
                .withDelsvar("", InternalConverterUtil.aPartialDate(PartialDateTypeFormatEnum.YYYY, Year.of(1999)))
                .build());
        return intyg;
    }

    public static Person buildPerson() {
        return new Person(Personnummer.createValidatedPersonnummerWithDash("191212121212").get(),
                false, false, "Tolvan", "Mellis", "Tolvansson", "Tolvgatan 12", "12121", "Tolvhult");
    }

    private static Patient buildPatient() {
        Patient patient = new Patient();
        PersonId personId = new PersonId();
        personId.setExtension("191212121212");
        patient.setPersonId(personId);
        patient.setFornamn("");
        patient.setMellannamn("");
        patient.setEfternamn("");
        patient.setPostadress("");
        patient.setPostnummer("");
        patient.setPostort("");
        return patient;
    }

}

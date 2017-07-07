package se.inera.intyg.webcert.web.service.patient;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.IntegrationParameters;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

/**
 * Created by eriklupander on 2017-07-03.
 */
public class PatientDetailsResolver {

    private static final char SPACE = ' ';

    @Autowired
    private PUService puService;

    @Autowired
    private WebCertUserService webCertUserService;

    public Patient resolvePatient(Personnummer personnummer, String intygsTyp) {

        WebCertUser user = null;
        if (webCertUserService.hasAuthenticationContext()) {
            user = webCertUserService.getUser();
        } else {
            // If this code is called in a non-user context, use a dummy user for now.
            user = new WebCertUser(new IntygUser(""));
            user.setOrigin(WebCertUserOriginType.NORMAL.name());
        }

        switch (intygsTyp) {
        case "fk7263":
        case "luse":
        case "lisjp":
        case "luae_na":
        case "luae_fs":
            return resolveFkPatient(personnummer, user);

        case "ts-bas":
        case "ts-diabetes":
            return resolveTsPatient(personnummer, user);

        case "db":
            return resolveDbPatient(personnummer, user);

        case "doi":
            return resolveDoiPatient(personnummer, user);
        default:
            throw new IllegalArgumentException("Unknown intygsTyp: " + intygsTyp);
        }

    }

    public boolean isSekretessmarkering(Personnummer personNummer) {
        PersonSvar person = puService.getPerson(personNummer);
        if (person.getStatus() == PersonSvar.Status.FOUND) {
            return person.getPerson().isSekretessmarkering();
        } else {
            return false;
        }
    }

    public SekretessStatus getSekretessStatus(Personnummer personNummer) {
        PersonSvar person = puService.getPerson(personNummer);
        if (person.getStatus() == PersonSvar.Status.FOUND) {
            if (person.getPerson().isSekretessmarkering()) {
                return SekretessStatus.TRUE;
            } else {
                return SekretessStatus.FALSE;
            }
        } else {
            return SekretessStatus.UNDEFINED;
        }
    }

    private Patient resolveFkPatient(Personnummer personnummer, WebCertUser user) {
        try {
            PersonSvar personSvar = puService.getPerson(personnummer);
            if (personSvar.getStatus() == PersonSvar.Status.FOUND) {
                return toPatientFromPersonSvarNameOnly(personnummer, personSvar);
            } else {
                throw new IllegalStateException("Could not resolve Patient from PU-service, resorting to fallback.");
            }

        } catch (Exception e) {
            // For any kind of exception, try to use IntegrationParameters, if possible.
            if (user.getOrigin().equals(WebCertUserOriginType.DJUPINTEGRATION.name()) && user.getParameters() != null) {
                // DJUPINTEGRATION
                return toPatientFromParametersNameOnly(personnummer, user.getParameters());
            } else {
                return null;
            }
        }
    }

    /*
     * I: Namn, s-markering från PU-tjänst, info om avliden.
     * I: Adress från journalsystem
     * F: PU-tjänsten (alla uppgifter)
     */
    private Patient resolveTsPatient(Personnummer personnummer, WebCertUser user) {
        try {
            PersonSvar personSvar = puService.getPerson(personnummer);
            if (personSvar.getStatus() == PersonSvar.Status.FOUND) {

                // Get address if djupintegration
                if (user.getOrigin().equals(WebCertUserOriginType.DJUPINTEGRATION.name())) {
                    Patient patient = toPatientFromPersonSvarNameOnly(personnummer, personSvar);
                    IntegrationParameters parameters = user.getParameters();
                    patient.setPostadress(parameters.getPostadress());
                    patient.setPostnummer(parameters.getPostnummer());
                    patient.setPostort(parameters.getPostort());
                } else {
                    return toPatientFromPersonSvar(personnummer, personSvar);
                }
            } else {
                throw new IllegalStateException("Could not resolve Patient from PU-service, resorting to fallback.");
            }
        } catch (Exception e) {
            /*
             * I: Namn och info om avliden från journalsystem
             * I: Adress från PU-tjänsten
             * F: Manuell inmatning av namn & adress
             */
            return toPatientFromParametersNameOnly(personnummer, user.getParameters());
        }
        return null;
    }

    private Patient resolveDbPatient(Personnummer personnummer, WebCertUser user) {
        return null;

    }

    private Patient resolveDoiPatient(Personnummer personnummer, WebCertUser user) {
        return null;
    }

    private Patient toPatientFromParameters(Personnummer personnummer, IntegrationParameters parameters) {
        Patient patient = buildBasePatientFromParameters(personnummer, parameters);

        patient.setPostadress(parameters.getPostadress());
        patient.setPostnummer(parameters.getPostnummer());
        patient.setPostort(parameters.getPostort());

        return patient;
    }

    private Patient toPatientFromParametersNameOnly(Personnummer personnummer, IntegrationParameters parameters) {
        return buildBasePatientFromParameters(personnummer, parameters);
    }

    @NotNull
    private Patient buildBasePatientFromParameters(Personnummer personnummer, IntegrationParameters parameters) {
        Patient patient = new Patient();
        patient.setPersonId(personnummer);

        patient.setFornamn(parameters.getFornamn());
        patient.setEfternamn(parameters.getEfternamn());
        patient.setMellannamn(parameters.getMellannamn());
        patient.setAvliden(parameters.isPatientDeceased());
        return patient;
    }

    private Patient toPatientFromPersonSvarNameOnly(Personnummer personnummer, PersonSvar personSvar) {
        return buildBasePatient(personnummer, personSvar);
    }

    private Patient toPatientFromPersonSvar(Personnummer personnummer, PersonSvar personSvar) {
        Patient patient = buildBasePatient(personnummer, personSvar);

        // Address
        patient.setPostadress(personSvar.getPerson().getPostadress());
        patient.setPostnummer(personSvar.getPerson().getPostnummer());
        patient.setPostort(personSvar.getPerson().getPostort());
        return patient;
    }

    private Patient buildBasePatient(Personnummer personnummer, PersonSvar personSvar) {
        Patient patient = new Patient();
        patient.setPersonId(personnummer);

        // Namn
        patient.setFornamn(personSvar.getPerson().getFornamn());
        patient.setMellannamn(personSvar.getPerson().getMellannamn());
        patient.setEfternamn(personSvar.getPerson().getEfternamn());
        patient.setFullstandigtNamn(
                IntygConverterUtil.concatPatientName(patient.getFornamn(), patient.getMellannamn(), patient.getEfternamn()));

        // Övrigt
        patient.setAvliden(personSvar.getPerson().isAvliden());
        patient.setSekretessmarkering(personSvar.getPerson().isSekretessmarkering());
        return patient;
    }

    public boolean isAvliden(Personnummer personnummer) {
        PersonSvar personSvar = puService.getPerson(personnummer);
        if (personSvar.getStatus() == PersonSvar.Status.FOUND) {
            return personSvar.getPerson().isAvliden();
        } else {
            return false;
        }
    }

    /**
     * Implements business rules where the intygsTyp determines which patient information that's saved to the backend.
     *
     * @param patient
     * @param intygsTyp
     * @return
     *      An updated patient DTO with the non-relevant fields nulled out.
     */
    public Patient updatePatientForSaving(Patient patient, String intygsTyp) {

        Patient retPatient = new Patient();
        // Always transfer
        retPatient.setSekretessmarkering(patient.isSekretessmarkering());
        retPatient.setAvliden(patient.isAvliden());

        switch (intygsTyp) {
        case "fk7263":
        case "luse":
        case "lisjp":
        case "luae_na":
        case "luae_fs":
            // For FK intyg, never save anything other than personnummer.

            retPatient.setPersonId(patient.getPersonId());
            break;

        case "ts-bas":
        case "ts-diabetes":
            // For TS-intyg, return the patient "as-is"
            return patient;

        case "db":
        case "doi":
            // For DB/DOI, return only personnummer and name, no address.

            retPatient.setPersonId(patient.getPersonId());
            retPatient.setFornamn(patient.getFornamn());
            retPatient.setMellannamn(patient.getMellannamn());
            retPatient.setEfternamn(patient.getEfternamn());
            break;
        default:
            throw new IllegalArgumentException("Unknown intygsTyp: " + intygsTyp);
        }

        return retPatient;
    }
}

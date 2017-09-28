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
package se.inera.intyg.webcert.web.service.patient;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.integration.validator.IntygsTypToInternal;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.IntegrationParameters;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is responsible for implementing GE-002, e.g. requirements on how to fetch patient details for a given
 * intygstyp.
 *
 * It's over-complex and needs refactoring.
 *
 * Created by eriklupander on 2017-07-03.
 */
public class PatientDetailsResolverImpl implements PatientDetailsResolver {

    private static final List<UtkastStatus> UTKAST_STATUSES = Arrays.asList(UtkastStatus.DRAFT_INCOMPLETE, UtkastStatus.DRAFT_COMPLETE,
            UtkastStatus.SIGNED);

    @Autowired
    private PUService puService;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    /**
     * Just forwards the call to the PU service so we don't have to expose both this class and {@link PUService} in
     * certain consumer classes.
     */
    @Override
    public PersonSvar getPersonFromPUService(Personnummer personnummer) {
        return puService.getPerson(personnummer);
    }

    @Override
    public Patient resolvePatient(Personnummer personnummer, String intygsTyp) {

        WebCertUser user;
        if (webCertUserService.hasAuthenticationContext()) {
            user = webCertUserService.getUser();
        } else {
            throw new IllegalStateException("The PatientDetailsResolver#resolvePatient method cannot be used without a "
                    + "valid authentication context");
        }

        // Make sure any external intygstyp representations (such as TSTRK1007) are mapped to our internal types.
        String internalIntygsTyp = IntygsTypToInternal.convertToInternalIntygsTyp(intygsTyp);

        switch (internalIntygsTyp.toLowerCase()) {
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

    @Override
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

    @Override
    public boolean isAvliden(Personnummer personnummer) {
        PersonSvar personSvar = puService.getPerson(personnummer);
        if (personSvar.getStatus() == PersonSvar.Status.FOUND) {
            return personSvar.getPerson().isAvliden();
        } else {
            return false;
        }
    }

    @Override
    public boolean isPatientAddressChanged(Patient oldPatient, Patient newPatient) {
        return (oldPatient.getPostadress() != null && !oldPatient.getPostadress().equals(newPatient.getPostadress()))
                || (oldPatient.getPostnummer() != null && !oldPatient.getPostnummer().equals(newPatient.getPostnummer()))
                || (oldPatient.getPostort() != null && !oldPatient.getPostort().equals(newPatient.getPostort()));
    }

    @Override
    public boolean isPatientNamedChanged(Patient oldPatient, Patient newPatient) {
        return (oldPatient.getFornamn() != null && !oldPatient.getFornamn().equals(newPatient.getFornamn()))
                || (oldPatient.getEfternamn() != null && !oldPatient.getEfternamn().equals(newPatient.getEfternamn()));
    }

    /**
     * Implements business rules where the intygsTyp determines which patient information that's saved to the backend.
     *
     * @param patient
     * @param intygsTyp
     * @return
     *         An updated patient DTO with the non-relevant fields nulled out.
     */
    @Override
    public Patient updatePatientForSaving(Patient patient, String intygsTyp) {

        Patient retPatient = new Patient();
        // Always transfer
        retPatient.setSekretessmarkering(patient.isSekretessmarkering());
        retPatient.setAvliden(patient.isAvliden());

        // Make sure any external intygstyp representations (such as TSTRK1007) are mapped to our internal types.
        String internalIntygsTyp = IntygsTypToInternal.convertToInternalIntygsTyp(intygsTyp);

        switch (internalIntygsTyp.toLowerCase()) {
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

    private Patient resolveFkPatient(Personnummer personnummer, WebCertUser user) {
        PersonSvar personSvar = puService.getPerson(personnummer);
        if (personSvar.getStatus() == PersonSvar.Status.FOUND) {
            return toPatientFromPersonSvarNameOnly(personnummer, personSvar);
        } else {
            return resolveFkPatientPuUnavailable(personnummer, user);
        }
    }

    private Patient resolveFkPatientPuUnavailable(Personnummer personnummer, WebCertUser user) {
        if (user.getOrigin().equals(WebCertUserOriginType.DJUPINTEGRATION.name()) && user.getParameters() != null) {
            // DJUPINTEGRATION
            return toPatientFromParametersNameOnly(personnummer, user.getParameters());
        } else {
            // Perhaps return Patient instance with personnummer only?
            return null;
        }
    }

    /*
     * I: Namn, s-markering från PU-tjänst, info om avliden.
     * I: Adress från journalsystem
     * F: PU-tjänsten (alla uppgifter)
     */
    private Patient resolveTsPatient(Personnummer personnummer, WebCertUser user) {

        PersonSvar personSvar = puService.getPerson(personnummer);
        if (personSvar.getStatus() == PersonSvar.Status.FOUND) {

            // Get address if djupintegration from params, fallback to PU for address if unavailable.
            if (user.getOrigin().equals(WebCertUserOriginType.DJUPINTEGRATION.name())) {
                Patient patient = toPatientFromPersonSvarNameOnly(personnummer, personSvar);
                IntegrationParameters parameters = user.getParameters();

                // All address fields needs to be present from Journalsystem, otherwise use PU instead.
                if (isNotNullOrEmpty(parameters.getPostadress()) && isNotNullOrEmpty(parameters.getPostnummer())
                        && isNotNullOrEmpty(parameters.getPostort())) {
                    patient.setPostadress(parameters.getPostadress());
                    patient.setPostnummer(parameters.getPostnummer());
                    patient.setPostort(parameters.getPostort());
                } else {
                    patient.setPostadress(personSvar.getPerson().getPostadress());
                    patient.setPostnummer(personSvar.getPerson().getPostnummer());
                    patient.setPostort(personSvar.getPerson().getPostort());
                }

                return patient;
            } else {
                return toPatientFromPersonSvar(personnummer, personSvar);
            }
        } else {
            // Om PU-svar saknas helt skall allt som går hämtas från Integrationsparametrar om sådana finns.
            if (user.getOrigin().equals(WebCertUserOriginType.DJUPINTEGRATION.name())) {
                return toPatientFromParameters(personnummer, user.getParameters());
            } else {
                // Om vi varken har DJUPINTEGRATION parametrar eller PU så blir det manuellt. Eller ska vi köra på
                // ett Patient-objekt med endast personnummer?
                return null;
            }
        }

    }

    /**
     * Integration: Namn, s-markering från PU-tjänst & info om avliden. Journalsystem för adress.
     * Free: PU-tjänsten (alla uppgifter)
     */
    private Patient resolveDbPatient(Personnummer personnummer, WebCertUser user) {
        PersonSvar personSvar = puService.getPerson(personnummer);

        // Djupintegration
        if (user.getOrigin().equals(WebCertUserOriginType.DJUPINTEGRATION.name())) {

            // Om svar från PU finns, kombinera ihop PU för namn och s-mark medan adress från J-system.
            if (personSvar.getStatus() == PersonSvar.Status.FOUND) {
                Patient patient = toPatientFromPersonSvarNameOnly(personnummer, personSvar);
                IntegrationParameters parameters = user.getParameters();
                if (isNotNullOrEmpty(parameters.getPostadress()) && isNotNullOrEmpty(parameters.getPostnummer())
                        && isNotNullOrEmpty(parameters.getPostort())) {
                    patient.setPostadress(parameters.getPostadress());
                    patient.setPostnummer(parameters.getPostnummer());
                    patient.setPostort(parameters.getPostort());
                    return patient;
                } else {
                    // Manuell inmatning, dvs lämna fälten nullade.
                    return patient;
                }

            } else {
                // Om patient ej finns i PU alls, bygg namn från parametrar och lämna adress tomt för manuell ifyllnad.
                return toPatientFromParameters(personnummer, user.getParameters());
            }

        } else {
            // Fristående byggs helt från PU, om möjligt
            if (personSvar.getStatus() == PersonSvar.Status.FOUND) {
                return toPatientFromPersonSvar(personnummer, personSvar);
            } else {
                // Om ej svar från PU, returnera blankt för manuell ifyllnad.
                return null;
            }
        }

    }

    /**
     * DOI har en specialregel som säger att namn och adress skall hämtas från ett DB-intyg i det fall sådant finns
     * utfärdat inom samma Vårdgivare (Integration) eller Vårdenhet (Fristående).
     */
    private Patient resolveDoiPatient(Personnummer personnummer, WebCertUser user) {

        PersonSvar personSvar = puService.getPerson(personnummer);

        // Find ALL existing intyg for this patient, filter out so we only have DB left.
        // Förstahands-valen för sos_doi är identiska oavett Integration eller Fristående
        List<Utkast> utkastList = new ArrayList<>();
        if (user.getOrigin().equals(WebCertUserOriginType.DJUPINTEGRATION.name())) {
            utkastList.addAll(utkastRepository.findDraftsByPatientAndVardgivareAndStatus(personnummer.getPersonnummer(),
                    user.getValdVardgivare().getId(),
                    UTKAST_STATUSES, Sets.newHashSet("db")));
        } else {
            utkastList.addAll(utkastRepository.findDraftsByPatientAndEnhetAndStatus(personnummer.getPersonnummer(),
                    Arrays.asList(user.getValdVardenhet().getId()), UTKAST_STATUSES,
                    Sets.newHashSet("db")));
        }

        // If any utkast were found, take the newest one and transfer name & address från DB intyg. (Use PU for s-markering and
        // deceased)
        if (utkastList.size() > 0) {
            Utkast newest = utkastList.stream()
                    .sorted((u1, u2) -> u2.getSenastSparadDatum().compareTo(u1.getSenastSparadDatum()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("List was > 0 but findFirst() returned no result."));

            try {
                ModuleApi moduleApi = moduleRegistry.getModuleApi("db");
                Utlatande utlatande = moduleApi.getUtlatandeFromJson(newest.getModel());
                Patient patient = utlatande.getGrundData().getPatient();
                if (personSvar.getStatus() == PersonSvar.Status.FOUND) {
                    patient.setAvliden(personSvar.getPerson().isAvliden());
                    patient.setSekretessmarkering(personSvar.getPerson().isSekretessmarkering());
                } else if (user.getParameters() != null) {
                    patient.setAvliden(user.getParameters().isPatientDeceased());
                } else {
                    // Avliden ej möjlig att ta reda på. Men eftersom vi skriver DOI-intyg kan vi förmoda att patienten är död.
                    patient.setAvliden(true);
                }
                return patient;
            } catch (ModuleNotFoundException | IOException e) {
                // Vid fel här, gå vidare utifrån att DB-intyg saknas.
                return handleDoiNoExistingDb(personnummer, personSvar, user);
            }
        } else {
            // Om ej existerande och användbar DB verkar finnas, fortsätt här.
            return handleDoiNoExistingDb(personnummer, personSvar, user);
        }
    }

    private Patient handleDoiNoExistingDb(Personnummer personnummer, PersonSvar personSvar, WebCertUser user) {

        // Handle DJUPINTEGRATION
        if (user.getOrigin().equals(WebCertUserOriginType.DJUPINTEGRATION.name())) {
            // Hämta namn från PU, om möjligt
            if (personSvar.getStatus() == PersonSvar.Status.FOUND) {
                // Hämta namn från PU
                Patient patient = toPatientFromPersonSvarNameOnly(personnummer, personSvar);
                // Address från Integrationsparametrar
                patient.setPostadress(user.getParameters().getPostadress());
                patient.setPostnummer(user.getParameters().getPostnummer());
                patient.setPostort(user.getParameters().getPostort());
                return patient;
            } else {
                // Om PU saknas, bygg så gott det går från parmetrar.
                return toPatientFromParameters(personnummer, user.getParameters());
            }
        } else {
            // HANDLE FRISTÅENDE
            if (personSvar.getStatus() == PersonSvar.Status.FOUND) {
                return toPatientFromPersonSvar(personnummer, personSvar);
            } else {
                // Knappa in manuellt...
                return null;
            }
        }
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

    private Patient buildBasePatientFromParameters(Personnummer personnummer, IntegrationParameters parameters) {
        Patient patient = new Patient();
        patient.setPersonId(personnummer);

        patient.setFornamn(parameters.getFornamn());
        patient.setEfternamn(parameters.getEfternamn());
        patient.setMellannamn(parameters.getMellannamn());
        patient.setFullstandigtNamn(Joiner.on(' ').skipNulls().join(parameters.getFornamn(), parameters.getMellannamn(),
                parameters.getEfternamn()));
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
                Joiner.on(' ').skipNulls().join(personSvar.getPerson().getFornamn(), personSvar.getPerson().getMellannamn(),
                        personSvar.getPerson().getEfternamn()));

        // Övrigt
        patient.setAvliden(personSvar.getPerson().isAvliden());
        patient.setSekretessmarkering(personSvar.getPerson().isSekretessmarkering());
        return patient;
    }

    private boolean isNotNullOrEmpty(String value) {
        return !Strings.isNullOrEmpty(value);
    }
}

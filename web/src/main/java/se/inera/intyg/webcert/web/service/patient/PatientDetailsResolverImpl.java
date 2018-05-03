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
package se.inera.intyg.webcert.web.service.patient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.integration.converters.IntygsTypToInternal;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

/**
 * This class is responsible for implementing GE-002, e.g. requirements on how to fetch patient details for a given
 * intygstyp.
 * <p>
 * It's over-complex and needs refactoring.
 * <p>
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
        return getPersonSvar(personnummer);
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
        PersonSvar person = getPersonSvar(personNummer);
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
    public Map<Personnummer, SekretessStatus> getSekretessStatusForList(List<Personnummer> personnummerList) {
        Map<Personnummer, SekretessStatus> sekretessStatusMap = new HashMap<>();
        if (personnummerList == null || personnummerList.size() == 0) {
            return sekretessStatusMap;
        }

        // Make sure we don't ask twice for a given personnummer.
        List<Personnummer> distinctPersonnummerList = personnummerList.stream().distinct().collect(Collectors.toList());

        Map<Personnummer, PersonSvar> persons = puService.getPersons(distinctPersonnummerList);
        persons.entrySet().stream().forEach(entry -> {
            if (entry.getValue() != null && entry.getValue().getStatus() == PersonSvar.Status.FOUND) {
                sekretessStatusMap.put(entry.getKey(),
                        entry.getValue().getPerson().isSekretessmarkering() ? SekretessStatus.TRUE : SekretessStatus.FALSE);
            } else {
                // contains no person instance.
                sekretessStatusMap.put(entry.getKey(), SekretessStatus.UNDEFINED);
            }
        });

        return sekretessStatusMap;
    }

    @Override
    public boolean isAvliden(Personnummer personnummer) {
        PersonSvar personSvar = getPersonSvar(personnummer);
        boolean avlidenPU = personSvar.getStatus() == PersonSvar.Status.FOUND && personSvar.getPerson().isAvliden();

        WebCertUser user = webCertUserService.hasAuthenticationContext() ? webCertUserService.getUser() : null;
        boolean avlidenIntegration = user != null && user.getParameters() != null && user.getParameters().isPatientDeceased();

        return avlidenPU || avlidenIntegration;
    }

    @Override
    public boolean isPatientAddressChanged(Patient oldPatient, Patient newPatient) {
        return (oldPatient != null && newPatient == null)
                || (oldPatient.getPostadress() != null && !oldPatient.getPostadress().equals(newPatient.getPostadress()))
                || (oldPatient.getPostnummer() != null && !oldPatient.getPostnummer().equals(newPatient.getPostnummer()))
                || (oldPatient.getPostort() != null && !oldPatient.getPostort().equals(newPatient.getPostort()));
    }

    @Override
    public boolean isPatientNamedChanged(Patient oldPatient, Patient newPatient) {
        return (oldPatient != null && newPatient == null)
                || (oldPatient.getFornamn() != null && !oldPatient.getFornamn().equals(newPatient.getFornamn()))
                || (oldPatient.getEfternamn() != null && !oldPatient.getEfternamn().equals(newPatient.getEfternamn()));
    }

    private PersonSvar getPersonSvar(Personnummer personnummer) {
        if (personnummer == null) {
            String errMsg = "No personnummer present. Unable to make a call to PUService";
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM, errMsg);
        }

        return puService.getPerson(personnummer);
    }

    /*
     * I: Info om avliden från både PU-tjänst och journalsystem.
     */
    private Patient resolveFkPatient(Personnummer personnummer, WebCertUser user) {
        PersonSvar personSvar = getPersonSvar(personnummer);
        Patient patient = personSvar.getStatus() == PersonSvar.Status.FOUND
                ? toPatientFromPersonSvarNameOnly(personnummer, personSvar)
                : resolveFkPatientPuUnavailable(personnummer, user);
        if (patient != null) {
            patient.setAvliden(patient.isAvliden()
                    || Optional.ofNullable(user.getParameters()).map(IntegrationParameters::isPatientDeceased).orElse(false));
        }
        return patient;
    }

    private Patient resolveFkPatientPuUnavailable(Personnummer personnummer, WebCertUser user) {
        if (user.getOrigin().equals(UserOriginType.DJUPINTEGRATION.name()) && user.getParameters() != null) {
            return toPatientFromParametersNameOnly(personnummer, user.getParameters());
        } else {
            return null;
        }
    }

    /*
     * I: Namn, s-markering från PU-tjänst.
     * I: Info om avliden från både PU-tjänst och journalsystem.
     * I: Adress från journalsystem.
     * F: PU-tjänsten (alla uppgifter).
     */
    private Patient resolveTsPatient(Personnummer personnummer, WebCertUser user) {
        PersonSvar personSvar = getPersonSvar(personnummer);
        if (personSvar.getStatus() == PersonSvar.Status.FOUND) {
            Patient patient = toPatientFromPersonSvar(personnummer, personSvar);

            // Get address if djupintegration from params, fallback to PU for address if unavailable.
            if (user.getOrigin().equals(UserOriginType.DJUPINTEGRATION.name())) {
                IntegrationParameters parameters = user.getParameters();
                // Loading utkast without uthoppslänk would fail during end-to-end tests, thus the line below
                if (parameters == null) {
                    parameters = new IntegrationParameters(null, null, null, null, null, null, null, null, null, false, false, false,
                            false);
                }

                // Update avliden with integrationparameters
                patient.setAvliden(patient.isAvliden() || parameters.isPatientDeceased());

                // All address fields needs to be present from integration parameters, otherwise use PU instead.
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
            }
            return patient;
        } else {
            // No PU means only use integration parameters
            if (user.getOrigin().equals(UserOriginType.DJUPINTEGRATION.name())) {
                return toPatientFromParameters(personnummer, user.getParameters());
            } else {
                return null;
            }
        }

    }

    /**
     * Integration: Namn, s-markering från PU-tjänst & info om avliden. Journalsystem för adress.
     * Free: PU-tjänsten (alla uppgifter)
     */
    private Patient resolveDbPatient(Personnummer personnummer, WebCertUser user) {
        PersonSvar personSvar = getPersonSvar(personnummer);

        Patient patient = null;
        // NORMAL and DJUPINTEGRATION uses only PU for db
        if (personSvar.getStatus() == PersonSvar.Status.FOUND) {
            patient = toPatientFromPersonSvar(personnummer, personSvar);
        }
        return patient;
    }

    /**
     * DOI har en specialregel som säger att namn och adress skall hämtas från ett DB-intyg i det fall sådant finns
     * utfärdat inom samma Vårdgivare (Integration) eller Vårdenhet (Fristående).
     */
    private Patient resolveDoiPatient(Personnummer personnummer, WebCertUser user) {

        PersonSvar personSvar = getPersonSvar(personnummer);

        // Find ALL existing intyg for this patient, filter out so we only have DB left.
        List<Utkast> utkastList = new ArrayList<>();
        if (user.getOrigin().equals(UserOriginType.DJUPINTEGRATION.name())) {
            utkastList.addAll(utkastRepository.findDraftsByPatientAndVardgivareAndStatus(personnummer.getPersonnummerWithDash(),
                    user.getValdVardgivare().getId(),
                    UTKAST_STATUSES, Sets.newHashSet("db")));
        } else {
            utkastList.addAll(utkastRepository.findDraftsByPatientAndEnhetAndStatus(personnummer.getPersonnummerWithDash(),
                    Arrays.asList(user.getValdVardenhet().getId()), UTKAST_STATUSES,
                    Sets.newHashSet("db")));
        }

        // If any utkast were found, take the newest one and transfer name & address från DB intyg.
        // Use PU for s-markering
        // Use PU and integration parameters for deceased
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
                    patient.setSekretessmarkering(personSvar.getPerson().isSekretessmarkering());
                }
                patient.setAvliden(
                        (personSvar.getStatus() == PersonSvar.Status.FOUND && personSvar.getPerson().isAvliden())
                                || (user.getParameters() != null && user.getParameters().isPatientDeceased())
                                || (personSvar.getStatus() != PersonSvar.Status.FOUND && user.getParameters() == null));
                return patient;
            } catch (ModuleNotFoundException | IOException e) {
                // No usable DB exist
                return handleDoiNoExistingDb(personnummer, personSvar, user);
            }
        } else {
            // No usable DB exist
            return handleDoiNoExistingDb(personnummer, personSvar, user);
        }
    }

    private Patient handleDoiNoExistingDb(Personnummer personnummer, PersonSvar personSvar, WebCertUser user) {
        Patient patient = null;
        // NORMAL and DJUPINTEGRATION uses only PU for doi
        if (personSvar.getStatus() == PersonSvar.Status.FOUND) {
            patient = toPatientFromPersonSvar(personnummer, personSvar);
        }
        return patient;
    }

    private Patient toPatientFromParameters(Personnummer personnummer, IntegrationParameters parameters) {
        Patient patient = buildBasePatientFromParameters(personnummer, parameters);

        patient.setPostadress(parameters.getPostadress());
        patient.setPostnummer(parameters.getPostnummer());
        patient.setPostort(parameters.getPostort());
        patient.setAvliden(parameters.isPatientDeceased());

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

        // Name
        patient.setFornamn(personSvar.getPerson().getFornamn());
        patient.setMellannamn(personSvar.getPerson().getMellannamn());
        patient.setEfternamn(personSvar.getPerson().getEfternamn());
        patient.setFullstandigtNamn(
                Joiner.on(' ').skipNulls().join(personSvar.getPerson().getFornamn(), personSvar.getPerson().getMellannamn(),
                        personSvar.getPerson().getEfternamn()));

        // Other
        patient.setAvliden(personSvar.getPerson().isAvliden());
        patient.setSekretessmarkering(personSvar.getPerson().isSekretessmarkering());
        return patient;
    }

    private boolean isNotNullOrEmpty(String value) {
        return !Strings.isNullOrEmpty(value);
    }
}

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
package se.inera.intyg.webcert.web.service.patient;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.PatientDetailResolveOrder;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.pu.integration.api.model.PersonSvar;
import se.inera.intyg.infra.pu.integration.api.services.PUService;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
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

    private static final List<UtkastStatus> UTKAST_STATUSES = Arrays.asList(UtkastStatus.DRAFT_INCOMPLETE,
        UtkastStatus.DRAFT_COMPLETE, UtkastStatus.SIGNED);

    private static final Logger LOG = LoggerFactory.getLogger(PatientDetailsResolverImpl.class);
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
    public SekretessStatus getSekretessStatus(Personnummer personNummer) {
        PersonSvar person = getPersonSvar(personNummer);
        if (person.getStatus() == PersonSvar.Status.FOUND) {
            if (person.getPerson().sekretessmarkering()) {
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
        persons.forEach((key, value) -> {
            if (value != null && value.getStatus() == PersonSvar.Status.FOUND) {
                sekretessStatusMap.put(key,
                    value.getPerson().sekretessmarkering() ? SekretessStatus.TRUE : SekretessStatus.FALSE);
            } else {
                // contains no person instance.
                sekretessStatusMap.put(key, SekretessStatus.UNDEFINED);
            }
        });

        return sekretessStatusMap;
    }

    @Override
    public Map<Personnummer, PatientDetailsResolverResponse> getPersonStatusesForList(List<Personnummer> personnummerList) {
        Map<Personnummer, PatientDetailsResolverResponse> statusMap = new HashMap<>();
        if (personnummerList == null || personnummerList.size() == 0) {
            return statusMap;
        }

        List<Personnummer> distinctPersonnummerList = personnummerList.stream().distinct().collect(Collectors.toList());

        Map<Personnummer, PersonSvar> persons = puService.getPersons(distinctPersonnummerList);
        persons.forEach((key, value) -> {
            PatientDetailsResolverResponse patientResponse = new PatientDetailsResolverResponse();
            if (value != null && value.getStatus() == PersonSvar.Status.FOUND) {
                patientResponse.setDeceased(value.getPerson().avliden());
                patientResponse.setTestIndicator(value.getPerson().testIndicator());
                patientResponse.setProtectedPerson(value.getPerson().sekretessmarkering() ? SekretessStatus.TRUE : SekretessStatus.FALSE);
            } else {
                patientResponse.setDeceased(false);
                patientResponse.setTestIndicator(true);
                patientResponse.setProtectedPerson(SekretessStatus.UNDEFINED);
            }
            statusMap.put(key, patientResponse);
        });

        return statusMap;
    }

    @Override
    public boolean isAvliden(Personnummer personnummer) {
        PersonSvar personSvar = getPersonSvar(personnummer);
        return personSvar.getStatus() == PersonSvar.Status.FOUND && personSvar.getPerson().avliden();
    }

    @Override
    public boolean isTestIndicator(Personnummer personnummer) {
        PersonSvar personSvar = getPersonSvar(personnummer);
        return personSvar.getPerson().testIndicator();
    }

    @Override
    public boolean isPatientAddressChanged(Patient oldPatient, Patient newPatient) {
        return oldPatient != null && (newPatient == null
            || (oldPatient.getPostadress() != null && !oldPatient.getPostadress().equals(newPatient.getPostadress()))
            || (oldPatient.getPostnummer() != null && !oldPatient.getPostnummer().equals(newPatient.getPostnummer()))
            || (oldPatient.getPostort() != null && !oldPatient.getPostort().equals(newPatient.getPostort())));
    }

    @Override
    public boolean isPatientNamedChanged(Patient oldPatient, Patient newPatient) {
        return oldPatient != null && (newPatient == null
            || (oldPatient.getFornamn() != null && !oldPatient.getFornamn().equals(newPatient.getFornamn()))
            || (oldPatient.getEfternamn() != null && !oldPatient.getEfternamn().equals(newPatient.getEfternamn())));
    }

    @Override
    @Transactional(readOnly = true)
    public Patient resolvePatient(Personnummer personnummer, String intygsTyp, String intygsTypVersion) {
        WebCertUser user = null;
        if (webCertUserService.hasAuthenticationContext()) {
            user = webCertUserService.getUser();
        }

        // Make sure any external intygstyp representations (such as TSTRK1007) are mapped to our internal types.
        String internalIntygsTyp = intygsTyp;
        if (!moduleRegistry.moduleExists(intygsTyp)) {
            internalIntygsTyp = moduleRegistry.getModuleIdFromExternalId(intygsTyp);
        }

        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(internalIntygsTyp, intygsTypVersion);
            PatientDetailResolveOrder resolveOrder = moduleApi.getPatientDetailResolveOrder();
            if (user != null) {
                return resolvePatient(personnummer, user, resolveOrder);
            } else {
                return resolvePatient(personnummer);
            }
        } catch (ModuleNotFoundException e) {
            throw new IllegalArgumentException("Unknown intygsTyp: " + intygsTyp);
        }
    }

    private Patient resolvePatient(Personnummer personnummer, WebCertUser user, PatientDetailResolveOrder resolveOrder) {
        PersonSvar personSvar = getPersonSvar(personnummer);

        //PU unavailable
        if (personSvar.getStatus().equals(PersonSvar.Status.ERROR)) {
            return null;
        }

        Utlatande predecessor = null;

        // If any strategy contains PREDECESSOR, do the lookup ONCE
        if (isPredecessorStrategy(resolveOrder)) {
            List<Utkast> utkastList = getPredecessor(personnummer, user, resolveOrder.getPredecessorType());
            if (utkastList.size() > 0) {
                Utkast newest = utkastList.stream()
                    .sorted((u1, u2) -> u2.getSenastSparadDatum().compareTo(u1.getSenastSparadDatum()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("List was > 0 but findFirst() returned no result."));
                try {
                    ModuleApi moduleApi = moduleRegistry.getModuleApi(resolveOrder.getPredecessorType(), newest.getIntygTypeVersion());
                    predecessor = moduleApi.getUtlatandeFromJson(newest.getModel());
                } catch (ModuleException | ModuleNotFoundException | IOException e) {
                    LOG.info("No predecessor found!");
                }
            }
        }

        Patient patient = new Patient();
        patient.setPersonId(personnummer);

        if (resolveOrder.getAdressStrategy() != null) {
            resolvePatientAdressDetails(patient, resolveOrder, personSvar, user, predecessor);
        }

        resolvePatientAvlidenDetails(patient, personSvar);

        if (resolveOrder.getOtherStrategy() != null) {
            resolvePatientOtherDetails(patient, resolveOrder, personSvar, user, predecessor);
        }

        patient.setTestIndicator(personSvar.getPerson().testIndicator());

        return patient;
    }

    private Patient resolvePatient(Personnummer personnummer) {
        PersonSvar personSvar = getPersonSvar(personnummer);

        //PU unavailable
        if (personSvar.getStatus().equals(PersonSvar.Status.ERROR)) {
            return null;
        }

        Patient patient = new Patient();
        patient.setPersonId(personnummer);
        resolvePatientAvlidenDetails(patient, personSvar);
        patient.setTestIndicator(personSvar.getPerson().testIndicator());

        return patient;
    }

    private void resolvePatientAdressDetails(Patient patient, PatientDetailResolveOrder resolveOrder,
        PersonSvar personSvar, WebCertUser user, Utlatande predecessor) {
        List<PatientDetailResolveOrder.ResolveOrder> adressStrategy = resolveOrder.getAdressStrategy();
        int index = 0;
        boolean done = false;
        while (index < adressStrategy.size() && !done) {
            switch (adressStrategy.get(index)) {
                case PARAMS:
                    done = setAdressFromParams(patient, user);
                    break;
                case PU:
                    done = setAdressFromPu(patient, personSvar);
                    break;
                case PREDECESSOR:
                    done = setAdressFromPredecessor(patient, predecessor);
                    break;
            }
            index++;
        }
    }

    private void resolvePatientAvlidenDetails(Patient patient, PersonSvar personSvar) {
        setAvlidenFromPu(patient, personSvar);
    }

    private void resolvePatientOtherDetails(Patient patient, PatientDetailResolveOrder resolveOrder,
        PersonSvar personSvar, WebCertUser user, Utlatande predecessor) {
        List<PatientDetailResolveOrder.ResolveOrder> otherStrategy = resolveOrder.getOtherStrategy();
        int index = 0;
        boolean done = false;
        while (index < otherStrategy.size() && !done) {
            switch (otherStrategy.get(index)) {
                case PARAMS:
                    done = setOtherFromParams(patient, user);
                    break;
                case PU:
                    done = setOtherFromPu(patient, personSvar);
                    break;
                case PREDECESSOR:
                    done = setOtherFromPredecessor(patient, predecessor);
                    break;
            }
            index++;
        }
    }

    private boolean setOtherFromPredecessor(Patient patient, Utlatande predecessor) {
        if (predecessor == null) {
            return false;
        }
        Patient from = predecessor.getGrundData().getPatient();
        patient.setFornamn(from.getFornamn());
        patient.setEfternamn(from.getEfternamn());
        patient.setMellannamn(from.getMellannamn());
        patient.setFullstandigtNamn(from.getFullstandigtNamn());
        patient.setSekretessmarkering(from.isSekretessmarkering());
        return true;
    }

    private boolean setOtherFromPu(Patient patient, PersonSvar personSvar) {
        if (personSvar.getStatus().equals(PersonSvar.Status.FOUND)) {
            patient.setFornamn(personSvar.getPerson().fornamn());
            patient.setEfternamn(personSvar.getPerson().efternamn());
            patient.setMellannamn(personSvar.getPerson().mellannamn());
            patient.setFullstandigtNamn(Joiner.on(' ').skipNulls().join(personSvar.getPerson().fornamn(),
                personSvar.getPerson().mellannamn(), personSvar.getPerson().efternamn()));
            patient.setSekretessmarkering(personSvar.getPerson().sekretessmarkering());
            return true;
        }
        return false;
    }

    private boolean setOtherFromParams(Patient patient, WebCertUser user) {
        if (user.getOrigin().equals(UserOriginType.DJUPINTEGRATION.name())) {
            IntegrationParameters parameters = user.getParameters();
            patient.setFornamn(parameters.getFornamn());
            patient.setEfternamn(parameters.getEfternamn());
            patient.setMellannamn(parameters.getMellannamn());
            patient.setFullstandigtNamn(Joiner.on(' ').skipNulls().join(parameters.getFornamn(), parameters.getMellannamn(),
                parameters.getEfternamn()));
            return true;
        }
        return false;
    }

    private boolean setAvlidenFromPu(Patient patient, PersonSvar personSvar) {
        if (personSvar.getStatus().equals(PersonSvar.Status.FOUND)) {
            patient.setAvliden(personSvar.getPerson().avliden());
            return true;
        }
        return false;
    }

    private boolean setAdressFromPredecessor(Patient patient, Utlatande predessor) {
        if (predessor == null) {
            return false;
        }
        patient.setPostort(predessor.getGrundData().getPatient().getPostort());
        patient.setPostnummer(predessor.getGrundData().getPatient().getPostnummer());
        patient.setPostadress(predessor.getGrundData().getPatient().getPostadress());
        return true;
    }

    private boolean setAdressFromPu(Patient patient, PersonSvar personSvar) {
        if (personSvar.getStatus().equals(PersonSvar.Status.FOUND)) {
            patient.setPostadress(personSvar.getPerson().postadress());
            patient.setPostnummer(personSvar.getPerson().postnummer());
            patient.setPostort(personSvar.getPerson().postort());
            patient.setAddressDetailsSourcePU(patient.isCompleteAddressProvided());
            return true;
        }
        return false;
    }

    private boolean setAdressFromParams(Patient patient, WebCertUser user) {
        if (user.getOrigin().equals(UserOriginType.DJUPINTEGRATION.name())
            && user.getParameters() != null
            && isNotNullOrEmpty(user.getParameters().getPostadress())
            && isNotNullOrEmpty(user.getParameters().getPostnummer())
            && isNotNullOrEmpty(user.getParameters().getPostort())) {
            patient.setPostadress(user.getParameters().getPostadress());
            patient.setPostnummer(user.getParameters().getPostnummer());
            patient.setPostort(user.getParameters().getPostort());
            return true;
        }
        return false;
    }

    private PersonSvar getPersonSvar(Personnummer personnummer) {
        if (personnummer == null) {
            String errMsg = "No personnummer present. Unable to make a call to PUService";
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM, errMsg);
        }

        return puService.getPerson(personnummer);
    }

    private List<Utkast> getPredecessor(Personnummer personnummer, WebCertUser user, String intygsTyp) {
        // Find ALL existing intyg for this patient, filter out so we only have DB left.
        List<Utkast> utkastList = new ArrayList<>();
        if (user.getOrigin().equals(UserOriginType.DJUPINTEGRATION.name())) {
            utkastList.addAll(utkastRepository.findDraftsByPatientAndVardgivareAndStatus(personnummer.getPersonnummerWithDash(),
                user.getValdVardgivare().getId(),
                UTKAST_STATUSES, Sets.newHashSet(intygsTyp)));
        } else {
            utkastList.addAll(utkastRepository.findDraftsByPatientAndEnhetAndStatus(personnummer.getPersonnummerWithDash(),
                Collections.singletonList(user.getValdVardenhet().getId()), UTKAST_STATUSES,
                Sets.newHashSet(intygsTyp)));
        }
        return utkastList;
    }

    private boolean isPredecessorStrategy(PatientDetailResolveOrder resolveOrder) {
        return (resolveOrder.getAdressStrategy() != null
            && resolveOrder.getAdressStrategy().stream().anyMatch(it -> it.equals(PatientDetailResolveOrder.ResolveOrder.PREDECESSOR)))
            || (resolveOrder.getOtherStrategy() != null && resolveOrder.getOtherStrategy().stream()
            .anyMatch(it -> it.equals(PatientDetailResolveOrder.ResolveOrder.PREDECESSOR)));
    }

    private boolean isNotNullOrEmpty(String value) {
        return !Strings.isNullOrEmpty(value);
    }
}

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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.PatientDetailResolveOrder;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
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

    private static final List<UtkastStatus> UTKAST_STATUSES = Arrays.asList(UtkastStatus.DRAFT_INCOMPLETE, UtkastStatus.DRAFT_COMPLETE,
            UtkastStatus.SIGNED);

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
    @Transactional(readOnly = true)
    public Patient resolvePatient(Personnummer personnummer, String intygsTyp) {

        WebCertUser user;
        if (webCertUserService.hasAuthenticationContext()) {
            user = webCertUserService.getUser();
        } else {
            throw new IllegalStateException("The PatientDetailsResolver#resolvePatient method cannot be used without a "
                    + "valid authentication context");
        }

        // Make sure any external intygstyp representations (such as TSTRK1007) are mapped to our internal types.
        String internalIntygsTyp = intygsTyp;
        if (!moduleRegistry.moduleExists(intygsTyp)) {
            internalIntygsTyp = moduleRegistry.getModuleIdFromExternalId(intygsTyp);
        }

        try {
            ModuleEntryPoint moduleEntryPoint = moduleRegistry.getModuleEntryPoint(internalIntygsTyp);
            PatientDetailResolveOrder resolveOrder = moduleEntryPoint.getPatientDetailResolveOrder();
            return resolvePatientWithOrder(personnummer, user, resolveOrder);
        } catch (ModuleNotFoundException e) {
            throw new IllegalArgumentException("Unknown intygsTyp: " + intygsTyp);
        }
    }

    private Patient resolvePatientWithOrder(Personnummer personnummer, WebCertUser user, PatientDetailResolveOrder resolveOrder) {
        PersonSvar personSvar = getPersonSvar(personnummer);

        // Fristående and PU unavailable
        if (user.getOrigin().equals(UserOriginType.NORMAL.name()) && personSvar.getStatus().equals(PersonSvar.Status.ERROR)
                && !isPredecessorStrategy(resolveOrder)) {
            return null;
        }

        Utlatande predecessor = null;

        // If any strategy contains PREDECESSOR, do the lookup ONCE
        if (isPredecessorStrategy(resolveOrder)) {
            List<Utkast> utkastList = getPredecessor(personnummer, user, personSvar,resolveOrder.getPredecessorType());
            if (utkastList != null && utkastList.size() > 0) {
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

        // Only PU strategy and PU unavailable
        if (isPuOnlyStrategy(resolveOrder) && personSvar.getStatus().equals(PersonSvar.Status.ERROR)) {
            return null;
        }

        // Integrated with predecessor strategy, no predecessor and PU unavailable
        if (isPredecessorStrategy(resolveOrder) &&
                predecessor == null && personSvar.getStatus().equals(PersonSvar.Status.ERROR)) {
            return null;
        }

        Patient patient = new Patient();
        patient.setPersonId(personnummer);

        if (resolveOrder.getAdressStrategy() != null) {
            resolvePatientAdressDetails(patient, resolveOrder, personSvar, user, predecessor);
        }

        if (resolveOrder.getAvlidenStrategy() != null) {
            resolvePatientAvlidenDetails(patient, resolveOrder, personSvar, user, predecessor);
        }

        if (resolveOrder.getOtherStrategy() != null) {
            resolvePatientOtherDetails(patient, resolveOrder, personSvar, user, predecessor);
        }
        return patient;
    }

    private boolean isPuOnlyStrategy(PatientDetailResolveOrder resolveOrder) {
        return resolveOrder.getOtherStrategy().stream().allMatch(it -> it.equals(PatientDetailResolveOrder.ResolveOrder.PU)) &&
                resolveOrder.getAvlidenStrategy().stream().allMatch(it -> it.equals(PatientDetailResolveOrder.ResolveOrder.PU)) &&
                resolveOrder.getAdressStrategy().stream().allMatch(it -> it.equals(PatientDetailResolveOrder.ResolveOrder.PU));
    }

    private boolean isPredecessorStrategy(PatientDetailResolveOrder resolveOrder) {
        return (resolveOrder.getAdressStrategy() != null &&
                resolveOrder.getAdressStrategy().stream().anyMatch(it -> it.equals(PatientDetailResolveOrder.ResolveOrder.PREDECESSOR))) ||
                (resolveOrder.getAvlidenStrategy() != null  &&
                        resolveOrder.getAvlidenStrategy().stream().anyMatch(it -> it.equals(PatientDetailResolveOrder.ResolveOrder.PREDECESSOR))) ||
                (resolveOrder.getOtherStrategy() != null &&
                        resolveOrder.getOtherStrategy().stream().anyMatch(it -> it.equals(PatientDetailResolveOrder.ResolveOrder.PREDECESSOR)));
    }

    private void resolvePatientOtherDetails(Patient patient, PatientDetailResolveOrder resolveOrder, PersonSvar personSvar, WebCertUser user, Utlatande predecessor) {
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
            patient.setFornamn(personSvar.getPerson().getFornamn());
            patient.setEfternamn(personSvar.getPerson().getEfternamn());
            patient.setMellannamn(personSvar.getPerson().getMellannamn());
            patient.setFullstandigtNamn(Joiner.on(' ').skipNulls().join(personSvar.getPerson().getFornamn(),
                    personSvar.getPerson().getMellannamn(), personSvar.getPerson().getEfternamn()));
            patient.setSekretessmarkering(personSvar.getPerson().isSekretessmarkering());
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

    private void resolvePatientAvlidenDetails(Patient patient, PatientDetailResolveOrder resolveOrder,
                                              PersonSvar personSvar, WebCertUser user, Utlatande predecessor) {
        List<PatientDetailResolveOrder.ResolveOrder> avlidenStrategy = resolveOrder.getAvlidenStrategy();
        int index = 0;
        boolean done = false;
        while (index < avlidenStrategy.size() && !done) {
            switch (avlidenStrategy.get(index)) {
                case PARAMS:
                    done = setAvlidenFromParams(patient, user);
                    break;
                case PU:
                    done = setAvlidenFromPu(patient, personSvar);
                    break;
                case PARAMS_OR_PU:
                    done = setAvlidenFromParamsOrPU(patient, personSvar, user);
                    break;
                case PREDECESSOR:
                    done = setAvlidenFromPredecessor(patient, predecessor);
                    break;
            }
            index++;
        }
    }

    private boolean setAvlidenFromParamsOrPU(Patient patient, PersonSvar personSvar, WebCertUser user) {
        patient.setAvliden(
                (personSvar.getStatus() == PersonSvar.Status.FOUND && personSvar.getPerson().isAvliden())
                || (user.getParameters() != null && user.getParameters().isPatientDeceased())
                || (personSvar.getStatus() != PersonSvar.Status.FOUND && user.getParameters() == null));
        return true;
    }

    private boolean setAvlidenFromPredecessor(Patient patient, Utlatande predecessor) {
        if (predecessor != null) {
            patient.setAvliden(predecessor.getGrundData().getPatient().isAvliden());
            return true;
        }
        return false;
    }

    private boolean setAvlidenFromPu(Patient patient, PersonSvar personSvar) {
        if (personSvar.getStatus().equals(PersonSvar.Status.FOUND)) {
            patient.setAvliden(personSvar.getPerson().isAvliden());
            return true;
        }
        return false;
    }

    private boolean setAvlidenFromParams(Patient patient, WebCertUser user) {
        if (user.getOrigin().equals(UserOriginType.DJUPINTEGRATION.name())) {
            patient.setAvliden(user.getParameters().isPatientDeceased());
            return true;
        }
        return false;
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
            patient.setPostadress(personSvar.getPerson().getPostadress());
            patient.setPostnummer(personSvar.getPerson().getPostnummer());
            patient.setPostort(personSvar.getPerson().getPostort());
            return true;
        }
        return false;
    }

    private boolean setAdressFromParams(Patient patient, WebCertUser user) {
        if ( user.getOrigin().equals(UserOriginType.DJUPINTEGRATION.name()) &&
        isNotNullOrEmpty(user.getParameters().getPostadress()) && isNotNullOrEmpty(user.getParameters().getPostnummer())
                && isNotNullOrEmpty(user.getParameters().getPostort())) {
            patient.setPostadress(user.getParameters().getPostadress());
            patient.setPostnummer(user.getParameters().getPostnummer());
            patient.setPostort(user.getParameters().getPostort());
            return true;
        }
        return false;
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

    private PersonSvar getPersonSvar(Personnummer personnummer) {
        if (personnummer == null) {
            String errMsg = "No personnummer present. Unable to make a call to PUService";
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM, errMsg);
        }

        return puService.getPerson(personnummer);
    }

    List<Utkast> getPredecessor(Personnummer personnummer, WebCertUser user, PersonSvar personSvar, String intygsTyp) {
        // Find ALL existing intyg for this patient, filter out so we only have DB left.
        List<Utkast> utkastList = new ArrayList<>();
        if (user.getOrigin().equals(UserOriginType.DJUPINTEGRATION.name())) {
            utkastList.addAll(utkastRepository.findDraftsByPatientAndVardgivareAndStatus(personnummer.getPersonnummerWithDash(),
                    user.getValdVardgivare().getId(),
                    UTKAST_STATUSES, Sets.newHashSet(intygsTyp)));
        } else {
            utkastList.addAll(utkastRepository.findDraftsByPatientAndEnhetAndStatus(personnummer.getPersonnummerWithDash(),
                    Arrays.asList(user.getValdVardenhet().getId()), UTKAST_STATUSES,
                    Sets.newHashSet(intygsTyp)));
        }
        return utkastList;
    }

    private boolean isNotNullOrEmpty(String value) {
        return !Strings.isNullOrEmpty(value);
    }
}

/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.web.controller.testability.facade.util;

import static se.inera.intyg.common.af00213.v1.model.converter.RespConstants.ARBETETS_PAVERKAN_DELSVAR_ID_41;
import static se.inera.intyg.common.af00213.v1.model.converter.RespConstants.ARBETETS_PAVERKAN_SVAR_JSON_ID_41;
import static se.inera.intyg.common.af00213.v1.model.converter.RespConstants.FUNKTIONSNEDSATTNING_DELSVAR_ID_11;
import static se.inera.intyg.common.af00213.v1.model.converter.RespConstants.FUNKTIONSNEDSATTNING_SVAR_JSON_ID_11;
import static se.inera.intyg.common.af00213.v1.model.converter.RespConstants.UTREDNING_BEHANDLING_DELSVAR_ID_31;
import static se.inera.intyg.common.af00213.v1.model.converter.RespConstants.UTREDNING_BEHANDLING_SVAR_JSON_ID_31;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.af00213.support.Af00213EntryPoint;
import se.inera.intyg.common.fkparent.model.converter.RespConstants;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateDataElement;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValue;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueBoolean;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRange;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRangeList;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDiagnosis;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDiagnosisList;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CreateNewDraftHolder;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.auth.WebcertUserDetailsService;
import se.inera.intyg.webcert.web.integration.util.HoSPersonHelper;
import se.inera.intyg.webcert.web.service.facade.util.CertificateConverter;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateFillType;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateRequestDTO;

@Component
public class CreateCertificateTestabilityUtil {

    private final IntygModuleRegistry moduleRegistry;

    private final WebcertUserDetailsService webcertUserDetailsService;

    private final PatientDetailsResolver patientDetailsResolver;

    private final UtkastService utkastService;

    private final CertificateConverter certificateConverter;

    private final UtkastRepository utkastRepository;

    private final IntygTextsService intygTextsService;

    private static final int DEFAULT_SICK_LEAVE_LENGTH = 14;

    @Autowired
    public CreateCertificateTestabilityUtil(IntygModuleRegistry moduleRegistry,
        WebcertUserDetailsService webcertUserDetailsService,
        PatientDetailsResolver patientDetailsResolver, UtkastService utkastService,
        CertificateConverter certificateConverter, UtkastRepository utkastRepository,
        IntygTextsService intygTextsService) {
        this.moduleRegistry = moduleRegistry;
        this.webcertUserDetailsService = webcertUserDetailsService;
        this.patientDetailsResolver = patientDetailsResolver;
        this.utkastService = utkastService;
        this.certificateConverter = certificateConverter;
        this.utkastRepository = utkastRepository;
        this.intygTextsService = intygTextsService;
    }

    public String createNewCertificate(@NotNull CreateCertificateRequestDTO createCertificateRequest) {
        final var hosPersonal = getHoSPerson(
            createCertificateRequest.getPersonId(),
            createCertificateRequest.getUnitId()
        );

        final var patient = getPatient(
            createCertificateRequest.getPatientId(),
            createCertificateRequest.getCertificateType(),
            createCertificateRequest.getCertificateTypeVersion()
        );

        final var createNewDraftRequest = new CreateNewDraftRequest(
            null,
            createCertificateRequest.getCertificateType(),
            createCertificateRequest.getCertificateTypeVersion(),
            null,
            hosPersonal,
            patient
        );

        final var utkast = createNewDraft(createNewDraftRequest);

        final var certificate = certificateConverter.convert(utkast);

        updateCertificate(createCertificateRequest, certificate);

        utkast.setModel(getJsonFromCertificate(certificate, utkast.getModel()));

        updateCertificateWithRequestedStatus(createCertificateRequest, hosPersonal, utkast);

        utkastRepository.save(utkast);

        return certificate.getMetadata().getId();
    }

    private Utkast createNewDraft(CreateNewDraftRequest createNewDraftRequest) {
        final var utkast = utkastService.createNewDraft(createNewDraftRequest);
        downgradeMinorVersionIfNecessary(createNewDraftRequest, utkast);
        return utkast;
    }

    private void downgradeMinorVersionIfNecessary(CreateNewDraftRequest createNewDraftRequest, Utkast utkast) {
        final var latestVersionForSameMajorVersion = intygTextsService.getLatestVersionForSameMajorVersion(
            createNewDraftRequest.getIntygType(),
            createNewDraftRequest.getIntygTypeVersion()
        );

        utkast.setModel(utkast.getModel().replace(latestVersionForSameMajorVersion, createNewDraftRequest.getIntygTypeVersion()));
    }

    private void updateCertificateWithRequestedStatus(CreateCertificateRequestDTO createCertificateRequest, HoSPersonal hosPersonal,
        Utkast utkast) {
        if (createCertificateRequest.getStatus() == CertificateStatus.UNSIGNED) {
            final var draftValidation = utkastService.validateDraft(utkast.getIntygsId(), utkast.getIntygsTyp(), utkast.getModel());
            UtkastStatus utkastStatus = draftValidation.isDraftValid() ? UtkastStatus.DRAFT_COMPLETE : UtkastStatus.DRAFT_INCOMPLETE;
            utkast.setStatus(utkastStatus);
        } else if (createCertificateRequest.getStatus() == CertificateStatus.SIGNED) {
            final var signature = new Signatur(LocalDateTime.now(), utkast.getSkapadAv().getHsaId(), utkast.getIntygsId(),
                utkast.getModel(), "ruffel", "fusk", SignaturTyp.LEGACY);
            utkast.setSignatur(signature);
            utkast.setStatus(UtkastStatus.SIGNED);
            updateJsonBeforeSigning(hosPersonal, utkast, signature);
            if (createCertificateRequest.isSent()) {
                utkast.setSkickadTillMottagare("FKASSA");
                utkast.setSkickadTillMottagareDatum(LocalDateTime.now());
            }
        } else if (createCertificateRequest.getStatus() == CertificateStatus.LOCKED) {
            utkast.setStatus(UtkastStatus.DRAFT_LOCKED);
        } else {
            throw new IllegalArgumentException(
                String.format("Status '%s' not supported when creating certificate!", createCertificateRequest.getStatus()));
        }
    }

    private void updateCertificate(CreateCertificateRequestDTO createCertificateRequest, Certificate certificate) {
        if (createCertificateRequest.getFillType() == CreateCertificateFillType.EMPTY) {
            return;
        }

        if (createCertificateRequest.getFillType() == CreateCertificateFillType.WITH_VALUES) {
            updateCertificate(certificate, createCertificateRequest.getValues());
        }

        final var valueMap = createValues(createCertificateRequest);
        updateCertificate(certificate, valueMap);
    }

    private Map<String, CertificateDataValue> createValues(CreateCertificateRequestDTO createCertificateRequest) {
        if (createCertificateRequest.getCertificateType().equalsIgnoreCase(LisjpEntryPoint.MODULE_ID)) {
            return createMinimumValuesLisjp();
        }

        if (createCertificateRequest.getCertificateType().equalsIgnoreCase(Af00213EntryPoint.MODULE_ID)) {
            return createMinimumValuesAf00213();
        }

        return Collections.emptyMap();
    }

    private Map<String, CertificateDataValue> createMinimumValuesLisjp() {
        final var values = new HashMap<String, CertificateDataValue>();

        final CertificateDataValueBoolean avstangningSmittskydd = CertificateDataValueBoolean.builder()
            .id(RespConstants.AVSTANGNING_SMITTSKYDD_SVAR_JSON_ID_27)
            .selected(true)
            .build();
        values.put(RespConstants.AVSTANGNING_SMITTSKYDD_SVAR_ID_27, avstangningSmittskydd);

        final CertificateDataValueDiagnosisList diagnos = CertificateDataValueDiagnosisList.builder()
            .list(
                Collections.singletonList(
                    CertificateDataValueDiagnosis.builder()
                        .id("1")
                        .terminology("ICD_10_SE")
                        .code("J09")
                        .description("Influensa orsakad av identifierat zoonotiskt eller pandemiskt influensavirus")
                        .build()
                )
            )
            .build();
        values.put(RespConstants.DIAGNOS_SVAR_ID_6, diagnos);

        final var bedomning = CertificateDataValueDateRangeList.builder()
            .list(
                Collections.singletonList(
                    CertificateDataValueDateRange.builder()
                        .id("HELT_NEDSATT")
                        .from(LocalDate.now())
                        .to(LocalDate.now().plusDays(DEFAULT_SICK_LEAVE_LENGTH))
                        .build()
                )
            )
            .build();
        values.put(RespConstants.BEHOV_AV_SJUKSKRIVNING_SVAR_ID_32, bedomning);

        return values;
    }

    private Map<String, CertificateDataValue> createMinimumValuesAf00213() {
        final var values = new HashMap<String, CertificateDataValue>();

        final CertificateDataValueBoolean harFunktionsnedsattning = CertificateDataValueBoolean.builder()
            .id(FUNKTIONSNEDSATTNING_SVAR_JSON_ID_11)
            .selected(false)
            .build();
        values.put(FUNKTIONSNEDSATTNING_DELSVAR_ID_11, harFunktionsnedsattning);

        final CertificateDataValueBoolean harUtredningBehandling = CertificateDataValueBoolean.builder()
            .id(UTREDNING_BEHANDLING_SVAR_JSON_ID_31)
            .selected(false)
            .build();
        values.put(UTREDNING_BEHANDLING_DELSVAR_ID_31, harUtredningBehandling);

        final CertificateDataValueBoolean harArbetspaverkan = CertificateDataValueBoolean.builder()
            .id(ARBETETS_PAVERKAN_SVAR_JSON_ID_41)
            .selected(false)
            .build();
        values.put(ARBETETS_PAVERKAN_DELSVAR_ID_41, harArbetspaverkan);

        return values;
    }

    private void updateCertificate(Certificate certificate, Map<String, CertificateDataValue> valueMap) {
        valueMap.forEach((key, value) -> {
            final var certificateDataElement = certificate.getData().get(key);
            final var updatedCertificateDataElement = CertificateDataElement.builder()
                .id(certificateDataElement.getId())
                .parent(certificateDataElement.getParent())
                .index(certificateDataElement.getIndex())
                .config(certificateDataElement.getConfig())
                .validation(certificateDataElement.getValidation())
                .value(value)
                .build();
            certificate.getData().put(key, updatedCertificateDataElement);
        });
    }

    private void updateJsonBeforeSigning(HoSPersonal hosPersonal, Utkast utkast, Signatur signature) {
        try {
            final var updatedJson = getModuleApi(utkast.getIntygsTyp(), utkast.getIntygTypeVersion())
                .updateBeforeSigning(utkast.getModel(), hosPersonal, signature.getSigneringsDatum());
            utkast.setModel(updatedJson);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String getJsonFromCertificate(Certificate certificate, String currentModel) {
        try {
            final var moduleApi = moduleRegistry.getModuleApi(
                certificate.getMetadata().getType(),
                certificate.getMetadata().getTypeVersion()
            );

            return moduleApi.getJsonFromCertificate(certificate, currentModel);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String createNewInternal(String type, CreateNewDraftHolder createNewDraftHolder) {
        try {
            final var moduleApi = moduleRegistry.getModuleApi(type, createNewDraftHolder.getIntygTypeVersion());
            return moduleApi.createNewInternal(createNewDraftHolder);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private HoSPersonal getHoSPerson(String personId, String unitId) {
        final var user = getUser(personId);
        user.changeValdVardenhet(unitId);
        final var unit = HoSPersonHelper.createVardenhetFromIntygUser(unitId, user);

        final var hosPerson = new HoSPersonal();
        hosPerson.setFullstandigtNamn(user.getNamn());
        hosPerson.setPersonId(personId);
        hosPerson.setVardenhet(unit);

        HoSPersonHelper.enrichHoSPerson(hosPerson, user);

        return hosPerson;
    }

    private ModuleApi getModuleApi(String type, String typeVersion) {
        try {
            return moduleRegistry.getModuleApi(type, typeVersion);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private IntygUser getUser(String personId) {
        try {
            return webcertUserDetailsService.loadUserByHsaId(personId);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Patient getPatient(String patientId, String type, String typeVersion) {
        final var patient = patientDetailsResolver.resolvePatient(
            Personnummer.createPersonnummer(patientId).orElseThrow(),
            type,
            typeVersion);
        final var personFromPUService = patientDetailsResolver.getPersonFromPUService(patient.getPersonId());
        patient.setFornamn(personFromPUService.getPerson().getFornamn());
        patient.setMellannamn(personFromPUService.getPerson().getMellannamn());
        patient.setEfternamn(personFromPUService.getPerson().getEfternamn());
        patient.setTestIndicator(personFromPUService.getPerson().isTestIndicator());
        patient.setAvliden(personFromPUService.getPerson().isAvliden());
        patient.setSekretessmarkering(personFromPUService.getPerson().isSekretessmarkering());
        return patient;
    }
}

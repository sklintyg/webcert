/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
import static se.inera.intyg.common.ag7804.converter.RespConstants.*;
import static se.inera.intyg.common.ag7804.converter.RespConstants.AVSTANGNING_SMITTSKYDD_SVAR_ID_27;
import static se.inera.intyg.common.ag7804.converter.RespConstants.AVSTANGNING_SMITTSKYDD_SVAR_JSON_ID_27;
import static se.inera.intyg.common.ag7804.converter.RespConstants.BEHOV_AV_SJUKSKRIVNING_SVAR_ID_32;
import static se.inera.intyg.common.fkparent.model.converter.RespConstants.*;
import static se.inera.intyg.common.fkparent.model.converter.RespConstants.ARBETSRESOR_SVAR_ID_34;
import static se.inera.intyg.common.fkparent.model.converter.RespConstants.ARBETSTIDSFORLAGGNING_SVAR_JSON_ID_33;
import static se.inera.intyg.common.fkparent.model.converter.RespConstants.NUVARANDE_ARBETE_SVAR_JSON_ID_29;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.af00213.support.Af00213EntryPoint;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.fkparent.model.converter.RespConstants;
import se.inera.intyg.common.lisjp.model.internal.ArbetslivsinriktadeAtgarder;
import se.inera.intyg.common.lisjp.model.internal.PrognosDagarTillArbeteTyp;
import se.inera.intyg.common.lisjp.model.internal.PrognosTyp;
import se.inera.intyg.common.lisjp.model.internal.Sysselsattning;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateDataElement;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.value.*;
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
import se.inera.intyg.webcert.web.service.facade.util.UtkastToCertificateConverter;
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

    private final UtkastToCertificateConverter utkastToCertificateConverter;

    private final UtkastRepository utkastRepository;

    private final IntygTextsService intygTextsService;

    private static final int DEFAULT_SICK_LEAVE_LENGTH = 14;
    private static final int DEFAULT_SHORT_SICK_LEAVE_LENGTH = 4;


    @Autowired
    public CreateCertificateTestabilityUtil(IntygModuleRegistry moduleRegistry,
        WebcertUserDetailsService webcertUserDetailsService,
        PatientDetailsResolver patientDetailsResolver, UtkastService utkastService,
        UtkastToCertificateConverter utkastToCertificateConverter, UtkastRepository utkastRepository,
        IntygTextsService intygTextsService) {
        this.moduleRegistry = moduleRegistry;
        this.webcertUserDetailsService = webcertUserDetailsService;
        this.patientDetailsResolver = patientDetailsResolver;
        this.utkastService = utkastService;
        this.utkastToCertificateConverter = utkastToCertificateConverter;
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

        final var certificate = utkastToCertificateConverter.convert(utkast);

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
            if(createCertificateRequest.getFillType() == CreateCertificateFillType.MINIMAL) {
                return createMinimumValuesLisjp();
            } else {
                return createMaximumValuesLisjp();
            }
        }

        if (createCertificateRequest.getCertificateType().equalsIgnoreCase(Af00213EntryPoint.MODULE_ID)) {
            return createMinimumValuesAf00213();
        }

        if (createCertificateRequest.getCertificateType().equalsIgnoreCase(Ag7804EntryPoint.MODULE_ID)) {
            return createMinimumValuesAg7804();
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

        final CertificateDataValueDiagnosisList diagnos = getCertificateDataValueDiagnosisList();
        values.put(RespConstants.DIAGNOS_SVAR_ID_6, diagnos);

        final CertificateDataValueDateRangeList bedomning = getCertificateDataValueDateRangeList(false);
        values.put(RespConstants.BEHOV_AV_SJUKSKRIVNING_SVAR_ID_32, bedomning);

        return values;
    }

    private CertificateDataValueDateRangeList getCertificateDataValueDateRangeList(boolean shortPeriod) {
        final var bedomning = CertificateDataValueDateRangeList.builder()
            .list(
                Collections.singletonList(
                    CertificateDataValueDateRange.builder()
                        .id("HELT_NEDSATT")
                        .from(LocalDate.now())
                        .to(LocalDate.now().plusDays(shortPeriod ? DEFAULT_SHORT_SICK_LEAVE_LENGTH : DEFAULT_SICK_LEAVE_LENGTH))
                        .build()
                )
            )
            .build();
        return bedomning;
    }

    private CertificateDataValueDiagnosisList getCertificateDataValueDiagnosisList() {
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
        return diagnos;
    }

    private Map<String, CertificateDataValue> createMaximumValuesLisjp() {
        final var values = new HashMap<String, CertificateDataValue>();
        final var EXAMPLE_TEXT = "Detta är ett exempel";

        final var baseratPa = CertificateDataValueDateList.builder()
                .list(
                        Collections.singletonList(
                                CertificateDataValueDate.builder()
                                        .id(RespConstants.GRUNDFORMEDICINSKTUNDERLAG_ANNAT_SVAR_JSON_ID_1)
                                        .date(LocalDate.now())
                                        .build()

                        )
                )
                .build();
        values.put(RespConstants.GRUNDFORMEDICINSKTUNDERLAG_SVAR_ID_1, baseratPa);

        final var motiveringAnnat = CertificateDataTextValue.builder()
                .id(RespConstants.GRUNDFORMEDICINSKTUNDERLAG_BESKRIVNING_DELSVAR_JSON_ID_1)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.GRUNDFORMEDICINSKTUNDERLAG_TYP_DELSVAR_ID_1, motiveringAnnat);

        final var motiveringEjUndersokning = CertificateDataTextValue.builder()
                .id(MOTIVERING_TILL_INTE_BASERAT_PA_UNDERLAG_ID_1)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.GRUNDFORMEDICINSKTUNDERLAG_DATUM_DELSVAR_ID_1, motiveringEjUndersokning);

        final var sysselsattning = CertificateDataValueCodeList.builder()
                .list(
                        Collections.singletonList(
                                CertificateDataValueCode.builder()
                                        .id(Sysselsattning.SysselsattningsTyp.NUVARANDE_ARBETE.getId())
                                        .code(Sysselsattning.SysselsattningsTyp.NUVARANDE_ARBETE.getId())
                                        .build()
                        )
                )
                .build();
        values.put(RespConstants.TYP_AV_SYSSELSATTNING_SVAR_ID_28, sysselsattning);

        final var arbetsuppgifter = CertificateDataTextValue.builder()
                .id(NUVARANDE_ARBETE_SVAR_JSON_ID_29)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.NUVARANDE_ARBETE_SVAR_ID_29, arbetsuppgifter);

        final var diagnos = getCertificateDataValueDiagnosisList();
        values.put(RespConstants.DIAGNOS_SVAR_ID_6, diagnos);

        final var funktionsnedsattning = CertificateDataIcfValue.builder()
                .id(RespConstants.FUNKTIONSNEDSATTNING_SVAR_JSON_ID_35)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.FUNKTIONSNEDSATTNING_SVAR_ID_35, funktionsnedsattning);

        final var aktivitetsbegransning = CertificateDataIcfValue.builder()
                .id(RespConstants.AKTIVITETSBEGRANSNING_SVAR_JSON_ID_17)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.AKTIVITETSBEGRANSNING_SVAR_ID_17, aktivitetsbegransning);

        final var pagaendeBehandling = CertificateDataTextValue.builder()
                .id(RespConstants.PAGAENDEBEHANDLING_SVAR_JSON_ID_19)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.PAGAENDEBEHANDLING_SVAR_ID_19, pagaendeBehandling);

        final var planeradBehandling = CertificateDataTextValue.builder()
                .id(RespConstants.PLANERADBEHANDLING_SVAR_JSON_ID_20)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.PLANERADBEHANDLING_SVAR_ID_20, planeradBehandling);

        final CertificateDataValueDateRangeList bedomning = getCertificateDataValueDateRangeList(true);
        values.put(RespConstants.BEHOV_AV_SJUKSKRIVNING_SVAR_ID_32, bedomning);

        final var motiveringTidigtStartdatum = CertificateDataTextValue.builder()
                .id(RespConstants.MOTIVERING_TILL_TIDIGT_STARTDATUM_FOR_SJUKSKRIVNING_ID)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.BEHOV_AV_SJUKSKRIVNING_NIVA_DELSVARSVAR_ID_32, motiveringTidigtStartdatum);

        final var forsakringsMedicinsktBeslutsstod = CertificateDataTextValue.builder()
                .id(RespConstants.FORSAKRINGSMEDICINSKT_BESLUTSSTOD_SVAR_JSON_ID_37)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.FORSAKRINGSMEDICINSKT_BESLUTSSTOD_SVAR_ID_37, forsakringsMedicinsktBeslutsstod);

        final var arbetstidsforlaggning = CertificateDataValueBoolean.builder()
                .selected(true)
                .id(RespConstants.ARBETSTIDSFORLAGGNING_SVAR_JSON_ID_33)
                .build();
        values.put(RespConstants.ARBETSTIDSFORLAGGNING_SVAR_ID_33, arbetstidsforlaggning);

        final var motiveringArbetstidsforlaggning = CertificateDataTextValue.builder()
                .id(RespConstants.ARBETSTIDSFORLAGGNING_MOTIVERING_SVAR_JSON_ID_33)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.ARBETSTIDSFORLAGGNING_MOTIVERING_SVAR_ID_33, motiveringArbetstidsforlaggning);

        final var arbetsresor = CertificateDataValueBoolean.builder()
                .selected(true)
                .id(RespConstants.ARBETSRESOR_SVAR_JSON_ID_34)
                .build();
        values.put(RespConstants.ARBETSRESOR_SVAR_ID_34, arbetsresor);

        final var prognos = CertificateDataValueCode.builder()
                .id(PrognosTyp.ATER_X_ANTAL_DGR.getId())
                .code(PrognosTyp.ATER_X_ANTAL_DGR.getId())
                .build();
        values.put(RespConstants.PROGNOS_SVAR_ID_39, prognos);

        final var prognosTimePeriod = CertificateDataValueCode.builder()
                .id(PrognosDagarTillArbeteTyp.DAGAR_30.getId())
                .code(PrognosDagarTillArbeteTyp.DAGAR_30.getId())
                .build();
        values.put(RespConstants.PROGNOS_BESKRIVNING_DELSVAR_ID_39, prognosTimePeriod);

        final var atgarder = CertificateDataValueCodeList.builder()
                .list(
                        Collections.singletonList(
                                CertificateDataValueCode.builder()
                                        .id(ArbetslivsinriktadeAtgarder.ArbetslivsinriktadeAtgarderVal.OVRIGT.getId())
                                        .code(ArbetslivsinriktadeAtgarder.ArbetslivsinriktadeAtgarderVal.OVRIGT.getId())
                                        .build()
                        )
                )
                .build();
        values.put(RespConstants.ARBETSLIVSINRIKTADE_ATGARDER_SVAR_ID_40, atgarder);

        final var atgarderBeskrivning = CertificateDataTextValue.builder()
                .id(RespConstants.ARBETSLIVSINRIKTADE_ATGARDER_BESKRIVNING_SVAR_JSON_ID_44)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.ARBETSLIVSINRIKTADE_ATGARDER_BESKRIVNING_SVAR_ID_44, atgarderBeskrivning);

        final var ovrigt = CertificateDataTextValue.builder()
                .id(RespConstants.OVRIGT_SVAR_JSON_ID_25)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.OVRIGT_SVAR_ID_25, ovrigt);

        final var kontakt = CertificateDataValueBoolean.builder()
                .selected(true)
                .id(KONTAKT_ONSKAS_SVAR_JSON_ID_26)
                .build();
        values.put(KONTAKT_ONSKAS_SVAR_ID_26, kontakt);

        final var kontaktMotivering = CertificateDataTextValue.builder()
                .id(RespConstants.ANLEDNING_TILL_KONTAKT_DELSVAR_JSON_ID_26)
                .text(EXAMPLE_TEXT)
                .build();
        values.put(RespConstants.ANLEDNING_TILL_KONTAKT_DELSVAR_ID_26, kontaktMotivering);

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

    private Map<String, CertificateDataValue> createMinimumValuesAg7804() {
        final var values = new HashMap<String, CertificateDataValue>();

        final CertificateDataValueBoolean avstangningSmittskydd = CertificateDataValueBoolean.builder()
            .id(AVSTANGNING_SMITTSKYDD_SVAR_JSON_ID_27)
            .selected(true)
            .build();
        values.put(AVSTANGNING_SMITTSKYDD_SVAR_ID_27, avstangningSmittskydd);

        final CertificateDataValueCode shouldIncludeDiagnoses = CertificateDataValueCode.builder()
            .code(NO_ID)
            .id(NO_ID)
            .build();
        values.put(ONSKAR_FORMEDLA_DIAGNOS_SVAR_ID_100, shouldIncludeDiagnoses);

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
        values.put(BEHOV_AV_SJUKSKRIVNING_SVAR_ID_32, bedomning);

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

/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import static se.inera.intyg.common.support.facade.util.TestabilityToolkit.updateCertificate;

import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.facade.FillType;
import se.inera.intyg.common.support.modules.support.facade.TypeAheadProvider;
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

    private final TypeAheadProvider typeAheadProvider;

    @Autowired
    public CreateCertificateTestabilityUtil(IntygModuleRegistry moduleRegistry,
        WebcertUserDetailsService webcertUserDetailsService,
        PatientDetailsResolver patientDetailsResolver, UtkastService utkastService,
        UtkastToCertificateConverter utkastToCertificateConverter, UtkastRepository utkastRepository,
        IntygTextsService intygTextsService, TypeAheadProvider typeAheadProvider) {
        this.moduleRegistry = moduleRegistry;
        this.webcertUserDetailsService = webcertUserDetailsService;
        this.patientDetailsResolver = patientDetailsResolver;
        this.utkastService = utkastService;
        this.utkastToCertificateConverter = utkastToCertificateConverter;
        this.utkastRepository = utkastRepository;
        this.intygTextsService = intygTextsService;
        this.typeAheadProvider = typeAheadProvider;
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
        final var updateJsonModel = getUpdateJsonModel(utkast, createCertificateRequest);
        utkast.setModel(updateJsonModel);

        updateCertificateWithRequestedStatus(createCertificateRequest, hosPersonal, utkast);

        utkastRepository.save(utkast);

        return utkast.getIntygsId();
    }

    private String getUpdateJsonModel(Utkast utkast, CreateCertificateRequestDTO createCertificateRequest) {
        if (CreateCertificateFillType.EMPTY.equals(createCertificateRequest.getFillType())) {
            return utkast.getModel();
        }

        if (CreateCertificateFillType.WITH_VALUES.equals(createCertificateRequest.getFillType())) {
            final var certificate = utkastToCertificateConverter.convert(utkast);
            updateCertificate(certificate, createCertificateRequest.getValues());
            utkast.setModel(getJsonFromCertificate(certificate, utkast.getModel()));
        }

        try {
            final var moduleApi = moduleRegistry.getModuleApi(
                utkast.getIntygsTyp(),
                utkast.getIntygTypeVersion()
            );
            final var fillType =
                CreateCertificateFillType.MINIMAL.equals(createCertificateRequest.getFillType()) ? FillType.MINIMAL : FillType.MAXIMAL;
            return moduleApi.getUpdatedJsonWithTestData(utkast.getModel(), fillType, typeAheadProvider);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
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
        if (latestVersionForSameMajorVersion != null) {
            utkast.setModel(utkast.getModel().replace(latestVersionForSameMajorVersion, createNewDraftRequest.getIntygTypeVersion()));
        }
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
        patient.setPostadress(personFromPUService.getPerson().getPostadress());
        patient.setPostnummer(personFromPUService.getPerson().getPostnummer());
        patient.setPostort(personFromPUService.getPerson().getPostort());
        return patient;
    }
}

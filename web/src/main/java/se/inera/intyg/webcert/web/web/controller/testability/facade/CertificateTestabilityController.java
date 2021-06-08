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

package se.inera.intyg.webcert.web.web.controller.testability.facade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.common.fkparent.model.converter.RespConstants;
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
import se.inera.intyg.webcert.web.service.facade.util.CertificateConverterImpl;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;

@Path("/certificate")
public class CertificateTestabilityController extends AbstractApiController {

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private WebcertUserDetailsService webcertUserDetailsService;

    @Autowired
    private PatientDetailsResolver patientDetailsResolver;

    @Autowired
    private UtkastService utkastService;

    @Autowired
    private CertificateConverter certificateConverter;

    @Autowired
    private UtkastRepository utkastRepository;

    @POST
    @Consumes(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response createCertificate(@RequestBody @NotNull CreateCertificateRequestDTO createCertificateRequest) {
        return Response.ok(
            createNewCertificate(createCertificateRequest)
        ).build();
    }

    private String createNewCertificate(@NotNull CreateCertificateRequestDTO createCertificateRequest) {
        final var hosPersonal = getHoSPerson(
            createCertificateRequest.getPersonId(),
            createCertificateRequest.getUnitId()
        );

        final var createNewDraftRequest = new CreateNewDraftRequest(
            null,
            createCertificateRequest.getCertificateType(),
            createCertificateRequest.getCertificateTypeVersion(),
            null,
            hosPersonal,
            getPatient(
                createCertificateRequest.getPatientId(),
                createCertificateRequest.getCertificateType(),
                createCertificateRequest.getCertificateTypeVersion()
            )
        );

        final var utkast = utkastService.createNewDraft(createNewDraftRequest);

        final var certificate = certificateConverter.convert(utkast);

        updateCertificate(createCertificateRequest, certificate);

        utkast.setModel(getJsonFromCertificate(certificate, utkast.getModel()));

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
        } else if (createCertificateRequest.getStatus() == CertificateStatus.LOCKED) {
            utkast.setStatus(UtkastStatus.DRAFT_LOCKED);
        } else {
            throw new IllegalArgumentException(
                String.format("Status '%s' not supported when creating certificate!", createCertificateRequest.getStatus()));
        }

        utkastRepository.save(utkast);

        return certificate.getMetadata().getId();
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
        if (createCertificateRequest.getCertificateType().equalsIgnoreCase("lisjp")) {
            return createValuesLisjp();
        }

        return Collections.emptyMap();
    }

    private Map<String, CertificateDataValue> createValuesLisjp() {
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
                        .code("A01")
                        .description("Tyfoidfeber och paratyfoidfeber")
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
                        .to(LocalDate.now().plusDays(14))
                        .build()
                )
            )
            .build();
        values.put(RespConstants.BEHOV_AV_SJUKSKRIVNING_SVAR_ID_32, bedomning);

        return values;
    }

    private void updateCertificate(Certificate certificate, Map<String, CertificateDataValue> valueMap) {
        valueMap.entrySet().forEach(entry -> {
            final var certificateDataElement = certificate.getData().get(entry.getKey());
            final var updatedCertificateDataElement = CertificateDataElement.builder()
                .id(certificateDataElement.getId())
                .parent(certificateDataElement.getParent())
                .index(certificateDataElement.getIndex())
                .config(certificateDataElement.getConfig())
                .validation(certificateDataElement.getValidation())
                .value(entry.getValue())
                .build();
            certificate.getData().put(entry.getKey(), updatedCertificateDataElement);
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
        hosPerson.setFullstandigtNamn("Name");
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
        return patientDetailsResolver.resolvePatient(Personnummer.createPersonnummer(patientId).get(), type, typeVersion);
    }
}

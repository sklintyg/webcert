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
package se.inera.intyg.webcert.web.service.utkast;

import java.util.Arrays;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Relation;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CreateDraftCopyHolder;
import se.inera.intyg.common.support.modules.support.api.dto.ValidateDraftResponse;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationStatus;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.infra.integration.pu.model.Person;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.log.LogRequestFactory;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.util.UpdateUserUtil;
import se.inera.intyg.webcert.web.service.utkast.dto.CopyUtkastBuilderResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.util.CreateIntygsIdStrategy;

public abstract class AbstractUtkastBuilder<T extends CreateCopyRequest> implements CopyUtkastBuilder<T> {
    private static final String SPACE = " ";

    private static final Logger LOG = LoggerFactory.getLogger(CreateCopyUtkastBuilder.class);

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private IntygService intygService;

    @Autowired
    private CreateIntygsIdStrategy intygsIdStrategy;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private LogService logService;

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.utkast.CopyUtkastBuilder#populateCopyUtkastFromSignedIntyg(se.inera.intyg.
     * webcert.web.service.utkast.dto.CreateNewDraftCopyRequest, se.inera.intyg.webcert.integration.pu.model.Person)
     */
    @Override
    public CopyUtkastBuilderResponse populateCopyUtkastFromSignedIntyg(T copyRequest, Person patientDetails, boolean addRelation,
            boolean coherentJournaling, boolean checkVardgivare) throws ModuleNotFoundException, ModuleException {

        String orignalIntygsId = copyRequest.getOriginalIntygId();
        String intygsTyp = copyRequest.getTyp();

        IntygContentHolder signedIntygHolder = intygService.fetchIntygData(orignalIntygsId, intygsTyp, coherentJournaling);

        GrundData grundData = signedIntygHolder.getUtlatande().getGrundData();
        se.inera.intyg.common.support.model.common.internal.Vardenhet vardenhet = grundData.getSkapadAv().getVardenhet();

        if (coherentJournaling && checkVardgivare) {
            verifyVardgivarId(vardenhet.getVardgivare().getVardgivarid());
        }

        LOG.debug("Populating copy with details from signed Intyg '{}'", orignalIntygsId);

        CopyUtkastBuilderResponse builderResponse = new CopyUtkastBuilderResponse();
        builderResponse.setOrginalEnhetsId(vardenhet.getEnhetsid());
        builderResponse.setOrginalEnhetsNamn(vardenhet.getEnhetsnamn());
        builderResponse.setOrginalVardgivarId(vardenhet.getVardgivare().getVardgivarid());
        builderResponse.setOrginalVardgivarNamn(vardenhet.getVardgivare().getVardgivarnamn());

        ModuleApi moduleApi = moduleRegistry.getModuleApi(intygsTyp);

        // Set relation to null if not applicable
        Relation relation = createRelation(copyRequest);

        CreateDraftCopyHolder draftCopyHolder = createModuleRequestForCopying(copyRequest, patientDetails, relation);

        String draftCopyJson = getInternalModel(signedIntygHolder.getContents(), moduleApi, draftCopyHolder);

        UtkastStatus utkastStatus = validateDraft(moduleApi, draftCopyJson);

        Utkast utkast = buildUtkastCopy(copyRequest, draftCopyHolder.getCertificateId(), intygsTyp, addRelation, relation, draftCopyJson,
                utkastStatus);

        if (patientDetails != null) {
            populatePatientDetailsFromPerson(utkast, patientDetails);
        } else {
            se.inera.intyg.common.support.model.common.internal.Patient patient = signedIntygHolder.getUtlatande().getGrundData()
                    .getPatient();
            populatePatientDetailsFromPatient(utkast, patient);
        }

        replacePatientPersonnummerWithNew(utkast, copyRequest);

        builderResponse.setUtkastCopy(utkast);

        return builderResponse;
    }

    public abstract Relation createRelation(T copyRequest);

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.utkast.CopyUtkastBuilder#populateCopyUtkastFromOrignalUtkast(se.inera.intyg.
     * webcert.web.service.utkast.dto.CreateNewDraftCopyRequest, se.inera.intyg.webcert.integration.pu.model.Person)
     */
    @Override
    @Transactional(value = "jpaTransactionManager", readOnly = true)
    public CopyUtkastBuilderResponse populateCopyUtkastFromOrignalUtkast(T copyRequest, Person patientDetails, boolean addRelation,
            boolean coherentJournaling, boolean checkVardgivare) throws ModuleNotFoundException, ModuleException {

        String orignalIntygsId = copyRequest.getOriginalIntygId();

        Utkast orgUtkast = utkastRepository.findOne(orignalIntygsId);

        // Perform enhets auth if coherent journaling is not active.
        if (!coherentJournaling) {
            verifyEnhetsAuth(orgUtkast, true);
        } else {
            if (checkVardgivare) {
                verifyVardgivarId(orgUtkast.getVardgivarId());
            }
            // If it is, log the read to PDL with additional info indicating that coherent journaling is active.
            LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(orgUtkast, coherentJournaling);
            logService.logReadIntyg(logRequest);
        }

        CopyUtkastBuilderResponse builderResponse = new CopyUtkastBuilderResponse();
        builderResponse.setOrginalEnhetsId(orgUtkast.getEnhetsId());
        builderResponse.setOrginalEnhetsNamn(orgUtkast.getEnhetsNamn());
        builderResponse.setOrginalVardgivarId(orgUtkast.getVardgivarId());
        builderResponse.setOrginalVardgivarNamn(orgUtkast.getVardgivarNamn());

        LOG.debug("Populating copy with details from Utkast '{}'", orignalIntygsId);

        ModuleApi moduleApi = moduleRegistry.getModuleApi(orgUtkast.getIntygsTyp());

        // Set relation to null if not applicable
        Relation relation = createRelation(copyRequest);

        CreateDraftCopyHolder draftCopyHolder = createModuleRequestForCopying(copyRequest, patientDetails, relation);

        String draftCopyJson = getInternalModel(orgUtkast.getModel(), moduleApi, draftCopyHolder);

        UtkastStatus utkastStatus = validateDraft(moduleApi, draftCopyJson);

        Utkast utkast = buildUtkastCopy(copyRequest, draftCopyHolder.getCertificateId(), orgUtkast.getIntygsTyp(), addRelation, relation,
                draftCopyJson, utkastStatus);

        if (patientDetails != null) {
            populatePatientDetailsFromPerson(utkast, patientDetails);
        } else {
            populatePatientDetailsFromUtkast(utkast, orgUtkast);
        }

        replacePatientPersonnummerWithNew(utkast, copyRequest);

        builderResponse.setUtkastCopy(utkast);

        return builderResponse;
    }

    protected void verifyEnhetsAuth(Utkast utlatande, boolean isReadOnlyOperation) {
        String vardgivarId = utlatande.getVardgivarId();
        String enhetsId = utlatande.getEnhetsId();
        if (!webCertUserService.isAuthorizedForUnit(vardgivarId, enhetsId, isReadOnlyOperation)) {
            String msg = "User not authorized for enhet " + enhetsId;
            LOG.debug(msg);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, msg);
        }
    }

    protected void verifyVardgivarId(String vardgivarId) {
        if (!Objects.equals(vardgivarId, webCertUserService.getUser().getValdVardgivare().getId())) {
            String message = "VardgivarId of user and utkast does not match";
            LOG.debug(message);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, message);
        }
    }

    protected String getInternalModel(String jsonModel, ModuleApi moduleApi, CreateDraftCopyHolder draftCopyHolder)
            throws ModuleException {
        return moduleApi.createNewInternalFromTemplate(draftCopyHolder, jsonModel);
    }

    protected Utkast buildUtkastCopy(T copyRequest, String utkastId, String utkastTyp, boolean addRelation, Relation relation,
            String draftCopyJson, UtkastStatus utkastStatus) {
        Utkast utkast = new Utkast();

        utkast.setIntygsId(utkastId);
        utkast.setIntygsTyp(utkastTyp);
        utkast.setStatus(utkastStatus);
        utkast.setModel(draftCopyJson);

        if (addRelation) {
            enrichWithRelation(utkast, relation);
        }

        populateUtkastWithVardenhetAndHoSPerson(utkast, copyRequest);

        return utkast;
    }

    private void enrichWithRelation(Utkast utkast, Relation relation) {
        utkast.setRelationIntygsId(relation.getRelationIntygsId());
        utkast.setRelationKod(relation.getRelationKod());
    }

    private CreateDraftCopyHolder createModuleRequestForCopying(CreateCopyRequest copyRequest, Person person, Relation relation) {

        String newDraftCopyId = intygsIdStrategy.createId();

        LOG.debug("Created id '{}' for the new copy", newDraftCopyId);

        CreateDraftCopyHolder newDraftCopyHolder = new CreateDraftCopyHolder(newDraftCopyId, copyRequest.getHosPerson(), relation);

        if (person != null) {
            Patient patient = new Patient();
            patient.setFornamn(person.getFornamn());
            patient.setMellannamn(person.getMellannamn());
            patient.setEfternamn(person.getEfternamn());
            patient.setPersonId(person.getPersonnummer());
            patient.setPostadress(person.getPostadress());
            patient.setPostnummer(person.getPostnummer());
            patient.setPostort(person.getPostort());
            patient.setFullstandigtNamn(
                    IntygConverterUtil.concatPatientName(patient.getFornamn(), patient.getMellannamn(), patient.getEfternamn()));
            newDraftCopyHolder.setPatient(patient);
            LOG.debug("Added new patient data to CreateDraftCopyHolder");
        }

        if (copyRequest.containsNyttPatientPersonnummer()) {
            newDraftCopyHolder.setNewPersonnummer(copyRequest.getNyttPatientPersonnummer());
            LOG.debug("Added new personnummer to CreateDraftCopyHolder");
        }

        return newDraftCopyHolder;
    }

    private void populateUtkastWithVardenhetAndHoSPerson(Utkast utkast, CreateCopyRequest copyRequest) {
        Vardenhet vardenhet = copyRequest.getHosPerson().getVardenhet();

        utkast.setEnhetsId(vardenhet.getEnhetsid());
        utkast.setEnhetsNamn(vardenhet.getEnhetsnamn());

        Vardgivare vardgivare = vardenhet.getVardgivare();

        utkast.setVardgivarId(vardgivare.getVardgivarid());
        utkast.setVardgivarNamn(vardgivare.getVardgivarnamn());

        VardpersonReferens creator = UpdateUserUtil.createVardpersonFromHosPerson(copyRequest.getHosPerson());

        utkast.setSenastSparadAv(creator);
        utkast.setSkapadAv(creator);
    }

    private void populatePatientDetailsFromUtkast(Utkast utkast, Utkast orgUtkast) {
        utkast.setPatientPersonnummer(orgUtkast.getPatientPersonnummer());
        utkast.setPatientFornamn(orgUtkast.getPatientFornamn());
        utkast.setPatientMellannamn(orgUtkast.getPatientMellannamn());
        utkast.setPatientEfternamn(orgUtkast.getPatientEfternamn());
    }

    private void populatePatientDetailsFromPerson(Utkast utkast, Person person) {
        utkast.setPatientPersonnummer(person.getPersonnummer());
        utkast.setPatientFornamn(person.getFornamn());
        utkast.setPatientMellannamn(person.getMellannamn());
        utkast.setPatientEfternamn(person.getEfternamn());
    }

    private void populatePatientDetailsFromPatient(Utkast utkast, Patient patient) {
        utkast.setPatientPersonnummer(patient.getPersonId());

        utkast.setPatientFornamn(patient.getFornamn());
        utkast.setPatientMellannamn(patient.getMellannamn());
        utkast.setPatientEfternamn(patient.getEfternamn());

        if (patient.getFullstandigtNamn() != null && (utkast.getPatientFornamn() == null || utkast.getPatientEfternamn() == null)) {
            String[] nameParts = extractNamePartsFromFullName(patient.getFullstandigtNamn());
            utkast.setPatientFornamn(nameParts[0]);
            utkast.setPatientEfternamn(nameParts[1]);
        }
    }

    public String[] extractNamePartsFromFullName(String fullName) {

        String[] res = new String[] { "", "" };

        if (Strings.nullToEmpty(fullName).trim().isEmpty()) {
            return res;
        }

        // use the last name from the template efternamn as efternamn and the rest as fornamn.
        String[] nameParts = fullName.split(SPACE);

        if (nameParts.length == 1) {
            res[0] = nameParts[0];
        } else {
            res[0] = Joiner.on(SPACE).join(Arrays.asList(nameParts).subList(0, nameParts.length - 1));
            res[1] = nameParts[nameParts.length - 1];
        }

        return res;
    }

    private void replacePatientPersonnummerWithNew(Utkast utkast, CreateCopyRequest copyRequest) {
        if (copyRequest.containsNyttPatientPersonnummer()) {
            utkast.setPatientPersonnummer(copyRequest.getNyttPatientPersonnummer());
            LOG.debug("Replaced patient SSN with new one");
        }
    }

    private UtkastStatus validateDraft(ModuleApi moduleApi, String draftCopyJson) throws ModuleException {
        ValidateDraftResponse validationResponse = moduleApi.validateDraft(draftCopyJson);

        ValidationStatus validationStatus = validationResponse.getStatus();

        return ValidationStatus.VALID.equals(validationStatus) ? UtkastStatus.DRAFT_COMPLETE : UtkastStatus.DRAFT_INCOMPLETE;
    }

}

/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import java.io.IOException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Relation;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CreateDraftCopyHolder;
import se.inera.intyg.common.support.modules.support.api.dto.ValidateDraftResponse;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationStatus;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.pu.integration.api.model.Person;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.util.UpdateUserUtil;
import se.inera.intyg.webcert.web.service.utkast.dto.AbstractCreateCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.UtkastBuilderResponse;
import se.inera.intyg.webcert.web.service.utkast.util.CreateIntygsIdStrategy;

public abstract class AbstractUtkastBuilder<T extends AbstractCreateCopyRequest> implements CopyUtkastBuilder<T> {

    private static final String SPACE = " ";

    private static final Logger LOG = LoggerFactory.getLogger(AbstractUtkastBuilder.class);

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private IntygService intygService;

    @Autowired
    private CreateIntygsIdStrategy intygsIdStrategy;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private IntygTextsService intygTextsService;

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.utkast.CopyUtkastBuilder#populateCopyUtkastFromSignedIntyg(se.inera.intyg.
     * webcert.web.service.utkast.dto.CreateNewDraftCopyRequest, se.inera.intyg.webcert.integration.pu.model.Person)
     */
    @Override
    public UtkastBuilderResponse populateCopyUtkastFromSignedIntyg(T copyRequest, Person patientDetails,
        boolean addRelation) throws ModuleNotFoundException, ModuleException {

        String orignalIntygsId = copyRequest.getOriginalIntygId();
        String originalIntygsTyp = copyRequest.getOriginalIntygTyp();
        String intygsTyp = copyRequest.getTyp();

        IntygContentHolder signedIntygHolder = intygService.fetchIntygData(orignalIntygsId, originalIntygsTyp);

        ModuleApi orgModuleApi = moduleRegistry.getModuleApi(originalIntygsTyp, signedIntygHolder.getUtlatande().getTextVersion());
        Utlatande orgUtlatande;
        try {
            orgUtlatande = orgModuleApi.getUtlatandeFromJson(signedIntygHolder.getContents());
        } catch (IOException e) {
            throw new ModuleException("Could not convert orignal certificate to Utlatande", e);
        }
        GrundData grundData = signedIntygHolder.getUtlatande().getGrundData();
        se.inera.intyg.common.support.model.common.internal.Vardenhet vardenhet = grundData.getSkapadAv().getVardenhet();

        LOG.debug("Populating copy with details from signed Intyg '{}'", orignalIntygsId);

        UtkastBuilderResponse builderResponse = new UtkastBuilderResponse();
        builderResponse.setOrginalEnhetsId(vardenhet.getEnhetsid());
        builderResponse.setOrginalEnhetsNamn(vardenhet.getEnhetsnamn());
        builderResponse.setOrginalVardgivarId(vardenhet.getVardgivare().getVardgivarid());
        builderResponse.setOrginalVardgivarNamn(vardenhet.getVardgivare().getVardgivarnamn());

        // Make sure the new Draft gets correct typeVersion.
        ensureCorrectNewVersion(copyRequest, signedIntygHolder.getUtlatande().getTextVersion());
        ModuleApi moduleApi = moduleRegistry.getModuleApi(intygsTyp, copyRequest.getTypVersion());

        // Set relation to null if not applicable
        Relation relation = createRelation(copyRequest);

        String newDraftCopyId = intygsIdStrategy.createId();
        String draftCopyJson = getInternalModel(orgUtlatande, moduleApi, copyRequest, patientDetails, relation,
            newDraftCopyId);

        UtkastStatus utkastStatus = validateDraft(moduleApi, draftCopyJson);

        // NOTE: See INTYG-7212 can we really just take textVersion of orgUtlatande like when db->doi?
        Utkast utkast = buildUtkastCopy(copyRequest, newDraftCopyId, intygsTyp, copyRequest.getTypVersion(),
            addRelation, relation, draftCopyJson, utkastStatus);

        if (patientDetails != null) {
            populatePatientDetailsFromPerson(utkast, patientDetails);
        } else {
            se.inera.intyg.common.support.model.common.internal.Patient patient = signedIntygHolder.getUtlatande().getGrundData()
                .getPatient();
            populatePatientDetailsFromPatient(utkast, patient);
        }

        utkast.setTestIntyg(copyRequest.isTestIntyg());

        replacePatientPersonnummerWithNew(utkast, copyRequest);

        builderResponse.setUtkast(utkast);

        return builderResponse;
    }

    /**
     * Makes sure that the new draft gets a suitable version accoring to business rules for versioning of intygtypes.
     * see GE-012 Versionshantering av intygstyper
     * see WC-F016
     */
    private void ensureCorrectNewVersion(T copyRequest, String originalVersion) {
        if (StringUtils.isEmpty(copyRequest.getTypVersion())) {
            String version = intygTextsService
                .getLatestVersionForSameMajorVersion(copyRequest.getTyp(), originalVersion);
            if (version == null) {
                version = originalVersion;
            }
            copyRequest.setTypVersion(version);
        }
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
    @Transactional(readOnly = true)
    public UtkastBuilderResponse populateCopyUtkastFromOrignalUtkast(T copyRequest, Person patientDetails,
        boolean addRelation) throws ModuleNotFoundException, ModuleException {

        String orignalIntygsId = copyRequest.getOriginalIntygId();

        Utkast orgUtkast = utkastRepository.findById(orignalIntygsId).orElse(null);

        if (orgUtkast == null) {
            throw new ModuleException("Could not convert original certificate to Utlatande. Original certificate not found");
        }
        ModuleApi orgModuleApi = moduleRegistry.getModuleApi(copyRequest.getOriginalIntygTyp(), orgUtkast.getIntygTypeVersion());
        Utlatande orgUtlatande;
        try {
            orgUtlatande = orgModuleApi.getUtlatandeFromJson(orgUtkast.getModel());
        } catch (IOException e) {
            throw new ModuleException("Could not convert original certificate to Utlatande", e);
        }

        UtkastBuilderResponse builderResponse = new UtkastBuilderResponse();
        builderResponse.setOrginalEnhetsId(orgUtkast.getEnhetsId());
        builderResponse.setOrginalEnhetsNamn(orgUtkast.getEnhetsNamn());
        builderResponse.setOrginalVardgivarId(orgUtkast.getVardgivarId());
        builderResponse.setOrginalVardgivarNamn(orgUtkast.getVardgivarNamn());

        LOG.debug("Populating copy with details from Utkast '{}'", orignalIntygsId);

        // Make sure the new Draft gets correct typeVersion.
        ensureCorrectNewVersion(copyRequest, orgUtkast.getIntygTypeVersion());

        ModuleApi moduleApi = moduleRegistry.getModuleApi(copyRequest.getTyp(), copyRequest.getTypVersion());

        // Set relation to null if not applicable
        Relation relation = createRelation(copyRequest);

        String newDraftCopyId = intygsIdStrategy.createId();
        String draftCopyJson = getInternalModel(orgUtlatande, moduleApi, copyRequest, patientDetails, relation,
            newDraftCopyId);

        UtkastStatus utkastStatus = validateDraft(moduleApi, draftCopyJson);
        Utkast utkast = buildUtkastCopy(copyRequest, newDraftCopyId, copyRequest.getTyp(), copyRequest.getTypVersion(), addRelation,
            relation, draftCopyJson, utkastStatus);

        if (patientDetails != null) {
            populatePatientDetailsFromPerson(utkast, patientDetails);
        } else {
            populatePatientDetailsFromUtkast(utkast, orgUtkast);
        }

        utkast.setTestIntyg(copyRequest.isTestIntyg());

        replacePatientPersonnummerWithNew(utkast, copyRequest);

        builderResponse.setUtkast(utkast);

        return builderResponse;
    }

    protected String getInternalModel(Utlatande template, ModuleApi moduleApi, AbstractCreateCopyRequest copyRequest,
        Person person, Relation relation, String newDraftCopyId) throws ModuleException {
        CreateDraftCopyHolder draftCopyHolder = createModuleRequestForCopying(copyRequest, person, relation, newDraftCopyId);
        return moduleApi.createNewInternalFromTemplate(draftCopyHolder, template);
    }

    // CHECKSTYLE:OFF ParameterNumber
    protected Utkast buildUtkastCopy(T copyRequest, String utkastId, String utkastTyp, String intygTypeVersion, boolean addRelation,
        Relation relation, String draftCopyJson, UtkastStatus utkastStatus) {

        Utkast utkast = new Utkast();
        utkast.setIntygsId(utkastId);
        utkast.setIntygsTyp(utkastTyp);
        utkast.setIntygTypeVersion(intygTypeVersion);
        utkast.setStatus(utkastStatus);
        utkast.setModel(draftCopyJson);

        if (addRelation && relation != null) {
            enrichWithRelation(utkast, relation);
        }

        // If source and target certificate types are different, the current copy action is for prefilling of a draft,
        // and version should be set to 1 (instead of 0) to suppress the option to prefill the draft once more.
        boolean isPrefillRequest = !utkastTyp.equals(copyRequest.getOriginalIntygTyp());
        if (isPrefillRequest) {
            utkast.setVersion(1);
        }

        populateUtkastWithVardenhetAndHoSPerson(utkast, copyRequest);

        return utkast;
    }
    // CHECKSTYLE:ON ParameterNumber

    private void enrichWithRelation(Utkast utkast, Relation relation) {
        utkast.setRelationIntygsId(relation.getRelationIntygsId());
        utkast.setRelationKod(relation.getRelationKod());
    }

    protected CreateDraftCopyHolder createModuleRequestForCopying(AbstractCreateCopyRequest copyRequest, Person person, Relation relation,
        String newDraftCopyId) {
        LOG.debug("Created id '{}' for the new copy", newDraftCopyId);

        CreateDraftCopyHolder newDraftCopyHolder = new CreateDraftCopyHolder(newDraftCopyId, copyRequest.getHosPerson(), relation);
        newDraftCopyHolder.setIntygTypeVersion(copyRequest.getTypVersion());
        newDraftCopyHolder.setTestIntyg(copyRequest.isTestIntyg());

        if (person != null) {
            Patient patient = new Patient();
            patient.setFornamn(person.fornamn());
            patient.setMellannamn(person.mellannamn());
            patient.setEfternamn(person.efternamn());
            patient.setPersonId(person.personnummer());
            patient.setPostadress(person.postadress());
            patient.setPostnummer(person.postnummer());
            patient.setPostort(person.postort());
            patient.setFullstandigtNamn(
                IntygConverterUtil.concatPatientName(patient.getFornamn(), patient.getMellannamn(), patient.getEfternamn()));
            patient.setTestIndicator(person.testIndicator());
            newDraftCopyHolder.setPatient(patient);
            LOG.debug("Added new patient data to CreateDraftCopyHolder");
        }

        if (copyRequest.containsNyttPatientPersonnummer()) {
            newDraftCopyHolder.setNewPersonnummer(copyRequest.getNyttPatientPersonnummer());
            LOG.debug("Added new personnummer to CreateDraftCopyHolder");
        }

        return newDraftCopyHolder;
    }

    private void populateUtkastWithVardenhetAndHoSPerson(Utkast utkast, AbstractCreateCopyRequest copyRequest) {
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
        utkast.setPatientPersonnummer(person.personnummer());
        utkast.setPatientFornamn(person.fornamn());
        utkast.setPatientMellannamn(person.mellannamn());
        utkast.setPatientEfternamn(person.efternamn());
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

        String[] res = new String[]{"", ""};

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

    private void replacePatientPersonnummerWithNew(Utkast utkast, AbstractCreateCopyRequest copyRequest) {
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
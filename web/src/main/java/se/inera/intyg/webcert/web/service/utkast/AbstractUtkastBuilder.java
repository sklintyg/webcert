/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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
import se.inera.intyg.infra.integration.pu.model.Person;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
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
import se.inera.intyg.webcert.web.service.utkast.dto.AbstractCreateCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CopyUtkastBuilderResponse;
import se.inera.intyg.webcert.web.service.utkast.util.CreateIntygsIdStrategy;

import java.io.IOException;
import java.util.Arrays;

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
            boolean coherentJournaling, boolean enforceEnhet) throws ModuleNotFoundException, ModuleException {

        String orignalIntygsId = copyRequest.getOriginalIntygId();
        String originalIntygsTyp = copyRequest.getOriginalIntygTyp();
        String intygsTyp = copyRequest.getTyp();

        IntygContentHolder signedIntygHolder = intygService.fetchIntygData(orignalIntygsId, originalIntygsTyp, coherentJournaling);

        ModuleApi orgModuleApi = moduleRegistry.getModuleApi(originalIntygsTyp, signedIntygHolder.getUtlatande().getTextVersion());
        Utlatande orgUtlatande;
        try {
            orgUtlatande = orgModuleApi.getUtlatandeFromJson(signedIntygHolder.getContents());
        } catch (IOException e) {
            throw new ModuleException("Could not convert orignal certificate to Utlatande", e);
        }
        GrundData grundData = signedIntygHolder.getUtlatande().getGrundData();
        se.inera.intyg.common.support.model.common.internal.Vardenhet vardenhet = grundData.getSkapadAv().getVardenhet();

        if (coherentJournaling && enforceEnhet) {
            verifyEnhetsAuth(vardenhet.getVardgivare().getVardgivarid(), vardenhet.getEnhetsid(), true);
        }

        LOG.debug("Populating copy with details from signed Intyg '{}'", orignalIntygsId);

        CopyUtkastBuilderResponse builderResponse = new CopyUtkastBuilderResponse();
        builderResponse.setOrginalEnhetsId(vardenhet.getEnhetsid());
        builderResponse.setOrginalEnhetsNamn(vardenhet.getEnhetsnamn());
        builderResponse.setOrginalVardgivarId(vardenhet.getVardgivare().getVardgivarid());
        builderResponse.setOrginalVardgivarNamn(vardenhet.getVardgivare().getVardgivarnamn());

        //NOTE: see INTYG-7212 can we really just take textVersion of orgUtlatande like when db->doi?
        ModuleApi moduleApi = moduleRegistry.getModuleApi(intygsTyp, signedIntygHolder.getUtlatande().getTextVersion());

        // Set relation to null if not applicable
        Relation relation = createRelation(copyRequest);

        String newDraftCopyId = intygsIdStrategy.createId();
        String draftCopyJson = getInternalModel(orgUtlatande, moduleApi, copyRequest, patientDetails, relation,
                newDraftCopyId);

        UtkastStatus utkastStatus = validateDraft(moduleApi, draftCopyJson);

        //NOTE: See INTYG-7212 can we really just take textVersion of orgUtlatande like when db->doi?
        Utkast utkast = buildUtkastCopy(copyRequest, newDraftCopyId, intygsTyp, signedIntygHolder.getUtlatande().getTextVersion(),
                addRelation, relation,
                draftCopyJson, utkastStatus);

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
    @Transactional(readOnly = true)
    public CopyUtkastBuilderResponse populateCopyUtkastFromOrignalUtkast(T copyRequest, Person patientDetails, boolean addRelation,
            boolean coherentJournaling, boolean enforceEnhet) throws ModuleNotFoundException, ModuleException {

        String orignalIntygsId = copyRequest.getOriginalIntygId();

        Utkast orgUtkast = utkastRepository.findOne(orignalIntygsId);

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

        // Perform enhets auth if coherent journaling is not active.
        if (!coherentJournaling || enforceEnhet) {
            verifyEnhetsAuth(orgUtkast.getVardgivarId(), orgUtkast.getEnhetsId(), true);
        } else {
            LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(orgUtkast, coherentJournaling);
            logService.logReadIntyg(logRequest);
        }

        CopyUtkastBuilderResponse builderResponse = new CopyUtkastBuilderResponse();
        builderResponse.setOrginalEnhetsId(orgUtkast.getEnhetsId());
        builderResponse.setOrginalEnhetsNamn(orgUtkast.getEnhetsNamn());
        builderResponse.setOrginalVardgivarId(orgUtkast.getVardgivarId());
        builderResponse.setOrginalVardgivarNamn(orgUtkast.getVardgivarNamn());

        LOG.debug("Populating copy with details from Utkast '{}'", orignalIntygsId);
        //NOTE: see INTYG-7212 can we really just take textVersion of orgUtlatande like when db->doi?
        //The new Utkast's version is assumed to be of the same version as original.
        ModuleApi moduleApi = moduleRegistry.getModuleApi(copyRequest.getTyp(), orgUtkast.getIntygTypeVersion());

        // Set relation to null if not applicable
        Relation relation = createRelation(copyRequest);

        String newDraftCopyId = intygsIdStrategy.createId();
        String draftCopyJson = getInternalModel(orgUtlatande, moduleApi, copyRequest, patientDetails, relation,
                newDraftCopyId);

        UtkastStatus utkastStatus = validateDraft(moduleApi, draftCopyJson);
        //NOTE: See INTYG-7212 can we really just take textVersion of orgUtlatande like when db->doi?
        //I.e when copying within the same intygType A -> A this should be OK, but maybe not for DB -> DOI
        Utkast utkast = buildUtkastCopy(copyRequest, newDraftCopyId, copyRequest.getTyp(), orgUtkast.getIntygTypeVersion(), addRelation,
                relation,
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

    protected void verifyEnhetsAuth(String vardgivarId, String enhetId, boolean isReadOnlyOperation) {
        if (!webCertUserService.isAuthorizedForUnit(vardgivarId, enhetId, isReadOnlyOperation)) {
            String msg = "User not authorized for enhet " + enhetId;
            LOG.debug(msg);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, msg);
        }
    }

    protected String getInternalModel(Utlatande template, ModuleApi moduleApi, AbstractCreateCopyRequest copyRequest,
                                      Person person, Relation relation, String newDraftCopyId) throws ModuleException {
        CreateDraftCopyHolder draftCopyHolder = createModuleRequestForCopying(copyRequest, person, relation, newDraftCopyId);
        return moduleApi.createNewInternalFromTemplate(draftCopyHolder, template);
    }

    // CHECKSTYLE:OFF ParameterNumber
    protected Utkast buildUtkastCopy(T copyRequest, String utkastId, String utkastTyp, String intygTypeVersion, boolean addRelation,
            Relation relation,
            String draftCopyJson, UtkastStatus utkastStatus) {
        Utkast utkast = new Utkast();

        utkast.setIntygsId(utkastId);
        utkast.setIntygsTyp(utkastTyp);
        utkast.setIntygTypeVersion(intygTypeVersion);
        utkast.setStatus(utkastStatus);
        utkast.setModel(draftCopyJson);

        if (addRelation && relation != null) {
            enrichWithRelation(utkast, relation);
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

/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.Relation;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CreateDraftCopyHolder;
import se.inera.intyg.common.support.modules.support.api.dto.InternalModelHolder;
import se.inera.intyg.common.support.modules.support.api.dto.InternalModelResponse;
import se.inera.intyg.common.support.modules.support.api.dto.ValidateDraftResponse;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationStatus;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.integration.pu.model.Person;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.dto.HoSPerson;
import se.inera.intyg.webcert.web.service.dto.Vardenhet;
import se.inera.intyg.webcert.web.service.dto.Vardgivare;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.util.UpdateUserUtil;
import se.inera.intyg.webcert.web.service.utkast.dto.CopyUtkastBuilderResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCompletionCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.util.CreateIntygsIdStrategy;

@Component
public class CopyUtkastBuilderImpl implements CopyUtkastBuilder {

    private static final String SPACE = " ";

    private static final Logger LOG = LoggerFactory.getLogger(CopyUtkastBuilderImpl.class);

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private IntygService intygService;

    @Autowired
    private CreateIntygsIdStrategy intygsIdStrategy;

    @Autowired
    private UtkastRepository utkastRepository;

    /* (non-Javadoc)
     * @see se.inera.intyg.webcert.web.service.utkast.CopyUtkastBuilder#populateCopyUtkastFromSignedIntyg(se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftCopyRequest, se.inera.intyg.webcert.integration.pu.model.Person)
     */
    @Override
    public CopyUtkastBuilderResponse populateCopyUtkastFromSignedIntyg(CreateCopyRequest copyRequest, Person patientDetails, boolean addRelation) throws ModuleNotFoundException,
            ModuleException {

        String orignalIntygsId = copyRequest.getOriginalIntygId();
        String intygsTyp = copyRequest.getTyp();

        IntygContentHolder signedIntygHolder = intygService.fetchIntygData(orignalIntygsId, intygsTyp);

        LOG.debug("Populating copy with details from signed Intyg '{}'", orignalIntygsId);

        GrundData grundData = signedIntygHolder.getUtlatande().getGrundData();

        CopyUtkastBuilderResponse builderResponse = new CopyUtkastBuilderResponse();
        se.inera.intyg.common.support.model.common.internal.Vardenhet vardenhet = grundData.getSkapadAv().getVardenhet();
        builderResponse.setOrginalEnhetsId(vardenhet.getEnhetsid());
        builderResponse.setOrginalEnhetsNamn(vardenhet.getEnhetsnamn());
        builderResponse.setOrginalVardgivarId(vardenhet.getVardgivare().getVardgivarid());
        builderResponse.setOrginalVardgivarNamn(vardenhet.getVardgivare().getVardgivarnamn());

        ModuleApi moduleApi = moduleRegistry.getModuleApi(intygsTyp);

        // Set relation to null if not applicable
        Relation relation = addRelation ? createRelation((CreateCompletionCopyRequest) copyRequest, RelationKod.KOMPLT) : null;

        CreateDraftCopyHolder draftCopyHolder = createModuleRequestForCopying(copyRequest, patientDetails, relation);

        InternalModelResponse draftResponse = moduleApi.createNewInternalFromTemplate(draftCopyHolder,
                new InternalModelHolder(signedIntygHolder.getContents()));

        String draftCopyJson = draftResponse.getInternalModel();

        UtkastStatus utkastStatus = validateDraft(moduleApi, draftCopyJson);

        Utkast utkast = buildUtkastCopy(copyRequest, draftCopyHolder.getCertificateId(), intygsTyp, addRelation, relation, draftCopyJson, utkastStatus);

        if (patientDetails != null) {
            populatePatientDetailsFromPerson(utkast, patientDetails);
        } else {
            se.inera.intyg.common.support.model.common.internal.Patient patient = signedIntygHolder.getUtlatande().getGrundData().getPatient();
            populatePatientDetailsFromPatient(utkast, patient);
        }

        replacePatientPersonnummerWithNew(utkast, copyRequest);

        builderResponse.setUtkastCopy(utkast);

        return builderResponse;
    }

    /* (non-Javadoc)
     * @see se.inera.intyg.webcert.web.service.utkast.CopyUtkastBuilder#populateCopyUtkastFromOrignalUtkast(se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftCopyRequest, se.inera.intyg.webcert.integration.pu.model.Person)
     */
    @Override
    @Transactional(value = "jpaTransactionManager", readOnly = true)
    public CopyUtkastBuilderResponse populateCopyUtkastFromOrignalUtkast(CreateCopyRequest copyRequest, Person patientDetails, boolean addRelation) throws ModuleNotFoundException,
            ModuleException {

        String orignalIntygsId = copyRequest.getOriginalIntygId();

        Utkast orgUtkast = utkastRepository.findOne(orignalIntygsId);

        CopyUtkastBuilderResponse builderResponse = new CopyUtkastBuilderResponse();
        builderResponse.setOrginalEnhetsId(orgUtkast.getEnhetsId());
        builderResponse.setOrginalEnhetsNamn(orgUtkast.getEnhetsNamn());
        builderResponse.setOrginalVardgivarId(orgUtkast.getVardgivarId());
        builderResponse.setOrginalVardgivarNamn(orgUtkast.getVardgivarNamn());

        LOG.debug("Populating copy with details from Utkast '{}'", orignalIntygsId);

        ModuleApi moduleApi = moduleRegistry.getModuleApi(orgUtkast.getIntygsTyp());

        // Set relation to null if not applicable
        Relation relation = addRelation ? createRelation((CreateCompletionCopyRequest) copyRequest, RelationKod.KOMPLT) : null;

        CreateDraftCopyHolder draftCopyHolder = createModuleRequestForCopying(copyRequest, patientDetails, relation);

        InternalModelResponse draftResponse = moduleApi.createNewInternalFromTemplate(draftCopyHolder,
                new InternalModelHolder(orgUtkast.getModel()));

        String draftCopyJson = draftResponse.getInternalModel();

        UtkastStatus utkastStatus = validateDraft(moduleApi, draftCopyJson);

        Utkast utkast = buildUtkastCopy(copyRequest, draftCopyHolder.getCertificateId(), orgUtkast.getIntygsTyp(), addRelation, relation, draftCopyJson, utkastStatus);

        if (patientDetails != null) {
            populatePatientDetailsFromPerson(utkast, patientDetails);
        } else {
            populatePatientDetailsFromUtkast(utkast, orgUtkast);
        }

        replacePatientPersonnummerWithNew(utkast, copyRequest);

        builderResponse.setUtkastCopy(utkast);

        return builderResponse;
    }

    private Utkast buildUtkastCopy(CreateCopyRequest copyRequest, String utkastId, String utkastTyp, boolean addRelation, Relation relation,
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

    private Relation createRelation(CreateCompletionCopyRequest request, RelationKod relationKod) {
        Relation relation = new Relation();
        relation.setRelationIntygsId(request.getOriginalIntygId());
        relation.setRelationKod(relationKod);
        relation.setMeddelandeId(request.getMeddelandeId());
        return relation;
    }

    private void enrichWithRelation(Utkast utkast, Relation relation) {
        utkast.setRelationIntygsId(relation.getRelationIntygsId());
        utkast.setRelationKod(relation.getRelationKod());
    }

    private CreateDraftCopyHolder createModuleRequestForCopying(CreateCopyRequest copyRequest, Person person, Relation relation) {

        String newDraftCopyId = intygsIdStrategy.createId();

        LOG.debug("Created id '{}' for the new copy", newDraftCopyId);

        Vardgivare reqVardgivare = copyRequest.getVardenhet().getVardgivare();
        se.inera.intyg.common.support.modules.support.api.dto.Vardgivare vardgivare = new se.inera.intyg.common.support.modules.support.api.dto.Vardgivare(
                reqVardgivare.getHsaId(), reqVardgivare.getNamn());

        Vardenhet reqVardenhet = copyRequest.getVardenhet();
        se.inera.intyg.common.support.modules.support.api.dto.Vardenhet vardenhet = new se.inera.intyg.common.support.modules.support.api.dto.Vardenhet(
                reqVardenhet.getHsaId(), reqVardenhet.getNamn(), reqVardenhet.getPostadress(),
                reqVardenhet.getPostnummer(), reqVardenhet.getPostort(), reqVardenhet.getTelefonnummer(), reqVardenhet.getEpost(),
                reqVardenhet.getArbetsplatskod(), vardgivare);

        HoSPerson reqHosPerson = copyRequest.getHosPerson();
        se.inera.intyg.common.support.modules.support.api.dto.HoSPersonal hosPerson = new se.inera.intyg.common.support.modules.support.api.dto.HoSPersonal(
                reqHosPerson.getHsaId(),
                reqHosPerson.getNamn(), reqHosPerson.getForskrivarkod(), reqHosPerson.getBefattning(), reqHosPerson.getSpecialiseringar(), vardenhet);

        CreateDraftCopyHolder newDraftCopyHolder = new CreateDraftCopyHolder(newDraftCopyId, hosPerson, relation);

        if (person != null) {
            se.inera.intyg.common.support.modules.support.api.dto.Patient patient = new se.inera.intyg.common.support.modules.support.api.dto.Patient(
                    person.getFornamn(),
                    person.getMellannamn(), person.getEfternamn(), person.getPersonnummer(), person.getPostadress(), person.getPostnummer(),
                    person.getPostort());
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
        Vardenhet vardenhet = copyRequest.getVardenhet();

        utkast.setEnhetsId(vardenhet.getHsaId());
        utkast.setEnhetsNamn(vardenhet.getNamn());

        Vardgivare vardgivare = vardenhet.getVardgivare();

        utkast.setVardgivarId(vardgivare.getHsaId());
        utkast.setVardgivarNamn(vardgivare.getNamn());

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

    private void populatePatientDetailsFromPatient(Utkast utkast, se.inera.intyg.common.support.model.common.internal.Patient patient) {
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

        if (StringUtils.isBlank(fullName)) {
            return res;
        }

        // use the last name from the template efternamn as efternamn and the rest as fornamn.
        String[] nameParts = StringUtils.split(fullName, SPACE);

        if (nameParts.length == 1) {
            res[0] = nameParts[0];
        } else {
            res[0] = StringUtils.join(ArrayUtils.subarray(nameParts, 0, nameParts.length - 1), SPACE);
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

        InternalModelHolder internalModel = new InternalModelHolder(draftCopyJson);
        ValidateDraftResponse validationResponse = moduleApi.validateDraft(internalModel);

        ValidationStatus validationStatus = validationResponse.getStatus();

        return (ValidationStatus.VALID.equals(validationStatus)) ? UtkastStatus.DRAFT_COMPLETE : UtkastStatus.DRAFT_INCOMPLETE;
    }
}

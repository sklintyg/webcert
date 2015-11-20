package se.inera.intyg.webcert.web.service.utkast;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import se.inera.certificate.model.common.internal.GrundData;
import se.inera.certificate.modules.registry.IntygModuleRegistry;
import se.inera.certificate.modules.registry.ModuleNotFoundException;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.CreateDraftCopyHolder;
import se.inera.certificate.modules.support.api.dto.InternalModelHolder;
import se.inera.certificate.modules.support.api.dto.InternalModelResponse;
import se.inera.certificate.modules.support.api.dto.ValidateDraftResponse;
import se.inera.certificate.modules.support.api.dto.ValidationStatus;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.webcert.pu.model.Person;
import se.inera.intyg.webcert.web.service.util.UpdateUserUtil;
import se.inera.intyg.webcert.web.service.dto.HoSPerson;
import se.inera.intyg.webcert.web.service.dto.Vardenhet;
import se.inera.intyg.webcert.web.service.dto.Vardgivare;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.utkast.dto.CopyUtkastBuilderResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftCopyRequest;
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
     * @see se.inera.intyg.webcert.web.service.utkast.CopyUtkastBuilder#populateCopyUtkastFromSignedIntyg(se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftCopyRequest, se.inera.webcert.pu.model.Person)
     */
    @Override
    public CopyUtkastBuilderResponse populateCopyUtkastFromSignedIntyg(CreateNewDraftCopyRequest copyRequest, Person patientDetails) throws ModuleNotFoundException,
            ModuleException {

        String orignalIntygsId = copyRequest.getOriginalIntygId();
        String intygsTyp = copyRequest.getTyp();

        IntygContentHolder signedIntygHolder = intygService.fetchIntygData(orignalIntygsId, intygsTyp);

        LOG.debug("Populating copy with details from signed Intyg '{}'", orignalIntygsId);

        GrundData grundData = signedIntygHolder.getUtlatande().getGrundData();

        CopyUtkastBuilderResponse builderResponse = new CopyUtkastBuilderResponse();
        se.inera.certificate.model.common.internal.Vardenhet vardenhet = grundData.getSkapadAv().getVardenhet();
        builderResponse.setOrginalEnhetsId(vardenhet.getEnhetsid());
        builderResponse.setOrginalEnhetsNamn(vardenhet.getEnhetsnamn());
        builderResponse.setOrginalVardgivarId(vardenhet.getVardgivare().getVardgivarid());
        builderResponse.setOrginalVardgivarNamn(vardenhet.getVardgivare().getVardgivarnamn());

        ModuleApi moduleApi = moduleRegistry.getModuleApi(intygsTyp);

        CreateDraftCopyHolder draftCopyHolder = createModuleRequestForCopying(copyRequest, patientDetails);
        InternalModelResponse draftResponse = moduleApi.createNewInternalFromTemplate(draftCopyHolder,
                new InternalModelHolder(signedIntygHolder.getContents()));

        String draftCopyJson = draftResponse.getInternalModel();

        UtkastStatus utkastStatus = validateDraft(moduleApi, draftCopyJson);

        Utkast utkast = new Utkast();

        utkast.setIntygsId(draftCopyHolder.getCertificateId());
        utkast.setIntygsTyp(intygsTyp);
        utkast.setStatus(utkastStatus);
        utkast.setModel(draftCopyJson);

        if (patientDetails != null) {
            populatePatientDetailsFromPerson(utkast, patientDetails);
        } else {
            se.inera.certificate.model.common.internal.Patient patient = signedIntygHolder.getUtlatande().getGrundData().getPatient();
            populatePatientDetailsFromPatient(utkast, patient);
        }

        populateUtkastWithVardenhetAndHoSPerson(utkast, copyRequest);

        replacePatientPersonnummerWithNew(utkast, copyRequest);

        builderResponse.setUtkastCopy(utkast);

        return builderResponse;
    }

    /* (non-Javadoc)
     * @see se.inera.intyg.webcert.web.service.utkast.CopyUtkastBuilder#populateCopyUtkastFromOrignalUtkast(se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftCopyRequest, se.inera.webcert.pu.model.Person)
     */
    @Override
    @Transactional(value = "jpaTransactionManager", readOnly = true)
    public CopyUtkastBuilderResponse populateCopyUtkastFromOrignalUtkast(CreateNewDraftCopyRequest copyRequest, Person patientDetails) throws ModuleNotFoundException,
            ModuleException {

        String orignalIntygsId = copyRequest.getOriginalIntygId();

        Utkast orgUtkast = utkastRepository.findOne(orignalIntygsId);

        // TODO throw exception if not found

        CopyUtkastBuilderResponse builderResponse = new CopyUtkastBuilderResponse();
        builderResponse.setOrginalEnhetsId(orgUtkast.getEnhetsId());
        builderResponse.setOrginalEnhetsNamn(orgUtkast.getEnhetsNamn());
        builderResponse.setOrginalVardgivarId(orgUtkast.getVardgivarId());
        builderResponse.setOrginalVardgivarNamn(orgUtkast.getVardgivarNamn());

        LOG.debug("Populating copy with details from Utkast '{}'", orignalIntygsId);

        ModuleApi moduleApi = moduleRegistry.getModuleApi(orgUtkast.getIntygsTyp());

        CreateDraftCopyHolder draftCopyHolder = createModuleRequestForCopying(copyRequest, patientDetails);
        InternalModelResponse draftResponse = moduleApi.createNewInternalFromTemplate(draftCopyHolder,
                new InternalModelHolder(orgUtkast.getModel()));

        String draftCopyJson = draftResponse.getInternalModel();

        UtkastStatus utkastStatus = validateDraft(moduleApi, draftCopyJson);

        Utkast utkast = new Utkast();

        utkast.setIntygsId(draftCopyHolder.getCertificateId());
        utkast.setIntygsTyp(orgUtkast.getIntygsTyp());
        utkast.setStatus(utkastStatus);
        utkast.setModel(draftCopyJson);

        if (patientDetails != null) {
            populatePatientDetailsFromPerson(utkast, patientDetails);
        } else {
            populatePatientDetailsFromUtkast(utkast, orgUtkast);
        }

        populateUtkastWithVardenhetAndHoSPerson(utkast, copyRequest);

        replacePatientPersonnummerWithNew(utkast, copyRequest);

        builderResponse.setUtkastCopy(utkast);

        return builderResponse;
    }

    private CreateDraftCopyHolder createModuleRequestForCopying(CreateNewDraftCopyRequest copyRequest, Person person) {

        String newDraftCopyId = intygsIdStrategy.createId();

        LOG.debug("Created id '{}' for the new copy", newDraftCopyId);

        Vardgivare reqVardgivare = copyRequest.getVardenhet().getVardgivare();
        se.inera.certificate.modules.support.api.dto.Vardgivare vardgivare = new se.inera.certificate.modules.support.api.dto.Vardgivare(
                reqVardgivare.getHsaId(), reqVardgivare.getNamn());

        Vardenhet reqVardenhet = copyRequest.getVardenhet();
        se.inera.certificate.modules.support.api.dto.Vardenhet vardenhet = new se.inera.certificate.modules.support.api.dto.Vardenhet(
                reqVardenhet.getHsaId(), reqVardenhet.getNamn(), reqVardenhet.getPostadress(),
                reqVardenhet.getPostnummer(), reqVardenhet.getPostort(), reqVardenhet.getTelefonnummer(), reqVardenhet.getEpost(),
                reqVardenhet.getArbetsplatskod(), vardgivare);

        HoSPerson reqHosPerson = copyRequest.getHosPerson();
        se.inera.certificate.modules.support.api.dto.HoSPersonal hosPerson = new se.inera.certificate.modules.support.api.dto.HoSPersonal(
                reqHosPerson.getHsaId(),
                reqHosPerson.getNamn(), reqHosPerson.getForskrivarkod(), reqHosPerson.getBefattning(), reqHosPerson.getSpecialiseringar(), vardenhet);

        CreateDraftCopyHolder newDraftCopyHolder = new CreateDraftCopyHolder(newDraftCopyId, hosPerson);

        if (person != null) {
            se.inera.certificate.modules.support.api.dto.Patient patient = new se.inera.certificate.modules.support.api.dto.Patient(
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

    private void populateUtkastWithVardenhetAndHoSPerson(Utkast utkast, CreateNewDraftCopyRequest copyRequest) {
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

    private void populatePatientDetailsFromPatient(Utkast utkast, se.inera.certificate.model.common.internal.Patient patient) {
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

    private void replacePatientPersonnummerWithNew(Utkast utkast, CreateNewDraftCopyRequest copyRequest) {
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

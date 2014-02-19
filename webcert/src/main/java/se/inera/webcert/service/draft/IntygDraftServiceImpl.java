package se.inera.webcert.service.draft;

import java.text.MessageFormat;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.inera.webcert.modules.ModuleRestApiFactory;
import se.inera.webcert.modules.api.ModuleRestApi;
import se.inera.webcert.modules.api.dto.CreateNewIntygModuleRequest;
import se.inera.webcert.modules.api.dto.DraftValidationMessage;
import se.inera.webcert.modules.api.dto.DraftValidationResponse;
import se.inera.webcert.modules.api.dto.Patient;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.model.VardpersonReferens;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.service.draft.dto.CreateNewDraftRequest;
import se.inera.webcert.service.draft.dto.DraftValidation;
import se.inera.webcert.service.draft.dto.DraftValidationStatus;
import se.inera.webcert.service.draft.dto.SaveAndValidateDraftRequest;
import se.inera.webcert.service.draft.util.CreateIntygsIdStrategy;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.dto.Vardenhet;
import se.inera.webcert.service.dto.Vardgivare;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;

@Service
public class IntygDraftServiceImpl implements IntygDraftService {

    private static Logger LOG = LoggerFactory.getLogger(IntygDraftServiceImpl.class);

    @Autowired
    private IntygRepository intygRepository;

    @Autowired
    private ModuleRestApiFactory moduleApiFactory;

    @Autowired
    private CreateIntygsIdStrategy intygsIdStrategy;

    public IntygDraftServiceImpl() {

    }

    @Override
    @Transactional
    public String createNewDraft(CreateNewDraftRequest request) {

        populateRequestWithIntygId(request);

        String intygType = request.getIntygType();

        CreateNewIntygModuleRequest moduleRequest = createModuleRequest(request);

        String intygJsonModel = getPopulatedModelFromIntygModule(intygType, moduleRequest);

        String persistedIntygId = persistNewDraft(request, intygJsonModel);

        return persistedIntygId;
    }

    private void populateRequestWithIntygId(CreateNewDraftRequest request) {

        if (StringUtils.isNotBlank(request.getIntygId())) {
            LOG.debug("Detected that the CreateNewDraftRequest already contains an intygId!");
            return;
        }

        String generatedIntygId = intygsIdStrategy.createId();
        request.setIntygId(generatedIntygId);

        LOG.debug("Created id '{}' for the new draft", generatedIntygId);
    }

    private String persistNewDraft(CreateNewDraftRequest request, String draftAsJson) {

        Intyg draft = new Intyg();
        
        se.inera.webcert.service.dto.Patient patient = request.getPatient();
        
        draft.setPatientPersonnummer(patient.getPersonNummer());
        draft.setPatientFornamn(patient.getForNamn());
        draft.setPatientEfternamn(patient.getEfterNamn());
        
        draft.setIntygsId(request.getIntygId());
        draft.setIntygsTyp(request.getIntygType());

        draft.setStatus(IntygsStatus.DRAFT_INCOMPLETE);

        draft.setModel(draftAsJson);

        Vardenhet vardenhet = request.getVardenhet();

        draft.setEnhetsId(vardenhet.getHsaId());
        draft.setEnhetsNamn(vardenhet.getNamn());

        Vardgivare vardgivare = vardenhet.getVardgivare();

        draft.setVardgivarId(vardgivare.getHsaId());
        draft.setVardgivarNamn(vardgivare.getNamn());

        VardpersonReferens creator = createVardpersonFromHosPerson(request.getHosPerson());

        draft.setSenastSparadAv(creator);
        draft.setSkapadAv(creator);

        Intyg savedDraft = intygRepository.save(draft);

        LOG.debug("Draft '{}' persisted", savedDraft.getIntygsId());
        
        return savedDraft.getIntygsId();
    }

    private VardpersonReferens createVardpersonFromHosPerson(HoSPerson hosPerson) {

        VardpersonReferens vardPerson = new VardpersonReferens();
        vardPerson.setNamn(hosPerson.getNamn());
        vardPerson.setHsaId(hosPerson.getHsaId());

        return vardPerson;
    }

    private String getPopulatedModelFromIntygModule(String intygType, CreateNewIntygModuleRequest moduleRequest) {

        LOG.debug("Calling module '{}' to get populated model", intygType);

        ModuleRestApi moduleRestService = moduleApiFactory.getModuleRestService(intygType);

        Response response = moduleRestService.createModel(moduleRequest);

        StatusType callStatus = response.getStatusInfo();

        if (!callStatus.getFamily().equals(Family.SUCCESSFUL)) {
            String msg = MessageFormat.format("Call to /create in module {0} failed with HTTP code {1}!", intygType,
                    callStatus.getStatusCode());
            LOG.error(msg);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM, msg);
        }

        String modelAsJson = response.readEntity(String.class);

        LOG.debug("Got populated model of {} chars from module '{}'", getSafeLength(modelAsJson), intygType);

        return modelAsJson;
    }

    private int getSafeLength(String str) {
        return (StringUtils.isNotBlank(str)) ? str.length() : 0;
    }

    private CreateNewIntygModuleRequest createModuleRequest(CreateNewDraftRequest draftReq) {

        CreateNewIntygModuleRequest modReq = new CreateNewIntygModuleRequest();
        modReq.setCertificateId(draftReq.getIntygId());

        se.inera.webcert.service.dto.Patient reqPat = draftReq.getPatient();

        Patient mrPat = new Patient();
        mrPat.setPersonnummer(reqPat.getPersonNummer());
        mrPat.setFornamn(reqPat.getForNamn());
        mrPat.setEfternamn(reqPat.getEfterNamn());
        // TODO: Populate with Patients address info
        
        modReq.setPatientInfo(mrPat);

        Vardenhet drVardenhet = draftReq.getVardenhet();
        
        Vardgivare drVardgivare = drVardenhet.getVardgivare();
        
        se.inera.webcert.modules.api.dto.Vardgivare mrVardgivare = new se.inera.webcert.modules.api.dto.Vardgivare();
        mrVardgivare.setHsaId(drVardgivare.getHsaId());
        mrVardgivare.setNamn(drVardgivare.getNamn());
        
        se.inera.webcert.modules.api.dto.Vardenhet mrVardenhet = new se.inera.webcert.modules.api.dto.Vardenhet();
        mrVardenhet.setHsaId(drVardenhet.getHsaId());
        mrVardenhet.setNamn(drVardenhet.getNamn());
        mrVardenhet.setVardgivare(mrVardgivare);
        
        HoSPerson reqHosp = draftReq.getHosPerson();

        se.inera.webcert.modules.api.dto.HoSPersonal mrHosp = new se.inera.webcert.modules.api.dto.HoSPersonal();
        mrHosp.setNamn(reqHosp.getNamn());
        mrHosp.setHsaId(reqHosp.getHsaId());
        mrHosp.setForskrivarkod(reqHosp.getForskrivarkod());
        // TODO: Populate with befattning
        
        mrHosp.setVardenhet(mrVardenhet);
        
        modReq.setSkapadAv(mrHosp);
        
        return modReq;
    }

    @Override
    @Transactional
    public DraftValidation saveAndValidateDraft(SaveAndValidateDraftRequest request) {

        String  intygId = request.getIntygId();
        
        LOG.debug("Saving and validating Intyg with id '{}'", intygId);

        Intyg intyg = intygRepository.findOne(intygId);
        
        if (intyg == null) {
            LOG.warn("Intyg with id '{}' was not found", intygId);
            return null;
        }

        String intygType = intyg.getIntygsTyp();

        String draftAsJson = request.getDraftAsJson();
        
        DraftValidation draftValidation = validateDraft(intygId, intygType, draftAsJson);

        IntygsStatus intygStatus = (draftValidation.isDraftValid()) ? IntygsStatus.DRAFT_COMPLETE
                : IntygsStatus.DRAFT_INCOMPLETE;

        intyg.setModel(draftAsJson);
        intyg.setStatus(intygStatus);

        VardpersonReferens vardPersonRef = createVardpersonFromHosPerson(request.getSavedBy());
        intyg.setSenastSparadAv(vardPersonRef);

        intygRepository.save(intyg);
        
        LOG.debug("Intyg '{}' updated", intygId);

        return draftValidation;
    }

    @Override
    public DraftValidation validateDraft(String intygId, String intygType, String draftAsJson) {
        LOG.debug("Validating Intyg with id {} and type {}", intygId, intygType);

        ModuleRestApi moduleRestService = moduleApiFactory.getModuleRestService(intygType);

        Response response = moduleRestService.validate(draftAsJson);

        StatusType callStatus = response.getStatusInfo();

        if (!callStatus.getFamily().equals(Family.SUCCESSFUL)) {
            String msg = MessageFormat.format(
                    "Call to /valid-draft for intyg {0} in module {1} failed with HTTP code {2}!", intygId,
                    intygType, callStatus.getStatusCode());
            LOG.error(msg);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM, msg);
        }

        DraftValidationResponse draftValidationResponse = response.readEntity(DraftValidationResponse.class);

        DraftValidation draftValidation = convertToDraftValidation(draftValidationResponse);

        return draftValidation;
    }

    private DraftValidation convertToDraftValidation(DraftValidationResponse dr) {
        DraftValidation dv = new DraftValidation();

        if (dr.checkIfValidAndEmpty()) {
            LOG.debug("Validation is OK");
            return dv;
        }

        dv.setStatus(DraftValidationStatus.INVALID);

        for (DraftValidationMessage drMsg : dr.getValidationErrors()) {
            dv.addMessage(new se.inera.webcert.service.draft.dto.DraftValidationMessage(drMsg.getField(), drMsg
                    .getMessage()));
        }

        LOG.debug("Validation failed with {} validation messages", dv.getMessages().size());

        return dv;
    }

}

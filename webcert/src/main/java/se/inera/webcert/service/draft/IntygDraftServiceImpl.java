package se.inera.webcert.service.draft;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.modules.ModuleRestApiFactory;
import se.inera.webcert.modules.api.ModuleRestApi;
import se.inera.webcert.modules.api.dto.CreateNewIntygModuleRequest;
import se.inera.webcert.modules.api.dto.CreateNewIntygModuleResponse;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.model.VardpersonReferens;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.service.draft.dto.DraftValidation;
import se.inera.webcert.service.draft.util.CreateIntygsIdStrategy;
import se.inera.webcert.web.service.WebCertUserService;

@Service
public class IntygDraftServiceImpl implements IntygDraftService {

    private static Logger LOG = LoggerFactory.getLogger(IntygDraftServiceImpl.class);

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private IntygRepository intygRepository;

    @Autowired
    private ModuleRestApiFactory moduleApiFactory;

    @Autowired
    private CreateIntygsIdStrategy intygsIdStrategy;

    public IntygDraftServiceImpl() {

    }

    @Override
    public String createNewDraft(String patientId, String intygType) {

        String generatedIntygId = intygsIdStrategy.createId();

        CreateNewIntygModuleRequest moduleRequest = createModuleRequest(generatedIntygId, patientId);

        String intygJsonModel = getPopulatedModelFromIntygModule(intygType, moduleRequest);

        persistNewDraft(generatedIntygId, intygType, intygJsonModel);
        
        return generatedIntygId;
    }

    private void persistNewDraft(String intygsId, String intygsTyp, String model) {

        Intyg draft = new Intyg();
        
        draft.setIntygsId(intygsId);
        draft.setIntygsTyp(intygsTyp);
        draft.setStatus(IntygsStatus.DRAFT_INCOMPLETE);
        draft.setModel(model);

        // TODO How do we find out which care unit this entity should belong to?
        
        // draft.setEnhetsId(enhetsId);
        // draft.setEnhetsNamn(enhetsNamn);

        // draft.setVardgivarId(vardgivarId);
        // draft.setVardgivarNamn(vardgivarNamn);

        VardpersonReferens creator = createVardperson();

        draft.setSenastSparadAv(creator);
        draft.setSkapadAv(creator);
                
        intygRepository.save(draft);
    }

    private VardpersonReferens createVardperson() {

        WebCertUser user = webCertUserService.getWebCertUser();

        VardpersonReferens vardPerson = new VardpersonReferens();
        vardPerson.setNamn(user.getNamn());
        vardPerson.setHsaId(user.getHsaId());

        return null;
    }

    private String getPopulatedModelFromIntygModule(String intygType, CreateNewIntygModuleRequest moduleRequest) {

        ModuleRestApi moduleRestService = moduleApiFactory.getModuleRestService(intygType);

        Response response = moduleRestService.createModel(moduleRequest);

        CreateNewIntygModuleResponse createModelResponse = response.readEntity(CreateNewIntygModuleResponse.class);

        String modelAsJson = createModelResponse.getContents();

        return modelAsJson;
    }

    private CreateNewIntygModuleRequest createModuleRequest(String intygId, String patientId) {

        CreateNewIntygModuleRequest moduleRequest = new CreateNewIntygModuleRequest();
        moduleRequest.setCertificateId(intygId);

        // TODO: Populate with user

        // TODO: How do we find out which careUnit to set as owner of this?

        return null;
    }

    @Override
    @Transactional
    public DraftValidation saveAndValidateDraft(String intygId, String draftAsJson) {
        
        LOG.debug("Saving and validating Intyg with id {}", intygId);
        
        Intyg intyg = intygRepository.findOne(intygId);
        
        if (intyg == null) {
            LOG.warn("Intyg with id {} was not found", intygId);
            return null;
        }
                
        String intygType = intyg.getIntygsTyp();
        
        DraftValidation draftValidation = validateDraft(intygId, intygType, draftAsJson);
        
        IntygsStatus intygStatus = (draftValidation.isDraftValid()) ? IntygsStatus.DRAFT_COMPLETE : IntygsStatus.DRAFT_INCOMPLETE;
                
        intyg.setModel(draftAsJson);
        intyg.setStatus(intygStatus);
        
        VardpersonReferens vardPersonRef = createVardperson();
        intyg.setSenastSparadAv(vardPersonRef);
        
        intygRepository.save(intyg);
        
        return draftValidation;
    }

    @Override
    public DraftValidation validateDraft(String intygId, String intygType, String draft) {
        LOG.debug("Validating Intyg with id {} and type {}", intygId, intygType);
        return new DraftValidation();
    }

}

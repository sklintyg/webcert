package se.inera.webcert.service.draft;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
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
import se.inera.webcert.modules.api.dto.DraftValidationMessage;
import se.inera.webcert.modules.api.dto.DraftValidationResponse;
import se.inera.webcert.modules.api.dto.HoSPersonal;
import se.inera.webcert.modules.api.dto.Patient;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.model.VardpersonReferens;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.service.draft.dto.CreateNewDraftRequest;
import se.inera.webcert.service.draft.dto.DraftValidation;
import se.inera.webcert.service.draft.dto.DraftValidationStatus;
import se.inera.webcert.service.draft.util.CreateIntygsIdStrategy;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.dto.Vardenhet;
import se.inera.webcert.service.dto.Vardgivare;
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
    @Transactional
    public String createNewDraft(CreateNewDraftRequest request) {
        
        populateRequestWithIntygId(request);
        
        String intygType = request.getIntygType();
        
        CreateNewIntygModuleRequest moduleRequest = createModuleRequest(request);
        
        String intygJsonModel = getPopulatedModelFromIntygModule(intygType, moduleRequest);

        persistNewDraft(request, intygJsonModel);
        
        return request.getIntygId();
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
    

    private void persistNewDraft(CreateNewDraftRequest request, String draftAsJson) {

        Intyg draft = new Intyg();
        
        draft.setPatientPersonnummer(request.getPatient().getPersonNummer());
        
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
    }

    private VardpersonReferens createVardpersonFromHosPerson(HoSPerson hosPerson) {
        
        VardpersonReferens vardPerson = new VardpersonReferens();
        vardPerson.setNamn(hosPerson.getNamn());
        vardPerson.setHsaId(hosPerson.getHsaId());

        return vardPerson;
    }

    private VardpersonReferens createVardperson() {

        WebCertUser user = webCertUserService.getWebCertUser();

        VardpersonReferens vardPerson = new VardpersonReferens();
        vardPerson.setNamn(user.getNamn());
        vardPerson.setHsaId(user.getHsaId());

        return vardPerson;
    }

    private String getPopulatedModelFromIntygModule(String intygType, CreateNewIntygModuleRequest moduleRequest) {
        
        LOG.debug("Calling module '{}' to get populated model", intygType);
        
        ModuleRestApi moduleRestService = moduleApiFactory.getModuleRestService(intygType);
        
        // TODO What happens if this fails? Throw exception?
        Response response = moduleRestService.createModel(moduleRequest);
        
        CreateNewIntygModuleResponse createModelResponse = response.readEntity(CreateNewIntygModuleResponse.class);

        String modelAsJson = createModelResponse.getContents();

        return modelAsJson;
    }

    private CreateNewIntygModuleRequest createModuleRequest(CreateNewDraftRequest req) {

        CreateNewIntygModuleRequest mr = new CreateNewIntygModuleRequest();
        mr.setCertificateId(req.getIntygId());
                
        se.inera.webcert.service.dto.Patient reqPat = req.getPatient();
                
        Patient mrPat = new Patient();
        mrPat.setPersonNummer(reqPat.getPersonNummer());
        mrPat.setForNamn(reqPat.getForNamn());
        mrPat.setEfterNamn(reqPat.getEfterNamn());
        // TODO: Populate with Patients address info
        mr.setPatientInfo(mrPat);

        HoSPerson reqHosp = req.getHosPerson();
        
        HoSPersonal mrHosp = new HoSPersonal();
        mrHosp.setNamn(reqHosp.getNamn());
        mrHosp.setHsaId(reqHosp.getHsaId());
        mrHosp.setForskrivarkod(reqHosp.getForskrivarkod());
        // TODO: Populate with befattning
                
        mr.setSkapadAv(mrHosp);
        
        // TODO: Populate with careUnit and careGiver

        return mr;
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
        
        // TODO This needs to be moved to the controller for SoC
        VardpersonReferens vardPersonRef = createVardperson();
        intyg.setSenastSparadAv(vardPersonRef);
        
        intygRepository.save(intyg);
        
        return draftValidation;
    }

    @Override
    public DraftValidation validateDraft(String intygId, String intygType, String draftAsJson) {
        LOG.debug("Validating Intyg with id {} and type {}", intygId, intygType);
        
        ModuleRestApi moduleRestService = moduleApiFactory.getModuleRestService(intygType);
        
        Response response = moduleRestService.validate(draftAsJson);
        
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
        
        for (DraftValidationMessage drMsg : dr.getMessages()) {
            dv.addMessage(new se.inera.webcert.service.draft.dto.DraftValidationMessage(drMsg.getField(), drMsg.getMessage()));
        }
        
        LOG.debug("Validation failed with {} validation messages", dv.getMessages().size());
        
        return dv;
    }
    
}

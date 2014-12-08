package se.inera.webcert.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.CreateDraftCertificateResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.CreateDraftCertificateResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.CreateDraftCertificateType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.UtlatandeType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.UtlatandeId;
import se.inera.certificate.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultType;
import se.inera.ifv.hsawsresponder.v3.MiuInformationType;
import se.inera.webcert.hsa.services.HsaPersonService;
import se.inera.webcert.integration.builder.CreateNewDraftRequestBuilder;
import se.inera.webcert.integration.registry.IntegreradeEnheterRegistry;
import se.inera.webcert.integration.registry.dto.IntegreradEnhetEntry;
import se.inera.webcert.integration.validator.CreateDraftCertificateValidator;
import se.inera.webcert.integration.validator.ValidationResult;
import se.inera.webcert.service.draft.IntygDraftService;
import se.inera.webcert.service.draft.dto.CreateNewDraftRequest;
import se.inera.webcert.service.dto.Vardenhet;
import se.inera.webcert.service.dto.Vardgivare;

import java.util.List;

public class CreateDraftCertificateResponderImpl implements CreateDraftCertificateResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(CreateDraftCertificateResponderImpl.class);

    @Autowired
    private IntygDraftService intygsUtkastService;

    @Autowired
    private HsaPersonService hsaPersonService;

    @Autowired
    private CreateNewDraftRequestBuilder draftRequestBuilder;

    @Autowired
    private CreateDraftCertificateValidator validator;

    @Autowired
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    @Override
    @Transactional
    public CreateDraftCertificateResponseType createDraftCertificate(String logicalAddress, CreateDraftCertificateType parameters) {

        UtlatandeType utkastsParams = parameters.getUtlatande();

        ValidationResult validationResults = validator.validate(utkastsParams);

        if (validationResults.hasErrors()) {
            String errMsgs = validationResults.getErrorMessagesAsString();
            LOG.warn("UtlatandeType did not validate correctly: {}", errMsgs);
            return createErrorResponse(errMsgs, ErrorIdType.VALIDATION_ERROR);
        }

        String invokingUserHsaId = utkastsParams.getSkapadAv().getPersonalId().getExtension();
        String invokingUnitHsaId = utkastsParams.getSkapadAv().getEnhet().getEnhetsId().getExtension();

        LOG.debug("Creating draft for invoker '{}' on unit '{}'", invokingUserHsaId, invokingUnitHsaId);

        MiuInformationType unitMIU = checkIfInvokingPersonHasMIUsOnUnit(
                invokingUserHsaId, invokingUnitHsaId);

        if (unitMIU == null) {
            String errMsg = String.format("No valid MIU was found for person %s on unit %s, can not create draft!", invokingUserHsaId,
                    invokingUnitHsaId);
            LOG.error(errMsg);
            return createErrorResponse(errMsg, ErrorIdType.VALIDATION_ERROR);
        }

        CreateNewDraftRequest utkastsRequest = draftRequestBuilder.buildCreateNewDraftRequest(utkastsParams, unitMIU);

        String nyttUtkastsId = intygsUtkastService.createNewDraft(utkastsRequest);

        addVardenhetToRegistry(utkastsRequest);

        return createSuccessResponse(nyttUtkastsId);
    }

    private MiuInformationType checkIfInvokingPersonHasMIUsOnUnit(
            String invokingUserHsaId, String invokingUnitHsaId) {

        List<MiuInformationType> miusOnUnit = hsaPersonService.checkIfPersonHasMIUsOnUnit(invokingUserHsaId, invokingUnitHsaId);

        switch (miusOnUnit.size()) {
        case 0:
            LOG.error("Found no MIUs for user '{}' on unit '{}', returning null", invokingUserHsaId, invokingUnitHsaId);
            return null;
        case 1:
            return miusOnUnit.get(0);
        default:
            LOG.warn("Found more than one MIU for user '{}' on unit '{}', returning the first one", invokingUserHsaId, invokingUnitHsaId);
            return miusOnUnit.get(0);
        }
    }

    private CreateDraftCertificateResponseType createErrorResponse(String errorMsg, ErrorIdType errorType) {
        CreateDraftCertificateResponseType response = new CreateDraftCertificateResponseType();
        ResultType result = ResultTypeUtil.errorResult(errorType, errorMsg);
        response.setResult(result);

        return response;
    }

    private CreateDraftCertificateResponseType createSuccessResponse(String nyttUtkastsId) {
        CreateDraftCertificateResponseType response = new CreateDraftCertificateResponseType();
        ResultType result = ResultTypeUtil.okResult();
        response.setResult(result);

        UtlatandeId utlId = new UtlatandeId();
        utlId.setRoot("utlatandeId");
        utlId.setExtension(nyttUtkastsId);

        response.setUtlatandeId(utlId);

        return response;
    }

    private void addVardenhetToRegistry(CreateNewDraftRequest utkastsRequest) {

        Vardenhet vardenhet = utkastsRequest.getVardenhet();
        Vardgivare vardgivare = vardenhet.getVardgivare();

        IntegreradEnhetEntry integreradEnhet = new IntegreradEnhetEntry(vardenhet.getHsaId(),
                vardenhet.getNamn(), vardgivare.getHsaId(), vardgivare.getNamn());

        boolean result = integreradeEnheterRegistry.addIfNotExistsIntegreradEnhet(integreradEnhet);

        if (result) {
            LOG.info("Added unit '{}' to registry of integrated units", vardenhet.getHsaId());
        } else {
            LOG.debug("Unit '{}' was alredy present in the registry of integrated units", vardenhet.getHsaId());
        }
    }
}

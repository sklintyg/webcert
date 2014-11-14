package se.inera.webcert.integration;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
import se.inera.webcert.service.draft.IntygDraftService;
import se.inera.webcert.service.draft.dto.CreateNewDraftRequest;

public class CreateDraftCertificateResponderImpl implements CreateDraftCertificateResponderInterface {

	private static final Logger LOG = LoggerFactory.getLogger(CreateDraftCertificateResponderImpl.class);

	@Autowired
	private IntygDraftService intygsUtkastService;
	
	@Autowired
	private HsaPersonService hsaPersonService;
	
	@Autowired
	private CreateNewDraftRequestBuilder draftRequestBuilder;
	
	@Override
	public CreateDraftCertificateResponseType createDraftCertificate(
			String logicalAddress, CreateDraftCertificateType parameters) {
				
		UtlatandeType utkastsParams = parameters.getUtlatande();
		
		String invokingUserHsaId = utkastsParams.getSkapadAv().getPersonalId().getExtension();
		String invokingUnitHsaId = utkastsParams.getSkapadAv().getEnhet().getEnhetsId().getExtension();
		
		LOG.debug("Creating draft for invoker '{}' on unit '{}'", invokingUserHsaId, invokingUnitHsaId);
		
		MiuInformationType unitMIU = checkIfInvokingPersonHasMIUsOnUnit(
				invokingUserHsaId, invokingUnitHsaId);
		
		if (unitMIU == null) {
			String errMsg = String.format("No valid MIU was found for person %s on unit %s, can not create draft!", invokingUserHsaId, invokingUnitHsaId);
			LOG.error(errMsg);
			return createErrorResponse(errMsg, ErrorIdType.VALIDATION_ERROR);
		}
				
		CreateNewDraftRequest utkastsRequest = draftRequestBuilder.buildCreateNewDraftRequest(utkastsParams, unitMIU);
				
		String nyttUtkastsId = intygsUtkastService.createNewDraft(utkastsRequest);
		
		return createSuccessResponse(nyttUtkastsId);
	}

	private MiuInformationType checkIfInvokingPersonHasMIUsOnUnit(
			String invokingUserHsaId, String invokingUnitHsaId) {
		
		List<MiuInformationType> miusOnUnit = hsaPersonService.checkIfPersonHasMIUsOnUnit(invokingUserHsaId, invokingUnitHsaId);
		
		switch (miusOnUnit.size()) {
		case 0:
			LOG.info("Found no MIUs for invoker, returning null");
			return null;
		case 1:
			return miusOnUnit.get(0);
		default:
			LOG.debug("Found more than one MIU for invoker, using the first one");
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
		utlId.setRoot("ABC123");
		utlId.setExtension(nyttUtkastsId);
		
		response.setUtlatandeId(utlId);
		
		return response;
	}
	
}

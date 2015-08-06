package se.inera.webcert.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.persistence.privatlakaravtal.model.Avtal;
import se.inera.webcert.service.privatlakaravtal.AvtalService;
import se.riv.infrastructure.directory.privatepractitioner.approveprivatepractitionerterms.v1.rivtabp21.ApprovePrivatePractitionerTermsResponderInterface;
import se.riv.infrastructure.directory.privatepractitioner.approveprivatepractitionertermsresponder.v1.ApprovePrivatePractitionerTermsResponseType;
import se.riv.infrastructure.directory.privatepractitioner.approveprivatepractitionertermsresponder.v1.ApprovePrivatePractitionerTermsType;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerterms.v1.rivtabp21.GetPrivatePractitionerTermsResponderInterface;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionertermsresponder.v1.GetPrivatePractitionerTermsResponseType;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionertermsresponder.v1.GetPrivatePractitionerTermsType;
import se.riv.infrastructure.directory.privatepractitioner.terms.v1.AvtalGodkannandeType;
import se.riv.infrastructure.directory.privatepractitioner.terms.v1.AvtalType;
import se.riv.infrastructure.directory.privatepractitioner.terms.v1.ResultCodeEnum;

/**
 * Created by eriklupander on 2015-08-05.
 */
public class ApprovePrivatePractitionerTermsResponderImpl implements ApprovePrivatePractitionerTermsResponderInterface {

    private static final Logger log = LoggerFactory.getLogger(ApprovePrivatePractitionerTermsResponderImpl.class);

    @Autowired
    AvtalService avtalService;

    @Override
    public ApprovePrivatePractitionerTermsResponseType approvePrivatePractitionerTerms(String logicalAddress, ApprovePrivatePractitionerTermsType parameters) {
        ApprovePrivatePractitionerTermsResponseType response = new ApprovePrivatePractitionerTermsResponseType();
        response.setResultCode(ResultCodeEnum.OK);

        validateRequest(parameters.getAvtalGodkannande(), response);
        if (response.getResultCode() == ResultCodeEnum.ERROR) {
            return response;
        }

        try {
            avtalService.approveLatestAvtal(parameters.getAvtalGodkannande().getPersonId().getExtension());
        } catch (Exception e) {
            log.error("An exception occured while approving latest private practitioner terms, message: {}", e.getMessage());
            response.setResultCode(ResultCodeEnum.ERROR);
            response.setResultText("Cannot approve private practitioner terms, unexpected exception: " + e.getMessage());
        }
        return response;
    }

    private void validateRequest(AvtalGodkannandeType avtalGodkannandeType, ApprovePrivatePractitionerTermsResponseType response) {
        if (avtalGodkannandeType == null) {
            response.setResultCode(ResultCodeEnum.ERROR);
            response.setResultText("Cannot approve private practitioner terms, request is missing required AvtalGodkannandeType parameter");
            return;
        }

        if (avtalGodkannandeType.getAvtalVersion() < 0) {
            response.setResultCode(ResultCodeEnum.ERROR);
            response.setResultText("Cannot approve private practitioner terms, request is missing required AvtalGodkannandeType.avtalVersion parameter");
            return;
        }

        if (avtalGodkannandeType.getPersonId() == null || avtalGodkannandeType.getPersonId().getExtension() == null || avtalGodkannandeType.getPersonId().getExtension().trim().length() == 0) {
            response.setResultCode(ResultCodeEnum.ERROR);
            response.setResultText("Cannot approve private practitioner terms, request is missing required AvtalGodkannandeType.personId parameter");
            return;
        }
    }
}

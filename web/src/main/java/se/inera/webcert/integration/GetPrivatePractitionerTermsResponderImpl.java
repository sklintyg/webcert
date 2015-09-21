package se.inera.webcert.integration;

import org.springframework.beans.factory.annotation.Autowired;
import se.inera.webcert.persistence.privatlakaravtal.model.Avtal;
import se.inera.webcert.service.privatlakaravtal.AvtalService;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerterms.v1.rivtabp21.GetPrivatePractitionerTermsResponderInterface;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionertermsresponder.v1.GetPrivatePractitionerTermsResponseType;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionertermsresponder.v1.GetPrivatePractitionerTermsType;
import se.riv.infrastructure.directory.privatepractitioner.terms.v1.AvtalType;
import se.riv.infrastructure.directory.privatepractitioner.terms.v1.ResultCodeEnum;

/**
 * Created by eriklupander on 2015-08-05.
 */
public class GetPrivatePractitionerTermsResponderImpl implements GetPrivatePractitionerTermsResponderInterface {

    @Autowired
    AvtalService avtalService;

    @Override
    public GetPrivatePractitionerTermsResponseType getPrivatePractitionerTerms(String logicalAddress, GetPrivatePractitionerTermsType parameters) {

        Avtal latestAvtal = avtalService.getLatestAvtal();
        GetPrivatePractitionerTermsResponseType response = new GetPrivatePractitionerTermsResponseType();

        if (latestAvtal == null) {
            response.setResultCode(ResultCodeEnum.ERROR);
            response.setResultText("No private practitioner terms found");
            return response;
        }

        AvtalType avtalType = new AvtalType();
        avtalType.setAvtalText(latestAvtal.getAvtalText());
        avtalType.setAvtalVersion(latestAvtal.getAvtalVersion());
        response.setAvtal(avtalType);
        response.setResultCode(ResultCodeEnum.OK);
        return response;

    }
}

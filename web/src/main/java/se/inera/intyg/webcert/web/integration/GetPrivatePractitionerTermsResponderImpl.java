package se.inera.intyg.webcert.web.integration;

import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.webcert.persistence.privatlakaravtal.model.Avtal;
import se.inera.intyg.webcert.web.service.privatlakaravtal.AvtalService;
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
    private AvtalService avtalService;

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
        avtalType.setAvtalVersionDatum(latestAvtal.getVersionDatum());
        response.setAvtal(avtalType);
        response.setResultCode(ResultCodeEnum.OK);
        return response;

    }
}

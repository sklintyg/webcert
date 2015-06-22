package se.inera.intyg.webcert.integration.pp.stub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitioner.v1.rivtabp21.GetPrivatePractitionerResponderInterface;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerresponder.v1.GetPrivatePractitionerResponseType;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerresponder.v1.GetPrivatePractitionerType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.ResultCodeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Magnus Ekstrand on 18/06/15.
 */
public class GetPrivatePractitionerResponderStub implements GetPrivatePractitionerResponderInterface {

    @Autowired
    private HoSPersonStub personStub;

    @Override
    public GetPrivatePractitionerResponseType getPrivatePractitioner(String logicalAddress, GetPrivatePractitionerType parameters) {

        validate(parameters);

        GetPrivatePractitionerResponseType response = new GetPrivatePractitionerResponseType();

        String id = parameters.getPersonalIdentityNumber();
        HoSPersonType person  = personStub.get(id);

        if (person == null) {
            response.setResultCode(ResultCodeEnum.INFO);
            response.setResultText("No private practitioner with personal identity number: " + id + " exists.");
        } else {
            response.setHoSPerson(person);
            response.setResultCode(ResultCodeEnum.OK);
        }

        return response;
     }

    private void validate(GetPrivatePractitionerType parameters) {
        List<String> messages = new ArrayList<>();

        if (parameters == null) {
            messages.add("GetPrivatePractitionerType cannot be null.");
        } else {
            String personId = parameters.getPersonalIdentityNumber();

            if (StringUtils.isEmpty(personId)) {
                messages.add("A personal identity number (personalIdentityNumber) must be supplied.");
            }
        }

        if (messages.size() > 0) {
            throw new IllegalArgumentException(StringUtils.collectionToCommaDelimitedString(messages));
        }
    }


}

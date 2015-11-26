package se.inera.intyg.webcert.integration.pp.stub;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import se.inera.intyg.common.util.logging.HashUtility;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.ResultCodeEnum;
import se.riv.infrastructure.directory.privatepractitioner.validateprivatepractitioner.v1.rivtabp21.ValidatePrivatePractitionerResponderInterface;
import se.riv.infrastructure.directory.privatepractitioner.validateprivatepractitionerresponder.v1.ValidatePrivatePractitionerResponseType;
import se.riv.infrastructure.directory.privatepractitioner.validateprivatepractitionerresponder.v1.ValidatePrivatePractitionerType;

/**
 * Created by Erik Lupander 13/08/15.
 */
public class ValidatePrivatePractitionerResponderStub implements ValidatePrivatePractitionerResponderInterface {

    @Autowired
    private HoSPersonStub personStub;

    @Override
    public ValidatePrivatePractitionerResponseType validatePrivatePractitioner(String logicalAddress, ValidatePrivatePractitionerType parameters) {
        // Do validation of parameters object
        validate(parameters);

        String id = parameters.getPersonalIdentityNumber();
        ValidatePrivatePractitionerResponseType response = new ValidatePrivatePractitionerResponseType();
        HoSPersonType person  = personStub.get(id);

        if (person == null) {
            response.setResultCode(ResultCodeEnum.ERROR);
            response.setResultText("No private practitioner with personal identity number: " + HashUtility.hash(id) + " exists.");
        } else if (person.isGodkandAnvandare()) {
            response.setResultCode(ResultCodeEnum.OK);
        } else {
            response.setResultCode(ResultCodeEnum.ERROR);
            response.setResultText("Private practitioner with personal identity number: " + HashUtility.hash(id) + " is not authorized to use webcert.");
        }
        return response;
    }

    private void validate(ValidatePrivatePractitionerType parameters) {
        List<String> messages = new ArrayList<>();

        if (parameters == null) {
            messages.add("ValidatePrivatePractitionerType cannot be null.");
        } else {
            String hsaId = parameters.getPersonHsaId();
            String personId = parameters.getPersonalIdentityNumber();

            // Exakt ett av fälten hsaIdentityNumber och personalIdentityNumber ska anges.
            if (StringUtils.isEmpty(hsaId) && StringUtils.isEmpty(personId)) {
                messages.add("Inget av argumenten hsaId och personId är satt. Ett av dem måste ha ett värde.");
            }

            if (!StringUtils.isEmpty(hsaId) && !StringUtils.isEmpty(personId)) {
                messages.add("Endast ett av argumenten hsaId och personId får vara satt.");
            }
        }

        if (messages.size() > 0) {
            throw new IllegalArgumentException(StringUtils.collectionToCommaDelimitedString(messages));
        }
    }
}

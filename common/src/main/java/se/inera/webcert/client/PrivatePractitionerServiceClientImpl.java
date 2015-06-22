package se.inera.webcert.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitioner.v1.rivtabp21.GetPrivatePractitionerResponderInterface;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerresponder.v1.GetPrivatePractitionerResponseType;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerresponder.v1.GetPrivatePractitionerType;
import se.riv.infrastructure.directory.privatepractitioner.validateprivatepractitioner.v1.rivtabp21.ValidatePrivatePractitionerResponderInterface;
import se.riv.infrastructure.directory.privatepractitioner.validateprivatepractitionerresponder.v1.ValidatePrivatePractitionerResponseType;
import se.riv.infrastructure.directory.privatepractitioner.validateprivatepractitionerresponder.v1.ValidatePrivatePractitionerType;

/**
 * Created by eriklupander on 2015-06-16.
 */
@Component
public class PrivatePractitionerServiceClientImpl implements PrivatePractitionerServiceClient {

    @Autowired
    GetPrivatePractitionerResponderInterface getPrivatePractitionerResponderInterface;

    @Autowired
    ValidatePrivatePractitionerResponderInterface validatePrivatePractitionerResponderInterface;

    @Override
    public ValidatePrivatePractitionerResponseType validatePrivatePractitioner(String personId, String logicalAddress) {
        ValidatePrivatePractitionerType parameters = new ValidatePrivatePractitionerType();
        parameters.setPersonalIdentityNumber(personId);
        return validatePrivatePractitionerResponderInterface.validatePrivatePractitioner(logicalAddress, parameters);
    }

    @Override
    public GetPrivatePractitionerResponseType getPrivatePractitioner(String personId, String logicalAddress) {
        GetPrivatePractitionerType parameters = new GetPrivatePractitionerType();
        parameters.setPersonalIdentityNumber(personId);
        return getPrivatePractitionerResponderInterface.getPrivatePractitioner(logicalAddress, parameters);
    }
}

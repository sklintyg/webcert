package se.inera.webcert.client;

import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerresponder.v1.GetPrivatePractitionerResponseType;
import se.riv.infrastructure.directory.privatepractitioner.validateprivatepractitionerresponder.v1.ValidatePrivatePractitionerResponseType;

/**
 * Created by eriklupander on 2015-06-16.
 */
public interface PrivatePractitionerServiceClient {

    ValidatePrivatePractitionerResponseType validatePrivatePractitioner(String personId, String logicalAddress);

    GetPrivatePractitionerResponseType getPrivatePractitioner(String personId, String logicalAddress);
}

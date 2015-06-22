package se.inera.intyg.webcert.integration.pp.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitioner.v1.rivtabp21.GetPrivatePractitionerResponderInterface;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerresponder.v1.GetPrivatePractitionerResponseType;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerresponder.v1.GetPrivatePractitionerType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;

/**
 * Created by Magnus Ekstrand on 18/06/15.
 */
public class PPServiceImpl implements PPService {

    private static final Logger LOG = LoggerFactory.getLogger(PPServiceImpl.class);

    @Autowired
    private GetPrivatePractitionerResponderInterface service;

    @Override
    public HoSPersonType getPrivatePractitioner(String logicalAddress, String personnummer) {

        GetPrivatePractitionerType request = new GetPrivatePractitionerType();
        request.setPersonalIdentityNumber(personnummer);

        GetPrivatePractitionerResponseType response = service.getPrivatePractitioner(logicalAddress, request);
        return response.getHoSPerson();
    }

}

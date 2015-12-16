package se.inera.intyg.webcert.integration.pp.services;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitioner.v1.rivtabp21.GetPrivatePractitionerResponderInterface;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerresponder.v1.GetPrivatePractitionerResponseType;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerresponder.v1.GetPrivatePractitionerType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.ResultCodeEnum;
import se.riv.infrastructure.directory.privatepractitioner.validateprivatepractitioner.v1.rivtabp21.ValidatePrivatePractitionerResponderInterface;
import se.riv.infrastructure.directory.privatepractitioner.validateprivatepractitionerresponder.v1.ValidatePrivatePractitionerResponseType;
import se.riv.infrastructure.directory.privatepractitioner.validateprivatepractitionerresponder.v1.ValidatePrivatePractitionerType;

/**
 * Interfaces with the get and validate private practitioner services.
 *
 * Created by Magnus Ekstrand on 18/06/15.
 */
public class PPServiceImpl implements PPService {

    private static final Logger LOG = LoggerFactory.getLogger(PPServiceImpl.class);

    @Autowired
    private GetPrivatePractitionerResponderInterface getPrivatePractitionerService;

    @Autowired
    private ValidatePrivatePractitionerResponderInterface validatePrivatePractitionerService;

    @Override
    public HoSPersonType getPrivatePractitioner(String logicalAddress, String hsaIdentityNumber, String personalIdentityNumber) {

        LOG.debug("Getting person information from Privatläkarportalen.");

        // Exakt ett av fälten hsaIdentityNumber och personalIdentityNumber ska anges.
        validateIdentifier(hsaIdentityNumber, personalIdentityNumber);

        GetPrivatePractitionerType request = createPrivatePractitionerType(hsaIdentityNumber, personalIdentityNumber);
        return getPrivatePractitioner(logicalAddress, request);
    }


    @Override
    public boolean validatePrivatePractitioner(String logicalAddress, String hsaIdentityNumber, String personalIdentityNumber) {
        LOG.debug("Validating person information from Privatläkarportalen.");
        validateIdentifier(hsaIdentityNumber, personalIdentityNumber);


        ValidatePrivatePractitionerType request = createValidatePrivatePractitionerType(hsaIdentityNumber, personalIdentityNumber);
        return validatePrivatePractitioner(logicalAddress, request);
    }

    private boolean validatePrivatePractitioner(String logicalAddress, ValidatePrivatePractitionerType parameters) {
        try {
            ValidatePrivatePractitionerResponseType response = validatePrivatePractitionerService.validatePrivatePractitioner(logicalAddress, parameters);

            if (response.getResultCode() == ResultCodeEnum.ERROR) {
                LOG.error(response.getResultText());
            }

            if (response.getResultCode() == ResultCodeEnum.INFO) {
                LOG.info(response.getResultText());
            }

            return response.getResultCode() == ResultCodeEnum.OK;

        } catch (SOAPFaultException e) {
            throw new WebServiceException(e);
        }
    }

    private void validateIdentifier(String hsaIdentityNumber, String personalIdentityNumber) {
        // Exakt ett av fälten hsaIdentityNumber och personalIdentityNumber ska anges.
        if (StringUtils.isEmpty(hsaIdentityNumber) && StringUtils.isEmpty(personalIdentityNumber)) {
            throw new IllegalArgumentException("Inget av argumenten hsaIdentityNumber och personalIdentityNumber är satt. Ett av dem måste ha ett värde.");
        }

        if (!StringUtils.isEmpty(hsaIdentityNumber) && !StringUtils.isEmpty(personalIdentityNumber)) {
            throw new IllegalArgumentException("Endast ett av argumenten hsaIdentityNumber och personalIdentityNumber får vara satt.");
        }
    }

    private HoSPersonType getPrivatePractitioner(String logicalAddress, GetPrivatePractitionerType parameters) throws WebServiceException {

        try {
            GetPrivatePractitionerResponseType response = getPrivatePractitionerService.getPrivatePractitioner(logicalAddress, parameters);

            if (response.getHoSPerson() == null) {
                LOG.info("No private practitioner with the requested identifiers exist.");
                return null;
            }

            if (response.getResultCode() == ResultCodeEnum.ERROR) {
                LOG.error(response.getResultText());
            }

            if (response.getResultCode() == ResultCodeEnum.INFO) {
                LOG.info(response.getResultText());
            }

            return response.getHoSPerson();

        } catch (SOAPFaultException e) {
            throw new WebServiceException(e);
        }
    }

    private GetPrivatePractitionerType createPrivatePractitionerType(String hsaIdentityNumber, String personalIdentityNumber) {
        GetPrivatePractitionerType ppType = new GetPrivatePractitionerType();
        ppType.setPersonHsaId(hsaIdentityNumber);
        ppType.setPersonalIdentityNumber(personalIdentityNumber);

        return ppType;
    }

    private ValidatePrivatePractitionerType createValidatePrivatePractitionerType(String hsaIdentityNumber, String personalIdentityNumber) {
        ValidatePrivatePractitionerType ppType = new ValidatePrivatePractitionerType();
        ppType.setPersonHsaId(hsaIdentityNumber);
        ppType.setPersonalIdentityNumber(personalIdentityNumber);

        return ppType;
    }
}

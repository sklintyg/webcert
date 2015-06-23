package se.inera.intyg.webcert.integration.pp.services;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import se.inera.certificate.common.util.StringUtil;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitioner.v1.rivtabp21.GetPrivatePractitionerResponderInterface;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerresponder.v1.GetPrivatePractitionerResponseType;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerresponder.v1.GetPrivatePractitionerType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.ResultCodeEnum;

/**
 * Created by Magnus Ekstrand on 18/06/15.
 */
public class PPServiceImpl implements PPService {

    private static final Logger LOG = LoggerFactory.getLogger(PPServiceImpl.class);

    @Autowired
    private GetPrivatePractitionerResponderInterface service;

    @Override
    public HoSPersonType getPrivatePractitioner(String logicalAddress, String hsaIdentityNumber, String personalIdentityNumber) {

        LOG.debug("Getting person information from Privatläkarportalen '{}'", personalIdentityNumber);

        // Exakt ett av fälten hsaIdentityNumber och personalIdentityNumber ska anges.
        if (StringUtils.isEmpty(hsaIdentityNumber) && StringUtils.isEmpty(personalIdentityNumber)) {
            throw new IllegalArgumentException("Inget av argumenten hsaIdentityNumber och personalIdentityNumber är satt. Ett av dem måste ha ett värde.");
        }

        if (!StringUtils.isEmpty(hsaIdentityNumber) && !StringUtils.isEmpty(personalIdentityNumber)) {
            throw new IllegalArgumentException("Endast ett av argumenten hsaIdentityNumber och personalIdentityNumber får vara satt.");
        }

        GetPrivatePractitionerType request = createPrivatePractitionerType(hsaIdentityNumber, personalIdentityNumber);
        return getPrivatePractitioner(logicalAddress, request);
    }

    private HoSPersonType getPrivatePractitioner(String logicalAddress, GetPrivatePractitionerType parameters) throws WebServiceException {

        try {
            GetPrivatePractitionerResponseType response = service.getPrivatePractitioner(logicalAddress, parameters);

            if (response.getHoSPerson() == null) {
                String msg = StringUtils.isEmpty(parameters.getPersonHsaId())
                        ? "personal identity number: " + parameters.getPersonalIdentityNumber()
                        : "HSA identity number : " + parameters.getPersonHsaId();

                LOG.info("No private practitioner with {} exists.", msg);
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


}

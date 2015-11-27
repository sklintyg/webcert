package se.inera.intyg.webcert.integration.pp.stub;

import java.util.ArrayList;
import java.util.List;

import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import se.inera.intyg.common.util.logging.HashUtility;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitioner.v1.rivtabp21.GetPrivatePractitionerResponderInterface;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerresponder.v1.GetPrivatePractitionerResponseType;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerresponder.v1.GetPrivatePractitionerType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.ResultCodeEnum;

/**
 * Stubbed responder for get private practitioner.
 *
 * Created by Magnus Ekstrand on 18/06/15.
 */
public class GetPrivatePractitionerResponderStub implements GetPrivatePractitionerResponderInterface {

    public static final String PERSONNUMMER_EXISTING = "19121212-1212";
    public static final String PERSONNUMMER_NONEXISTING = "19121212-7169";
    public static final String PERSONNUMMER_ERROR_RESPONSE = "19121212-XXXX";
    public static final String PERSONNUMMER_THROW_EXCEPTION = "19121212-ZZZZ";

    @Autowired
    private HoSPersonStub personStub;

    @Override
    public GetPrivatePractitionerResponseType getPrivatePractitioner(String logicalAddress, GetPrivatePractitionerType parameters) {

        // Do validation of parameters object
        validate(parameters);

        String id = parameters.getPersonalIdentityNumber();
        String hsa = parameters.getPersonHsaId();
        if (id != null && !id.isEmpty()) {
            return getGetPrivatePractitionerResponseTypeForPnr(parameters);
        } else if (hsa != null && !hsa.isEmpty()) {
            return getGetPrivatePractitionerResponseTypeForHsaid(parameters);
        } else {
            throw new IllegalArgumentException("Inget av argumenten hsaId och personId är satt. Ett av dem måste ha ett värde.");
        }
    }

    private GetPrivatePractitionerResponseType getGetPrivatePractitionerResponseTypeForHsaid(GetPrivatePractitionerType parameters) {
        String hsa = parameters.getPersonHsaId();

        GetPrivatePractitionerResponseType response = new GetPrivatePractitionerResponseType();

        // if matching -- create error response
        if (PERSONNUMMER_ERROR_RESPONSE.equals(hsa)) {
            response.setHoSPerson(null);
            response.setResultCode(ResultCodeEnum.ERROR);
            response.setResultText("FAILURE: an error occured while trying to get private practitioner with hsa id: " + hsa + " exists.");
            return response;
        }

        // if matching -- throw exception
        if (PERSONNUMMER_THROW_EXCEPTION.equals(hsa)) {
            throw new SOAPFaultException(createSOAPFault());
        }

        HoSPersonType person = personStub.getByHsaId(hsa);

        if (person == null) {
            response.setResultCode(ResultCodeEnum.INFO);
            response.setResultText("No private practitioner with hsa id: " + hsa + " exists.");
        } else {
            response.setHoSPerson(person);
            response.setResultCode(ResultCodeEnum.OK);
        }
        return response;

    }

    private GetPrivatePractitionerResponseType getGetPrivatePractitionerResponseTypeForPnr(GetPrivatePractitionerType parameters) {
        String id = parameters.getPersonalIdentityNumber();

        GetPrivatePractitionerResponseType response = new GetPrivatePractitionerResponseType();

        // if matching -- create error response
        if (PERSONNUMMER_ERROR_RESPONSE.equals(id)) {
            response.setHoSPerson(null);
            response.setResultCode(ResultCodeEnum.ERROR);
            response.setResultText("FAILURE: an error occured while trying to get private practitioner with personal identity number: " + HashUtility.hash(id) + " exists.");
            return response;
        }

        // if matching -- throw exception
        if (PERSONNUMMER_THROW_EXCEPTION.equals(id)) {
            throw new SOAPFaultException(createSOAPFault());
        }

        HoSPersonType person = personStub.get(id);

        if (person == null) {
            response.setResultCode(ResultCodeEnum.INFO);
            response.setResultText("No private practitioner with personal identity number: " + HashUtility.hash(id) + " exists.");
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

    private SOAPFault createSOAPFault()  {
        SOAPFault soapFault;

        try {
            soapFault = SOAPFactory.newInstance().createFault();
            soapFault.setFaultString("Response was of unexpected content type.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return soapFault;
    }

}

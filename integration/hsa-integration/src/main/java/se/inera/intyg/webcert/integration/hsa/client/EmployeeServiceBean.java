package se.inera.intyg.webcert.integration.hsa.client;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import se.inera.intyg.common.support.common.util.StringUtil;
import se.riv.infrastructure.directory.employee.getemployeeincludingprotectedperson.v1.rivtabp21.GetEmployeeIncludingProtectedPersonResponderInterface;
import se.riv.infrastructure.directory.employee.getemployeeincludingprotectedpersonresponder.v1.GetEmployeeIncludingProtectedPersonResponseType;
import se.riv.infrastructure.directory.employee.getemployeeincludingprotectedpersonresponder.v1.GetEmployeeIncludingProtectedPersonType;
import se.riv.infrastructure.directory.v1.ResultCodeEnum;

/**
 * Created by eriklupander on 2015-12-03.
 */
@Service
public class EmployeeServiceBean implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceBean.class);

    @Autowired
    private GetEmployeeIncludingProtectedPersonResponderInterface getEmployeeIncludingProtectedPersonResponderInterface;

    @Value("${infrastructure.directory.employee.logicalAddress}")
    private String logicalAddress;

    @Override
    public GetEmployeeIncludingProtectedPersonResponseType getEmployee(String personHsaId, String personalIdentityNumber, String searchBase) throws WebServiceException {

        LOG.debug("Getting info from HSA for person '{}'", personHsaId);

        // Exakt ett av fälten personHsaId och personalIdentityNumber ska anges.
        if (StringUtil.isNullOrEmpty(personHsaId) && StringUtil.isNullOrEmpty(personalIdentityNumber)) {
            throw new IllegalArgumentException("Inget av argumenten personHsaId och personalIdentityNumber är satt. Ett av dem måste ha ett värde.");
        }

        if (!StringUtil.isNullOrEmpty(personHsaId) && !StringUtil.isNullOrEmpty(personalIdentityNumber)) {
            throw new IllegalArgumentException("Endast ett av argumenten personHsaId och personalIdentityNumber får vara satt.");
        }

        GetEmployeeIncludingProtectedPersonType employeeType = createEmployeeType(personHsaId, personalIdentityNumber, searchBase);
        return getEmployee(logicalAddress, employeeType);
    }

    private GetEmployeeIncludingProtectedPersonResponseType getEmployee(String logicalAddress, GetEmployeeIncludingProtectedPersonType employeeType) throws WebServiceException {

        GetEmployeeIncludingProtectedPersonResponseType response;

        try {
            response = getEmployeeIncludingProtectedPersonResponderInterface.getEmployeeIncludingProtectedPerson(logicalAddress, employeeType);

            // check whether call was successful or not
            if (response.getResultCode() != ResultCodeEnum.OK) {
                String personHsaId = employeeType.getPersonHsaId();
                LOG.error("Failed getting employee information from HSA; personHsaId = '{}'", personHsaId);
                throw new WebServiceException(response.getResultText());
            }
        } catch (SOAPFaultException e) {
            throw new WebServiceException(e);
        }

        return response;
    }


    private GetEmployeeIncludingProtectedPersonType createEmployeeType(String personHsaId, String personalIdentityNumber, String searchBase) {
        GetEmployeeIncludingProtectedPersonType employeeType = new GetEmployeeIncludingProtectedPersonType();
        employeeType.setPersonHsaId(personHsaId);
        employeeType.setPersonalIdentityNumber(personalIdentityNumber);
        employeeType.setSearchBase(searchBase);

        return employeeType;
    }
}

package se.inera.webcert.hsa.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.riv.infrastructure.directory.employee.getemployee.v1.rivtabp21.GetEmployeeResponderInterface;
import se.riv.infrastructure.directory.employee.getemployeeresponder.v1.GetEmployeeResponseType;
import se.riv.infrastructure.directory.employee.getemployeeresponder.v1.GetEmployeeType;
import se.riv.infrastructure.directory.v1.ResultCodeEnum;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;

/**
 * Created by Magnus Ekstrand on 28/05/15.
 */
public class GetEmployeeServiceImpl implements GetEmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(GetEmployeeServiceImpl.class);

    @Autowired
    private GetEmployeeResponderInterface service;

    @Override
    public GetEmployeeResponseType getEmployee(String logicalAddress, String personHsaId, String personalIdentityNumber) throws WebServiceException {
        return getEmployee(logicalAddress, personHsaId, personalIdentityNumber, null);
    }

    @Override
    public GetEmployeeResponseType getEmployee(String logicalAddress, String personHsaId, String personalIdentityNumber, String searchBase) throws WebServiceException {
        // Exakt ett av fälten personHsaId och personalIdentityNumber ska anges.
        if (personHsaId == null && personalIdentityNumber == null) {
            throw new IllegalArgumentException("Ett av argumenten personHsaId och personalIdentityNumber måste vara satt");
        }

        GetEmployeeType employeeType = createEmployeeType(personHsaId, personalIdentityNumber, searchBase);
        return getEmployee(logicalAddress, employeeType);
    }

    protected GetEmployeeResponseType getEmployee(String logicalAddress, GetEmployeeType employeeType) throws WebServiceException {

        GetEmployeeResponseType response;

        // Check in-params

        try {
            response = service.getEmployee(logicalAddress, employeeType);

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

    private GetEmployeeType createEmployeeType(String personHsaId, String personalIdentityNumber, String searchBase) {
        GetEmployeeType employeeType = new GetEmployeeType();
        employeeType.setPersonHsaId(personHsaId);
        employeeType.setPersonalIdentityNumber(personalIdentityNumber);
        employeeType.setSearchBase(searchBase);

        return employeeType;
    }

}

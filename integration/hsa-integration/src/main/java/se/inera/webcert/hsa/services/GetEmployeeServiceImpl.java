package se.inera.webcert.hsa.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.common.support.common.util.StringUtil;
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

        LOG.debug("Getting info from HSA for person '{}'", personHsaId);

        // Exakt ett av fälten personHsaId och personalIdentityNumber ska anges.
        if (StringUtil.isNullOrEmpty(personHsaId) && StringUtil.isNullOrEmpty(personalIdentityNumber)) {
            throw new IllegalArgumentException("Inget av argumenten personHsaId och personalIdentityNumber är satt. Ett av dem måste ha ett värde.");
        }

        if (!StringUtil.isNullOrEmpty(personHsaId) && !StringUtil.isNullOrEmpty(personalIdentityNumber)) {
            throw new IllegalArgumentException("Endast ett av argumenten personHsaId och personalIdentityNumber får vara satt.");
        }

        GetEmployeeType employeeType = createEmployeeType(personHsaId, personalIdentityNumber, searchBase);
        return getEmployee(logicalAddress, employeeType);
    }

    protected GetEmployeeResponseType getEmployee(String logicalAddress, GetEmployeeType employeeType) throws WebServiceException {

        GetEmployeeResponseType response;

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

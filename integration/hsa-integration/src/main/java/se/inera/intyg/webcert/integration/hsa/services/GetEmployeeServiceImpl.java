package se.inera.intyg.webcert.integration.hsa.services;

import javax.xml.ws.WebServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.intyg.webcert.integration.hsa.client.EmployeeService;
import se.riv.infrastructure.directory.employee.getemployeeincludingprotectedpersonresponder.v1.GetEmployeeIncludingProtectedPersonResponseType;

/**
 * Created by Magnus Ekstrand on 28/05/15.
 */
@Service
public class GetEmployeeServiceImpl implements GetEmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(GetEmployeeServiceImpl.class);

    @Autowired
    private EmployeeService employeeService;

    @Override
    public GetEmployeeIncludingProtectedPersonResponseType getEmployee(String personHsaId, String personalIdentityNumber) throws WebServiceException {
        return employeeService.getEmployee(personHsaId, personalIdentityNumber, null);
    }

    @Override
    public GetEmployeeIncludingProtectedPersonResponseType getEmployee(String personHsaId, String personalIdentityNumber, String searchBase) throws WebServiceException {
        return employeeService.getEmployee(personHsaId, personalIdentityNumber, searchBase);
    }


}

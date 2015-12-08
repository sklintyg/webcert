package se.inera.intyg.webcert.integration.hsa.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.util.StringUtil;
import se.inera.intyg.webcert.integration.hsa.client.EmployeeService;
import se.riv.infrastructure.directory.employee.getemployee.v1.rivtabp21.GetEmployeeResponderInterface;
import se.riv.infrastructure.directory.employee.getemployeeincludingprotectedpersonresponder.v1.GetEmployeeIncludingProtectedPersonResponseType;
import se.riv.infrastructure.directory.employee.getemployeeresponder.v1.GetEmployeeResponseType;
import se.riv.infrastructure.directory.employee.getemployeeresponder.v1.GetEmployeeType;
import se.riv.infrastructure.directory.v1.ResultCodeEnum;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;

/**
 * Created by Magnus Ekstrand on 28/05/15.
 */
@Service
public class GetEmployeeServiceImpl implements GetEmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(GetEmployeeServiceImpl.class);

    @Autowired
    EmployeeService employeeService;

    @Override
    public GetEmployeeIncludingProtectedPersonResponseType getEmployee(String personHsaId, String personalIdentityNumber) throws WebServiceException {
        return employeeService.getEmployee(personHsaId, personalIdentityNumber, null);
    }

    @Override
    public GetEmployeeIncludingProtectedPersonResponseType getEmployee(String personHsaId, String personalIdentityNumber, String searchBase) throws WebServiceException {
        return employeeService.getEmployee(personHsaId, personalIdentityNumber, searchBase);
    }


}

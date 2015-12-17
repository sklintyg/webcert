package se.inera.intyg.webcert.integration.hsa.client;

import javax.xml.ws.WebServiceException;

import se.riv.infrastructure.directory.employee.getemployeeincludingprotectedpersonresponder.v1.GetEmployeeIncludingProtectedPersonResponseType;

/**
 * Created by eriklupander on 2015-12-03.
 */
public interface EmployeeService {
    GetEmployeeIncludingProtectedPersonResponseType getEmployee(String personHsaId, String personalIdentityNumber, String searchBase) throws WebServiceException;
}

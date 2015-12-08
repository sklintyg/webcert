package se.inera.intyg.webcert.integration.hsa.services;

import javax.xml.ws.WebServiceException;

import se.riv.infrastructure.directory.employee.getemployeeincludingprotectedpersonresponder.v1.GetEmployeeIncludingProtectedPersonResponseType;
import se.riv.infrastructure.directory.employee.getemployeeresponder.v1.GetEmployeeResponseType;

/**
 * Created by Magnus Ekstrand on 27/05/15.
 */
public interface GetEmployeeService {

    /**
     * Returnerar information, som kontaktinformation samt legitimerad yrkesgrupp och specialitet, för sökt person.
     * Exakt ett av fälten personHsaId och personalIdentityNumber ska anges.
     *
     * @param personHsaId Sökt persons HSA-id.
     * @param personalIdentityNumber Sökt persons Person-id (personnummer eller samordningsnummer).
     *
     * @return Information om sökt person.
     *
     * @throws WebServiceException
     */
    GetEmployeeIncludingProtectedPersonResponseType getEmployee(String personHsaId, String personalIdentityNumber) throws WebServiceException;

    /**
     * Returnerar information, som kontaktinformation samt legitimerad yrkesgrupp och specialitet, för sökt person.
     * Exakt ett av fälten personHsaId och personalIdentityNumber ska anges.
     *
     * @param personHsaId Sökt persons HSA-id.
     * @param personalIdentityNumber Sökt persons Person-id (personnummer eller samordningsnummer).
     * @param searchBase Sökbas. Om ingen sökbas anges används c=SE som sökbas.
     *
     * @return Information om sökt person.
     *
     * @throws WebServiceException
     */
    GetEmployeeIncludingProtectedPersonResponseType getEmployee(String personHsaId, String personalIdentityNumber, String searchBase) throws WebServiceException;

}

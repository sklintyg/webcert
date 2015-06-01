package se.inera.webcert.hsa.services;

import se.riv.infrastructure.directory.employee.getemployeeresponder.v1.GetEmployeeResponseType;
import se.riv.infrastructure.directory.employee.getemployeeresponder.v1.GetEmployeeType;

import javax.xml.ws.WebServiceException;

/**
 * Created by Magnus Ekstrand on 27/05/15.
 */
public interface GetEmployeeService {

    /**
     * Returnerar information, som kontaktinformation samt legitimerad yrkesgrupp och specialitet, för sökt person.
     * Exakt ett av fälten personHsaId och personalIdentityNumber ska anges.
     *
     * @param logicalAddress Mottagande systems logiska adress.
     * @param personHsaId Sökt persons HSA-id.
     * @param personalIdentityNumber Sökt persons Person-id (personnummer eller samordningsnummer).
     *
     * @return Information om sökt person.
     *
     * @throws WebServiceException
     */
    GetEmployeeResponseType getEmployee(String logicalAddress, String personHsaId, String personalIdentityNumber) throws WebServiceException;
    /**
     * Returnerar information, som kontaktinformation samt legitimerad yrkesgrupp och specialitet, för sökt person.
     * Exakt ett av fälten personHsaId och personalIdentityNumber ska anges.
     *
     * @param logicalAddress Mottagande systems logiska adress.
     * @param personHsaId Sökt persons HSA-id.
     * @param personalIdentityNumber Sökt persons Person-id (personnummer eller samordningsnummer).
     * @param searchBase Sökbas. Om ingen sökbas anges används c=SE som sökbas.
     *
     * @return Information om sökt person.
     *
     * @throws WebServiceException
     */
    GetEmployeeResponseType getEmployee(String logicalAddress, String personHsaId, String personalIdentityNumber, String searchBase) throws WebServiceException;

}

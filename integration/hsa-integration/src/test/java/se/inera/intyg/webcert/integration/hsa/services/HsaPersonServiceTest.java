package se.inera.intyg.webcert.integration.hsa.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.webcert.integration.hsa.client.AuthorizationManagementService;
import se.inera.intyg.webcert.integration.hsa.client.EmployeeService;
import se.riv.infrastructure.directory.employee.getemployeeincludingprotectedpersonresponder.v1.GetEmployeeIncludingProtectedPersonResponseType;
import se.riv.infrastructure.directory.v1.PersonInformationType;
import se.riv.infrastructure.directory.v1.ResultCodeEnum;

@RunWith(MockitoJUnitRunner.class)
public class HsaPersonServiceTest {

    private static final String VALID_HSA_ID = "SE11837399";
    private static final String INVALID_HSA_ID = "SE88888888";

    @Mock
    private EmployeeService employeeService;

    @Mock
    private AuthorizationManagementService authorizationManagementService;

    @InjectMocks
    private HsaPersonServiceImpl hsaPersonService;

    @Before
    public void setupExpectations() {

//        PersonInformationType validParams = new PersonInformationType();
//        validParams.setPersonHsaId(VALID_HSA_ID);
//
//        PersonInformationType invalidParams = new PersonInformationType();
//        invalidParams.setPersonHsaId(INVALID_HSA_ID);

        GetEmployeeIncludingProtectedPersonResponseType response = buildResponse();
        when(employeeService.getEmployee(VALID_HSA_ID, null, null)).thenReturn(response);

        GetEmployeeIncludingProtectedPersonResponseType emptyResponse = new GetEmployeeIncludingProtectedPersonResponseType();
        when(employeeService.getEmployee(INVALID_HSA_ID, null, null)).thenReturn(emptyResponse);
    }

    @Test
    public void testGetHsaPersonInfoWithValidPerson() {

        List<PersonInformationType> res = hsaPersonService.getHsaPersonInfo(VALID_HSA_ID);

        assertNotNull(res);
        assertFalse(res.isEmpty());
    }

    @Test
    public void testGetHsaPersonInfoWithInvalidPerson() {

        List<PersonInformationType> res = hsaPersonService.getHsaPersonInfo(INVALID_HSA_ID);

        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    private GetEmployeeIncludingProtectedPersonResponseType buildResponse() {

        PersonInformationType userType = buildUserType(VALID_HSA_ID, "Henry", "Jekyl");

        GetEmployeeIncludingProtectedPersonResponseType response = new GetEmployeeIncludingProtectedPersonResponseType();
        response.getPersonInformation().add(userType);
        response.setResultCode(ResultCodeEnum.OK);
        return response;
    }

    private PersonInformationType buildUserType(String hsaId, String fName, String lName) {

        PersonInformationType type = new PersonInformationType();
        type.setPersonHsaId(hsaId);
        type.setGivenName(fName);
        type.setMiddleAndSurName(lName);
        type.setMail(fName.concat(".").concat(lName).concat("@mailinator.com"));

        type.getSpecialityCode().addAll(Arrays.asList("100", "200", "300"));
        type.getSpecialityName().addAll(Arrays.asList("Kirurgi", "Psykiatri", "Ortopedi"));

        return type;
    }

}

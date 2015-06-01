package se.inera.webcert.hsa.services;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import se.riv.infrastructure.directory.employee.getemployeeresponder.v1.GetEmployeeResponseType;
import se.riv.infrastructure.directory.v1.ResultCodeEnum;

/**
 * Created by Magnus Ekstrand on 01/06/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class GetEmployeeServiceTest {

    private static final String LOGICAL_ADDRESS = "1234567890";
    private static final String VALID_HSA_ID = "SE11837399";
    private static final String INVALID_HSA_ID = "SE88888888";

    @InjectMocks
    GetEmployeeServiceImpl service;

    @Before
    public void setupExpectations() {

        GetEmployeeResponseType response = buildResponse();
        when(service.getEmployee(any(String.class), any(String.class), any(String.class))).thenReturn(response);

    }

    @Test
    public void testGetEmployeeWithHsaId() {

        GetEmployeeResponseType response = service.getEmployee(LOGICAL_ADDRESS, VALID_HSA_ID, null);

        assertNotNull(response.getResultCode() == ResultCodeEnum.OK);
    }

    private GetEmployeeResponseType buildResponse() {
        return new GetEmployeeResponseType();
    }

}
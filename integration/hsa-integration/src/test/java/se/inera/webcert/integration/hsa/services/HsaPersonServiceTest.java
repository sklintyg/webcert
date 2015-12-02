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

import se.inera.ifv.hsawsresponder.v3.GetHsaPersonHsaUserType;
import se.inera.ifv.hsawsresponder.v3.GetHsaPersonHsaUserType.SpecialityCodes;
import se.inera.ifv.hsawsresponder.v3.GetHsaPersonHsaUserType.SpecialityNames;
import se.inera.ifv.hsawsresponder.v3.GetHsaPersonResponseType;
import se.inera.ifv.hsawsresponder.v3.GetHsaPersonResponseType.UserInformations;
import se.inera.ifv.hsawsresponder.v3.GetHsaPersonType;
import se.inera.intyg.webcert.integration.hsa.ifv.webcert.spi.authorization.impl.HSAWebServiceCalls;

@RunWith(MockitoJUnitRunner.class)
public class HsaPersonServiceTest {

    private static final String VALID_HSA_ID = "SE11837399";
    private static final String INVALID_HSA_ID = "SE88888888";

    @Mock
    private HSAWebServiceCalls hsaWebServiceCalls;

    @InjectMocks
    private HsaPersonServiceImpl hsaPersonService;

    @Before
    public void setupExpectations() {

        GetHsaPersonType validParams = new GetHsaPersonType();
        validParams.setHsaIdentity(VALID_HSA_ID);

        GetHsaPersonType invalidParams = new GetHsaPersonType();
        invalidParams.setHsaIdentity(INVALID_HSA_ID);

        GetHsaPersonResponseType response = buildResponse();
        when(hsaWebServiceCalls.callGetHsaPerson(validParams)).thenReturn(response);

        GetHsaPersonResponseType emptyResponse = new GetHsaPersonResponseType();
        emptyResponse.setUserInformations(new UserInformations());
        when(hsaWebServiceCalls.callGetHsaPerson(invalidParams)).thenReturn(emptyResponse);
    }

    @Test
    public void testGetHsaPersonInfoWithValidPerson() {

        List<GetHsaPersonHsaUserType> res = hsaPersonService.getHsaPersonInfo(VALID_HSA_ID);

        assertNotNull(res);
        assertFalse(res.isEmpty());
    }

    @Test
    public void testGetHsaPersonInfoWithInvalidPerson() {

        List<GetHsaPersonHsaUserType> res = hsaPersonService.getHsaPersonInfo(INVALID_HSA_ID);

        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    private GetHsaPersonResponseType buildResponse() {

        GetHsaPersonHsaUserType userType = buildUserType(VALID_HSA_ID, "Henry", "Jekyl");

        UserInformations userInfo = new UserInformations();
        userInfo.getUserInformation().add(userType);

        GetHsaPersonResponseType response = new GetHsaPersonResponseType();
        response.setUserInformations(userInfo);

        return response;
    }

    private GetHsaPersonHsaUserType buildUserType(String hsaId, String fName, String lName) {

        GetHsaPersonHsaUserType type = new GetHsaPersonHsaUserType();
        type.setHsaIdentity(hsaId);
        type.setGivenName(fName);
        type.setSn(lName);
        type.setMail(fName.concat(".").concat(lName).concat("@mailinator.com"));

        SpecialityCodes specCodes = new SpecialityCodes();
        specCodes.getSpecialityCode().addAll(Arrays.asList("100", "200", "300"));
        type.setSpecialityCodes(specCodes);

        SpecialityNames specNames = new SpecialityNames();
        specNames.getSpecialityName().addAll(Arrays.asList("Kirurgi", "Psykiatri", "Ortopedi"));
        type.setSpecialityNames(specNames);

        return type;
    }

}

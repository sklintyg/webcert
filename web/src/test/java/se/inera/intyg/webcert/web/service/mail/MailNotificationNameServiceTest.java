package se.inera.intyg.webcert.web.service.mail;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import jakarta.xml.ws.WebServiceException;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaEmployeeService;

@ExtendWith(MockitoExtension.class)
class MailNotificationNameServiceTest {

    @Mock
    private HsaEmployeeService hsaEmployeeService;

    @InjectMocks
    private MailNotificationNameService employeeNameService;

    @Test
    void shallReturnNameIfEmployeeExists() {
        final var personInformation = new PersonInformation();
        personInformation.setGivenName("givenName");
        personInformation.setMiddleAndSurName("middleAnd surName");

        doReturn(Collections.singletonList(personInformation)).when(hsaEmployeeService).getEmployee(any(), any(), any());

        final var actualName = employeeNameService.getEmployeeHsaName("employeeId");

        assertEquals("givenName middleAnd surName", actualName);
    }

    @Test
    void shallReturnHsaIdAsNameIfEmployeeEmpty() {
        doReturn(Collections.emptyList()).when(hsaEmployeeService).getEmployee(any(), any(), any());

        final var actualName = employeeNameService.getEmployeeHsaName("employeeId");

        assertEquals("employeeId", actualName);
    }

    @Test
    void shallReturnHsaIdAsNameIfExceptionIsThrown() {
        doThrow(new WebServiceException("Something went wrong")).when(hsaEmployeeService).getEmployee(any(), any(), any());

        final var actualName = employeeNameService.getEmployeeHsaName("employeeId");

        assertEquals("employeeId", actualName);
    }
}
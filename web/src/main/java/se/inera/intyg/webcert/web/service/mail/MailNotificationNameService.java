package se.inera.intyg.webcert.web.service.mail;

import jakarta.xml.ws.WebServiceException;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaEmployeeService;

@Service
@RequiredArgsConstructor
public class MailNotificationNameService {

  private final HsaEmployeeService hsaEmployeeService;

  public String getEmployeeHsaName(String employeeHsaId) {
    final var employeeInfo = getEmployee(employeeHsaId);
    if (isEmpty(employeeInfo)) {
      return employeeHsaId;
    }
    return getName(employeeInfo);
  }

  private List<PersonInformation> getEmployee(String employeeHsaId) {
    try {
      return hsaEmployeeService.getEmployee(employeeHsaId, null, null);
    } catch (WebServiceException e) {
      return Collections.emptyList();
    }
  }

  private boolean isEmpty(List<PersonInformation> employeeInfo) {
    return employeeInfo == null || employeeInfo.isEmpty();
  }

  private String getName(List<PersonInformation> employeeInfo) {
    return "%s %s".formatted(
        employeeInfo.getFirst().getGivenName(),
        employeeInfo.getFirst().getMiddleAndSurName()
    );
  }
}
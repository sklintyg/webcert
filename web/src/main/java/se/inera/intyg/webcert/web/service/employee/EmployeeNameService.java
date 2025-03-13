package se.inera.intyg.webcert.web.service.employee;

import jakarta.xml.ws.WebServiceException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaEmployeeService;

@Service
@RequiredArgsConstructor
public class EmployeeNameService {

    private final HsaEmployeeService hsaEmployeeService;

    public String getEmployeeHsaName(String employeeHsaId) {
        final var employeeInfo = getEmployee(employeeHsaId);
        if (isEmpty(employeeInfo)) {
            return employeeHsaId;
        }

        return getName(employeeInfo).orElse(employeeHsaId);
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

    private Optional<String> getName(List<PersonInformation> employeeInfo) {
        return employeeInfo.stream()
            .filter(info -> isDefined(info.getMiddleAndSurName()))
            .map(this::buildName)
            .findFirst();
    }

    private String buildName(PersonInformation personInformation) {
        return isDefined(personInformation.getGivenName())
            ? "%s %s".formatted(
            personInformation.getGivenName(),
            personInformation.getMiddleAndSurName())
            : personInformation.getMiddleAndSurName();
    }

    private boolean isDefined(String value) {
        return value != null && !value.isBlank();
    }
}
package se.inera.intyg.webcert.web.privatepractitioner;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.web.service.facade.GetUserResourceLinks;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Component
@RequiredArgsConstructor
public class PrivatePractitionerAccessValidationHelper {

    private final GetUserResourceLinks getUserResourceLinks;

    public boolean hasAccessToRegister(WebCertUser user) {
        return Arrays.stream(getUserResourceLinks.get(user))
            .anyMatch(link -> (link.getType().equals(ResourceLinkTypeDTO.ACCESS_REGISTER_PRIVATE_PRACTITIONER) && link.isEnabled()));
    }

    public boolean hasAccessToUpdate(WebCertUser user) {
        return Arrays.stream(getUserResourceLinks.get(user))
            .anyMatch(link -> (link.getType().equals(ResourceLinkTypeDTO.ACCESS_EDIT_PRIVATE_PRACTITIONER) && link.isEnabled()));
    }
}

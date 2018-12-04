package se.inera.intyg.webcert.web.service.auth;

import org.springframework.stereotype.Component;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Component
public class WcAuthorityValidator implements AuthorityValidator {

    private static final String AUTH_MSG = "User missing required privilege or cannot handle sekretessmarkerad patient";
    private static final String PU_MSG = "Could not fetch sekretesstatus for patient from PU service";

    private final AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();

    private final WebCertUserService webCertUserService;
    private final PatientDetailsResolver patientDetailsResolver;

    public WcAuthorityValidator(final WebCertUserService webCertUserService, final PatientDetailsResolver patientDetailsResolver) {
        this.webCertUserService = webCertUserService;
        this.patientDetailsResolver = patientDetailsResolver;
    }

    @Override
    public void assertIsAuthorized(final Personnummer personnummer, final String authority) {

        final WebCertUser user = webCertUserService.getUser();

        authoritiesValidator.given(user)
                .privilege(authority)
                .orThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM_SEKRETESSMARKERING, AUTH_MSG));

        final SekretessStatus sekretessStatus = patientDetailsResolver.getSekretessStatus(personnummer);

        if (sekretessStatus == SekretessStatus.UNDEFINED) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM, PU_MSG);
        }

        if (sekretessStatus == SekretessStatus.TRUE) {
            authoritiesValidator.given(user)
                    .privilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT)
                    .orThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM_SEKRETESSMARKERING, AUTH_MSG));
        }
    }
}

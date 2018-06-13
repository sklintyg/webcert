package se.inera.intyg.webcert.web.web.controller.api;

import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.secmaker.netid.nias.v1.Sign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;

import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.xmldsig.service.FakeSignatureServiceImpl;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.SignaturStateDTO;

@Path("/signature")
public class SignatureApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(SignatureApiController.class);

    private static final String LAST_SAVED_DRAFT = "lastSavedDraft";

    @Autowired
    private UnderskriftService underskriftService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired(required = false)
    private FakeSignatureServiceImpl fakeSignatureService;

    @POST
    @Path("/{intygsTyp}/{intygsId}/{version}/signeringshash")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public SignaturStateDTO signeraUtkast(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
                                          @PathParam("version") long version) {

        LOG.info("ENTER - signeraUtkast");

        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp).features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
                .orThrow();

        try {
            SignaturBiljett sb = underskriftService.startSigningProcess(intygsId, intygsTyp, version);
            return SignaturStateDTO.SignaturStateDTOBuilder.aSignaturStateDTO()
                    .withTicketId(sb.getTicketId())
                    .withIntygsId(sb.getIntygsId())
                    .withStatus(sb.getStatus())
                    .withVersion(sb.getVersion())
                    .withSignableDigest(sb.getIntygSignature().getSigningData()) // This is what you stuff into NetiD SIGN.
                    .build();
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            monitoringLogService.logUtkastConcurrentlyEdited(intygsId, intygsTyp);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION, e.getMessage());
        }
    }


    /**
     * Signera utkast. Endast fejkinloggning.
     *
     * FLYTTA TILL EGEN BEAN som är !prod annoterad!!!!
     *
     * @param intygsId
     *            intyg id
     * @return SignaturTicketResponse
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/{version}/fejksignera/{ticketId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public SignaturStateDTO fejkSigneraUtkast(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
                                                    @PathParam("version") long version, @PathParam("ticketId") String ticketId, @Context HttpServletRequest request) {

        // Start by doing an extra server-side check of FAKE authentication.
        WebCertUser user = getWebCertUserService().getUser();
        if (user.getAuthenticationMethod() != AuthenticationMethod.FAKE) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "Fake signing is only allowed for users logged in by FAKE AuthenticationMethod.");
        }
        verifyIsAuthorizedToSignIntyg(intygsTyp);

        request.getSession(true).removeAttribute(LAST_SAVED_DRAFT);

        SignaturBiljett sb = underskriftService.fakeSignature(intygsId, intygsTyp, version, ticketId);

        return SignaturStateDTO.SignaturStateDTOBuilder.aSignaturStateDTO()
                .withTicketId(sb.getTicketId())
                .withIntygsId(sb.getIntygsId())
                .withStatus(sb.getStatus())
                .withVersion(sb.getVersion())
                .withSignableDigest(sb.getIntygSignature().getSigningData()) // This is what you stuff into NetiD SIGN.
                .build();
    }


    private void verifyIsAuthorizedToSignIntyg(String intygsTyp) {
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SIGNERA_INTYG)
                .orThrow();
    }
}

/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.service.underskrift;

import jakarta.persistence.OptimisticLockException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.EventCode;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.event.CertificateEventService;
import se.inera.intyg.webcert.web.service.access.AccessResult;
import se.inera.intyg.webcert.web.service.access.DraftAccessService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.factory.LogRequestFactory;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.underskrift.fake.FakeUnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.grp.GrpSignatureService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;
import se.inera.intyg.webcert.web.service.underskrift.validator.DraftModelToXmlValidator;
import se.inera.intyg.webcert.web.service.underskrift.xmldsig.UtkastModelToXMLConverter;
import se.inera.intyg.webcert.web.service.underskrift.xmldsig.XmlUnderskriftServiceImpl;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.util.access.AccessResultExceptionHelper;

@Service("signServiceForWC")
public class UnderskriftServiceImpl implements UnderskriftService {

    private static final Logger LOG = LoggerFactory.getLogger(UnderskriftServiceImpl.class);

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private GrpSignatureService grpUnderskriftService;

    @Autowired
    private XmlUnderskriftServiceImpl xmlUnderskriftService;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private CertificateEventService certificateEventService;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired(required = false)
    private FakeUnderskriftService fakeUnderskriftService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LogService logService;

    @Autowired
    private LogRequestFactory logRequestFactory;

    @Autowired
    private IntygService intygService;

    @Autowired
    private RedisTicketTracker redisTicketTracker;

    @Autowired
    private DraftAccessService draftAccessService;

    @Autowired
    private AccessResultExceptionHelper accessResultExceptionHelper;

    @Autowired
    private DraftModelToXmlValidator draftModelToXMLValidator;

    @Autowired
    private UtkastModelToXMLConverter utkastModelToXMLConverter;

    @Autowired
    private PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;

    @Autowired
    private CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;

    @Override
    public SignaturBiljett startSigningProcess(String intygsId, String intygsTyp, long version, SignMethod signMethod,
        String ticketId, String userIpAddress) {
        WebCertUser user = webCertUserService.getUser();

        // Check if Utkast is eligible for signing right now, if so get it.
        Utkast utkast = getUtkastForSignering(intygsId, version, user);

        validateXml(utkast);

        // Update JSON with current user as vardperson
        String updatedJson = updateJsonWithVardpersonAndSigneringsTid(utkast, user);

        // Determine which method to use for creating the SignaturBiljett that contains the payload to sign etc.
        SignaturBiljett signaturBiljett = null;
        switch (user.getAuthenticationMethod()) {
            case SITHS:
            case NET_ID:
            case FAKE:
                final var registerCertificateXml = utkastModelToXMLConverter.utkastToXml(updatedJson, intygsTyp);
                signaturBiljett = xmlUnderskriftService
                    .skapaSigneringsBiljettMedDigest(intygsId, intygsTyp, version, Optional.of(updatedJson), signMethod, ticketId,
                        userIpAddress, registerCertificateXml);
                break;
            case BANK_ID:
            case MOBILT_BANK_ID:
                signaturBiljett = grpUnderskriftService
                    .skapaSigneringsBiljettMedDigest(intygsId, intygsTyp, version, Optional.of(updatedJson), signMethod, ticketId,
                        userIpAddress, null);
                break;
        }

        if (signaturBiljett == null) {
            throw new IllegalStateException("Unhandled authentication method, could not create SignaturBiljett");
        }

        if (signaturBiljett.getSignMethod() == SignMethod.GRP) {
            grpUnderskriftService.startGrpCollectPoller(user.getPersonId(), signaturBiljett);
        }

        return signaturBiljett;
    }

    private void validateXml(Utkast draft) {
        try {
            var validationResponse = draftModelToXMLValidator.validateDraftModelAsXml(draft);
            draftModelToXMLValidator.assertResponse(draft.getIntygsId(), validationResponse);
        } catch (ModuleNotFoundException | ModuleException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e);
        }
    }

    /**
     * Called through the /api/signature endpoint when finalizing a FakeSignature.
     */
    @Override
    public SignaturBiljett fakeSignature(String intygsId, String intygsTyp, long version, String ticketId) {
        WebCertUser user = webCertUserService.getUser();
        Utkast utkast = getUtkastForSignering(intygsId, version, user);

        SignaturBiljett signaturBiljett = fakeUnderskriftService.finalizeFakeSignature(ticketId, utkast, user);

        finalizeSignature(utkast, user);
        return signaturBiljett;
    }

    /**
     * Called either when:
     * - the /api/signature endpoint when the NetiD plugin has signed the Base64-encoded SignedInfo XML.
     */
    @Override
    public SignaturBiljett netidSignature(String biljettId, byte[] signatur, String certifikat) {
        SignaturBiljett signaturBiljett = redisTicketTracker.findBiljett(biljettId);

        // Highly unlikely, but if the redis cache have crashed in between or similar, we need to handle that the there's
        // no SignaturBiljett to be had.
        if (signaturBiljett == null) {
            String errMsg = "No SignaturBiljett found for ticketId '{}' when finalizing signature. "
                + "Has Redis evicted the ticket early or has Redis crashed during the signature process?";
            LOG.error(errMsg, biljettId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, errMsg);
        }

        WebCertUser user = webCertUserService.getUser();
        Utkast utkast = getUtkastForSignering(signaturBiljett.getIntygsId(), signaturBiljett.getVersion(), user);

        SignaturBiljett finishedSignaturBiljett = xmlUnderskriftService.finalizeSignature(signaturBiljett, signatur,
            certifikat, utkast, user);
        finalizeSignature(utkast, user);
        return finishedSignaturBiljett;
    }

    @Override
    public SignaturBiljett grpSignature(String biljettId, byte[] signatur) {
        SignaturBiljett sb = redisTicketTracker.findBiljett(biljettId);

        WebCertUser user = webCertUserService.getUser();
        Utkast utkast = getUtkastForSignering(sb.getIntygsId(), sb.getVersion(), user);

        sb = grpUnderskriftService.finalizeSignature(sb, signatur, null, utkast, user);
        finalizeSignature(utkast, user);
        return sb;
    }

    @Override
    public SignaturBiljett signeringsStatus(String ticketId) {
        SignaturBiljett sb = redisTicketTracker.findBiljett(ticketId);
        if (sb == null) {
            LOG.error("No SignaturBiljett found for ticketId '{}'", ticketId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                "No SignaturBiljett found for ticketId '" + ticketId + "'");
        }
        return sb;
    }

    private void finalizeSignature(Utkast utkast, WebCertUser user) {
        // Notify stakeholders when certificate has been signed
        notificationService.sendNotificationForDraftSigned(utkast);

        publishCertificateAnalyticsMessage.publishEvent(
            certificateAnalyticsMessageFactory.certificateSigned(utkast)
        );

        certificateEventService.createCertificateEvent(utkast.getIntygsId(), user.getHsaId(), EventCode.SIGNAT,
            String.format("Certificate type: %s", utkast.getIntygsTyp()));

        LogRequest logRequest = logRequestFactory.createLogRequestFromUtkast(utkast);

        // Note that we explictly supplies the WebCertUser here.
        logService.logSignIntyg(logRequest, logService.getLogUser(user));

        // Sends intyg to Intygstjänsten.
        intygService.handleAfterSigned(utkast);
    }

    /**
     * Makes sure the specified Utkast is ready for signing, and then returns it. If Utkast is not ready for signing,
     * a WebCertServiceException is thrown.
     *
     * @param intygId id of utkast to be signed
     * @param version used to detect concurrent modification
     * @param user the user that is signing the utkast
     * @return the specified Utkast iff it's ready to be signed
     */
    private Utkast getUtkastForSignering(String intygId, long version, WebCertUser user) {
        Utkast utkast = utkastRepository.findById(intygId).orElse(null);

        if (utkast == null) {
            LOG.warn("Utkast '{}' was not found", intygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                "Internal error signing utkast, the utkast '" + intygId
                    + "' could not be found");
        }

        verifyAccessToSignDraft(utkast);

        if (utkast.getVersion() != version) {
            LOG.debug("Utkast '{}' was concurrently modified", intygId);
            throw new OptimisticLockException(utkast.getSenastSparadAv().getNamn());
        } else if (utkast.getStatus() != UtkastStatus.DRAFT_COMPLETE) {
            LOG.warn("Utkast '{}' med status '{}' kunde inte signeras. Måste vara i status {}", intygId, utkast.getStatus(),
                UtkastStatus.DRAFT_COMPLETE);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                "Internal error signing utkast, the utkast '" + intygId
                    + "' was not in state " + UtkastStatus.DRAFT_COMPLETE);
        }

        return utkast;
    }

    private String updateJsonWithVardpersonAndSigneringsTid(Utkast utkast, WebCertUser user) {
        LocalDateTime signeringstid = LocalDateTime.now();

        try {
            // VardpersonReferens vardpersonReferens = UpdateUserUtil.createVardpersonFromWebCertUser(user);
            ModuleApi moduleApi = moduleRegistry.getModuleApi(utkast.getIntygsTyp(), utkast.getIntygTypeVersion());
            Vardenhet vardenhetFromJson = moduleApi.getUtlatandeFromJson(utkast.getModel()).getGrundData().getSkapadAv().getVardenhet();
            return moduleApi
                .updateBeforeSigning(utkast.getModel(), IntygConverterUtil.buildHosPersonalFromWebCertUser(user, vardenhetFromJson),
                    signeringstid);

        } catch (ModuleNotFoundException | IOException | ModuleException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                "Unable to sign certificate: " + e.getMessage());
        }
    }

    private void verifyAccessToSignDraft(Utkast utkast) {
        final AccessResult accessResult = draftAccessService.allowToSignDraft(
            utkast.getIntygsTyp(),
            utkast.getIntygTypeVersion(),
            getVardenhet(utkast),
            utkast.getPatientPersonnummer(),
            utkast.getIntygsId());

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private Vardenhet getVardenhet(Utkast utkast) {
        final Vardgivare vardgivare = new Vardgivare();
        vardgivare.setVardgivarid(utkast.getVardgivarId());

        final Vardenhet vardenhet = new Vardenhet();
        vardenhet.setEnhetsid(utkast.getEnhetsId());
        vardenhet.setVardgivare(vardgivare);

        return vardenhet;
    }
}

package se.inera.intyg.webcert.web.service.underskrift;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.service.underskrift.grp.GrpUnderskriftServiceImpl;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.xmldsig.XmlUnderskriftServiceImpl;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.util.UpdateUserUtil;

import javax.persistence.OptimisticLockException;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class UnderskriftServiceImpl implements UnderskriftService {

    private static final Logger LOG = LoggerFactory.getLogger(UnderskriftServiceImpl.class);

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private GrpUnderskriftServiceImpl grpUnderskriftService;

    @Autowired
    private XmlUnderskriftServiceImpl xmlUnderskriftService;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Override
    public SignaturBiljett startSigningProcess(String intygsId, String intygsTyp, long version) {
        WebCertUser user = webCertUserService.getUser();

        // Check if Utkast is eligible for signing right now, if so get it.
        Utkast utkast = getUtkastForSignering(intygsId, version, user);

        // Update JSON with current user as vardperson
        String updatedJson = updateJsonWithVardpersonAndSigneringsTid(utkast, user);

        // Determine which method to use for this signing

        switch (user.getAuthenticationMethod()) {
        case SITHS:
        case NET_ID:
        case EFOS:
        case FAKE:
            return xmlUnderskriftService.startSigningProcess(intygsId, intygsTyp, version);
        case BANK_ID:
        case MOBILT_BANK_ID:
            return grpUnderskriftService.startSigningProcess(intygsId, intygsTyp, version);
        }
        throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM,
                "Unhandled user state for signing: " + user.getAuthenticationMethod());
    }






    private Utkast getUtkastForSignering(String intygId, long version, WebCertUser user) {
        Utkast utkast = utkastRepository.findOne(intygId);

        if (utkast == null) {
            LOG.warn("Utkast '{}' was not found", intygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                    "Internal error signing utkast, the utkast '" + intygId
                            + "' could not be found");
        }

        if (!user.getIdsOfAllVardenheter().contains(utkast.getEnhetsId())) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User does not have privileges to sign utkast '" + intygId + "'");
        } else if (utkast.getVersion() != version) {
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
            VardpersonReferens vardpersonReferens = UpdateUserUtil.createVardpersonFromWebCertUser(user);
            ModuleApi moduleApi = moduleRegistry.getModuleApi(utkast.getIntygsTyp());
            Vardenhet vardenhetFromJson = moduleApi.getUtlatandeFromJson(utkast.getModel()).getGrundData().getSkapadAv().getVardenhet();
            return moduleApi
                    .updateBeforeSigning(utkast.getModel(), IntygConverterUtil.buildHosPersonalFromWebCertUser(user, vardenhetFromJson),
                            signeringstid);


        } catch (ModuleNotFoundException | IOException | ModuleException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                    "Unable to sign certificate: " + e.getMessage());
        }
    }
}

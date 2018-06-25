/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

public abstract class BaseSignatureService {

    private static final Logger LOG = LoggerFactory.getLogger(BaseSignatureService.class);

    @Autowired
    protected UtkastRepository utkastRepository;

    @Autowired
    protected IntygModuleRegistry moduleRegistry;

    @Autowired
    protected IntygService intygService;

    @Autowired
    protected RedisTicketTracker redisTicketTracker;

    protected Utkast updateAndSaveUtkast(Utkast utkast, String payloadJson, Signatur signatur, WebCertUser user) {
        utkast.setSenastSparadAv(new VardpersonReferens(user.getHsaId(), user.getNamn()));

        // Write the JSON to the final utkast model. We've re-digested it above so we're sure it was what was signed.
        utkast.setModel(payloadJson);
        utkast.setSignatur(signatur);
        utkast.setStatus(UtkastStatus.SIGNED);

        // Persist utkast with added signature
        return utkastRepository.save(utkast);
    }

    protected void checkVersion(Utkast utkast, SignaturBiljett biljett) {
        if (utkast.getVersion() != biljett.getVersion()) {
            LOG.error(
                    "Signing of utkast '{}' failed since the version on the utkast ({}) differs from when the signing was initialized ({})",
                    utkast.getIntygsId(), utkast.getVersion(), biljett.getVersion());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION,
                    "Cannot complete signing, Utkast version differs from signature ticket version.");
        }
    }

    protected void checkDigests(Utkast utkast, String computedHash, String signatureHash) {
        if (!computedHash.equals(signatureHash)) {
            LOG.error("Signing of utkast '{}' failed since the payload has been modified since signing was initialized",
                    utkast.getIntygsId());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "Internal error signing utkast, the payload of utkast "
                            + utkast.getIntygsId() + " has been modified since signing was initialized");
        }
    }

    protected void checkIntysId(Utkast utkast, SignaturBiljett biljett) {
        if (!biljett.getIntygsId().equals(utkast.getIntygsId())) {
            LOG.error(
                    "Signing of utkast '{}' failed since the intygsId ({}) on the Utkast is different from the one "
                            + "on the signing operation ({})",
                    utkast.getIntygsId(), biljett.getIntygsId());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "Internal error signing utkast, the intygsId of utkast "
                            + utkast.getIntygsId() + " has been modified since signing was initialized");
        }
    }
}

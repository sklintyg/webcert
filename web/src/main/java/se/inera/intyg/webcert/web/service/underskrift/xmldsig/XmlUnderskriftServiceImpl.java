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
package se.inera.intyg.webcert.web.service.underskrift.xmldsig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.infra.xmldsig.model.IntygXMLDSignature;
import se.inera.intyg.infra.xmldsig.service.PrepareSignatureService;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.underskrift.BaseXMLSignatureService;
import se.inera.intyg.webcert.web.service.underskrift.CommonUnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class XmlUnderskriftServiceImpl extends BaseXMLSignatureService implements CommonUnderskriftService {

    @Autowired
    private UtkastModelToXMLConverter utkastModelToXMLConverter;

    @Autowired
    private PrepareSignatureService prepareSignatureService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Override
    public SignaturBiljett skapaSigneringsBiljettMedDigest(String intygsId, String intygsTyp, long version, String utkastJson) {
        String registerCertificateXml = utkastModelToXMLConverter.utkastToXml(utkastJson, intygsTyp);
        IntygXMLDSignature intygSignature = prepareSignatureService.prepareSignature(registerCertificateXml, intygsId);
        intygSignature.setIntygJson(utkastJson);

        SignaturBiljett biljett = SignaturBiljett.SignaturBiljettBuilder.aSignaturBiljett(UUID.randomUUID().toString(), SignaturTyp.XMLDSIG)
                .withIntygsId(intygsId)
                .withVersion(version)
                .withIntygSignature(intygSignature)
                .withStatus(SignaturStatus.BEARBETAR)
                .withSkapad(LocalDateTime.now())
                .withHash(intygSignature.getSignedInfoForSigning())
                .build();

        redisTicketTracker.trackBiljett(biljett);
        return biljett;
    }

    @Override
    public SignaturBiljett finalizeSignature(final SignaturBiljett biljett, final byte[] signatur, final String certifikat,
            final Utkast utkast, WebCertUser user) {
        SignaturBiljett sb = finalizeXMLDSigSignature(certifikat, user, biljett, signatur, utkast);
        monitoringLogService.logIntygSigned(utkast.getIntygsId(), utkast.getIntygsTyp(), user.getHsaId(), user.getAuthenticationScheme(),
                utkast.getRelationKod());

        return redisTicketTracker.updateStatus(sb.getTicketId(), sb.getStatus());
    }
}

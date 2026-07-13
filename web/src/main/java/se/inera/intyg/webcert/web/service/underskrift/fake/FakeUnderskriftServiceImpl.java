/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.underskrift.fake;

import java.nio.charset.Charset;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.infra.xmldsig.service.FakeSignatureService;
import se.inera.intyg.webcert.infra.xmldsig.service.PrepareSignatureService;
import se.inera.intyg.webcert.infra.xmldsig.service.XMLDSigService;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.csintegration.certificate.SignCertificateService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.underskrift.BaseXMLSignatureService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;
import se.inera.intyg.webcert.web.service.underskrift.xmldsig.UtkastModelToXMLConverter;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Service
@Profile({"!prod"})
public class FakeUnderskriftServiceImpl extends BaseXMLSignatureService
    implements FakeUnderskriftService {

  private final FakeSignatureService fakeSignatureService;

  private final MonitoringLogService monitoringLogService;

  public FakeUnderskriftServiceImpl(
      UtkastRepository utkastRepository,
      IntygModuleRegistry moduleRegistry,
      IntygService intygService,
      RedisTicketTracker redisTicketTracker,
      UtkastModelToXMLConverter utkastModelToXMLConverter,
      PrepareSignatureService prepareSignatureService,
      SignCertificateService signCertificateService,
      XMLDSigService xmldSigService,
      FakeSignatureService fakeSignatureService,
      MonitoringLogService monitoringLogService) {
    super(
        utkastRepository,
        moduleRegistry,
        intygService,
        redisTicketTracker,
        utkastModelToXMLConverter,
        prepareSignatureService,
        signCertificateService,
        xmldSigService);
    this.fakeSignatureService = fakeSignatureService;
    this.monitoringLogService = monitoringLogService;
  }

  @Override
  public SignaturBiljett finalizeFakeSignature(String ticketId, Utkast utkast, WebCertUser user) {

    SignaturBiljett biljett = redisTicketTracker.findBiljett(ticketId);
    if (biljett == null) {
      throw new RuntimeException("No biljett found in Redis for " + ticketId);
    }

    // We fake a signature here so stuff can validate.

    // Encode the <SignedInfo>...</SignedInfo> into a Base64 string.
    String base64EncodedSignedInfoXml =
        Base64.getEncoder()
            .encodeToString(
                biljett.getIntygSignature().getSigningData().getBytes(Charset.forName("UTF-8")));
    String fakeSignatureData = fakeSignatureService.createSignature(base64EncodedSignedInfoXml);

    monitoringLogService.logIntygSigned(
        utkast.getIntygsId(),
        utkast.getIntygsTyp(),
        user.getHsaId(),
        user.getAuthenticationScheme(),
        utkast.getRelationKod());

    // Pull the X509 from the keystore used for fake signing.
    X509Certificate x509Certificate = fakeSignatureService.getX509Certificate();

    try {
      biljett =
          finalizeXMLDSigSignature(
              Base64.getEncoder().encodeToString(x509Certificate.getEncoded()),
              user,
              biljett,
              Base64.getDecoder().decode(fakeSignatureData),
              utkast);
      return redisTicketTracker.updateStatus(biljett.getTicketId(), biljett.getStatus());
    } catch (CertificateEncodingException e) {
      throw new WebCertServiceException(
          WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e.getMessage());
    }
  }
}

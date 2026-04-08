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
package se.inera.intyg.webcert.infra.xmldsig.service;

import java.util.Map;
import org.w3._2000._09.xmldsig_.KeyInfoType;
import se.inera.intyg.webcert.infra.xmldsig.model.CertificateInfo;
import se.inera.intyg.webcert.infra.xmldsig.model.ValidationResponse;

public interface XMLDSigService {

  KeyInfoType buildKeyInfoForCertificate(String certificate);

  ValidationResponse validateSignatureValidity(String signatureXml, boolean checkReferences);

  Map<String, ValidationResponse> validateSignatureValidityMulti(
      String signatureXml, boolean checkReferences);

  Map<String, CertificateInfo> extractCertificateInfo(String signatureXml);
}

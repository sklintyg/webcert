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

package se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v3;

import se.inera.intyg.common.support.integration.converter.util.ResultTypeUtil;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.CreateDraftCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;

public class CreateDraftCertificateResponseFactory {

    private CreateDraftCertificateResponseFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static CreateDraftCertificateResponseType createErrorResponse(String errorMsg, ErrorIdType errorType) {
        final var result = ResultTypeUtil.errorResult(errorType, errorMsg);
        final var response = new CreateDraftCertificateResponseType();
        response.setResult(result);
        return response;
    }

    public static CreateDraftCertificateResponseType createSuccessResponse(String certificateId, String invokingUnitHsaId) {
        final var intygId = new IntygId();
        intygId.setRoot(invokingUnitHsaId);
        intygId.setExtension(certificateId);

        final var response = new CreateDraftCertificateResponseType();
        response.setResult(ResultTypeUtil.okResult());
        response.setIntygsId(intygId);
        return response;
    }
}

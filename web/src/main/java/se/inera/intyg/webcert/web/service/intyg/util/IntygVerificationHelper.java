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
package se.inera.intyg.webcert.web.service.intyg.util;

import static java.util.Objects.isNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.invoke.MethodHandles;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.intyg.IntygServiceImpl;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;

public final class IntygVerificationHelper {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private IntygVerificationHelper() {

    }

    public static void verifyIsSigned(final IntygContentHolder intyg, final IntygServiceImpl.IntygOperation operation) {

        final List<Status> states = (isNull(intyg) || isEmpty(intyg.getStatuses()))
                ? Lists.newArrayList()
                : intyg.getStatuses();

        if (states.stream().noneMatch(status -> CertificateState.RECEIVED.equals(status.getType()) && status.getTimestamp() != null)) {
            final String message = MessageFormat.format("Certificate {0} is not signed, cannot {1} an unsigned certificate",
                    intyg.getUtlatande().getId(), operation.getValue());
            LOG.debug(message);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, message);
        }
    }

    public static void verifyIsSigned(final Utkast intyg, final IntygServiceImpl.IntygOperation operation) {

        if (intyg.getSignatur() == null
                || intyg.getSignatur().getSigneringsDatum() == null
                || !Objects.equals(intyg.getStatus(), UtkastStatus.SIGNED)) {
            final String message = MessageFormat.format("Certificate {0} is not signed, cannot {1} an unsigned certificate",
                    intyg.getIntygsId(), operation.getValue());
            LOG.debug(message);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, message);
        }
    }

    public static void verifyIsSigned(final CertificateResponse certificate, final IntygServiceImpl.IntygOperation operation) {

        if (certificate.getUtlatande().getSignature() == null) {

            final List<Status> states = (isNull(certificate.getMetaData()) || isEmpty(certificate.getMetaData().getStatus())
                    ? Collections.emptyList()
                    : certificate.getMetaData().getStatus());

            if (states.stream().noneMatch(state -> CertificateState.RECEIVED.equals(state.getType()) && state.getTimestamp() != null)) {

                final String message = MessageFormat.format("Certificate {0} is not signed, cannot {1} an unsigned certificate",
                        certificate.getUtlatande().getId(), operation.getValue());

                LOG.debug(message);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, message);
            }
        }
    }

    public static void verifyIsNotRevoked(final IntygContentHolder intyg, final IntygServiceImpl.IntygOperation operation) {
        if (intyg.isRevoked()) {

            final String message = MessageFormat.format("certificate with id '{0}' is revoked, cannot {1} a revoked certificate",
                    intyg.getUtlatande().getId(), operation.getValue());

            LOG.debug(message);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, message);
        }
    }

    public static void verifyIsNotRevoked(final Utkast intyg, final IntygServiceImpl.IntygOperation operation) {
        if (intyg.getAterkalladDatum() != null) {

            final String message = MessageFormat.format("certificate with id '{0}' is revoked, cannot {1} a revoked certificate",
                    intyg.getIntygsId(), operation.getValue());

            LOG.debug(message);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, message);
        }
    }

    public static void verifyIsNotRevoked(final CertificateResponse certificate, final IntygServiceImpl.IntygOperation operation) {
        if (certificate.isRevoked()) {

            final String message = MessageFormat.format("certificate with id '{0}' is revoked, cannot {1} a revoked certificate",
                    certificate.getUtlatande().getId(), operation.getValue());

            LOG.debug(message);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, message);
        }


        final List<Status> states = (isNull(certificate.getMetaData()) || isEmpty(certificate.getMetaData().getStatus())
                ? Collections.emptyList()
                : certificate.getMetaData().getStatus());

        if (states.stream().anyMatch(state -> CertificateState.CANCELLED.equals(state.getType()) && state.getTimestamp() != null)) {
            final String message = MessageFormat.format("certificate with id '{0}' is revoked, cannot {1} a revoked certificate",
                    certificate.getUtlatande().getId(), operation.getValue());

            LOG.debug(message);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, message);
        }
    }
}

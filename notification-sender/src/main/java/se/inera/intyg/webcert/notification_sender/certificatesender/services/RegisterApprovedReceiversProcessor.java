/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.notification_sender.certificatesender.services;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.ws.WebServiceException;
import java.io.IOException;
import java.util.List;
import org.apache.camel.Body;
import org.apache.camel.Header;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.inera.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.inera.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.ReceiverApprovalStatus;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversType;
import se.inera.intyg.webcert.common.Constants;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.inera.intyg.webcert.logging.MdcLogConstants;

public class RegisterApprovedReceiversProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterApprovedReceiversProcessor.class);

    @Autowired
    private RegisterApprovedReceiversResponderInterface registerApprovedReceiversClient;
    @Autowired
    private MdcHelper mdcHelper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void process(@Body String jsonBody, @Header(Constants.INTYGS_ID) String intygsId, @Header(Constants.INTYGS_TYP) String intygsTyp,
        @Header(Constants.LOGICAL_ADDRESS) String logicalAddress) throws TemporaryException {

        List<ReceiverApprovalStatus> receiverIds = transformMessageBodyToReceiverList(jsonBody);

        try {
            MDC.put(MdcLogConstants.TRACE_ID_KEY, mdcHelper.traceId());
            MDC.put(MdcLogConstants.SPAN_ID_KEY, mdcHelper.spanId());
            MDC.put(MdcLogConstants.EVENT_CERTIFICATE_ID, intygsId);
            MDC.put(MdcLogConstants.EVENT_CERTIFICATE_TYPE, intygsTyp);
            MDC.put(MdcLogConstants.EVENT_LOGICAL_ADDRESS, logicalAddress);

            checkArgument(StringUtils.isNotEmpty(intygsId), "Message of type %s does not have a %s header.",
                Constants.REGISTER_APPROVED_RECEIVERS_MESSAGE,
                Constants.INTYGS_ID);
            checkArgument(StringUtils.isNotEmpty(intygsTyp), "Message of type %s does not have a %s header.",
                Constants.REGISTER_APPROVED_RECEIVERS_MESSAGE,
                Constants.INTYGS_TYP);

            RegisterApprovedReceiversType req = new RegisterApprovedReceiversType();
            IntygId intygId = new IntygId();
            intygId.setExtension(intygsId);
            req.setIntygId(intygId);

            TypAvIntyg typAvIntyg = new TypAvIntyg();
            typAvIntyg.setCode(intygsTyp);
            req.setTypAvIntyg(typAvIntyg);

            req.getApprovedReceivers().addAll(receiverIds);

            RegisterApprovedReceiversResponseType responseType = registerApprovedReceiversClient.registerApprovedReceivers(logicalAddress,
                req);

            // Any problems on the other end that yields an ERROR result are treated as PermanentExceptions.
            if (responseType.getResult().getResultCode() == ResultCodeType.ERROR) {
                throw new TemporaryException(responseType.getResult().getResultText());
            }
        } catch (IllegalArgumentException e) {
            LOG.error("RegisterApprovedReceiversProcessor message processing failed due to IllegalArgumentException, message: {}",
                e.getMessage());
            throw new TemporaryException(e.getMessage());
        } catch (WebServiceException e) {
            LOG.error("Call to RegisterApprovedReceivers for intyg {} caused an error: {}. Will retry.",
                intygsId, e.getMessage());
            throw new TemporaryException(e.getMessage());
        } finally {
            MDC.clear();
        }
    }

    private List<ReceiverApprovalStatus> transformMessageBodyToReceiverList(@Body String jsonBody) throws TemporaryException {
        try {
            return objectMapper.readValue(jsonBody, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new TemporaryException("Could not parse message body into list of approved receivers.");
        }
    }
}

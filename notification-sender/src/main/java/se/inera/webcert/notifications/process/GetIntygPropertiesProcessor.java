package se.inera.webcert.notifications.process;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.notifications.message.v1.HandelseType;
import se.inera.webcert.notifications.routes.RouteHeaders;
import se.inera.webcert.notifications.service.WebcertRepositoryService;
import se.inera.webcert.persistence.intyg.model.Utkast;

public class GetIntygPropertiesProcessor implements Processor {

    @Autowired
    private WebcertRepositoryService webcertRepositoryService;

    private static final Logger LOG = LoggerFactory.getLogger(GetIntygPropertiesProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {

        String intygsId = exchange.getIn().getHeader(RouteHeaders.INTYGS_ID, String.class);
        String logiskAddress = exchange.getIn().getHeader(RouteHeaders.LOGISK_ADRESS, String.class);
        String handelseStr = exchange.getIn().getHeader(RouteHeaders.HANDELSE, String.class);
        HandelseType handelse = convertToHandelseType(handelseStr);

        if (HandelseType.INTYGSUTKAST_RADERAT.equals(handelse)) {
            LOG.debug("Event is {}, do not fetch Intyg {} from database since it is already deleted", handelse, intygsId);

            // TODO; null check logisk adress, throw exception if no present
            return;
        }

        Utkast utkast = webcertRepositoryService.getUtkast(intygsId);

        if (utkast == null) {
            LOG.debug("Utkast '{}' not found in database!", intygsId);
            exchange.getIn().setHeader(RouteHeaders.SAKNAS_I_DB, "SAKNAS_I_DB");
            return;
        }

        String status = utkast.getStatus().toString();

        LOG.debug("Utkast '{}' has status {}", intygsId, status);
        exchange.getIn().setHeader(RouteHeaders.INTYGS_STATUS, status);

        // If logical address header is null, use enhetsId from the entity as logical address
        if (StringUtils.isBlank(logiskAddress)) {
            logiskAddress = utkast.getEnhetsId();
            LOG.debug("Logisk adress not set, using logisk adress '{}' for Utkast '{}'", logiskAddress, intygsId);
            exchange.getIn().setHeader(RouteHeaders.LOGISK_ADRESS, logiskAddress);
        }

        if (!webcertRepositoryService.isVardenhetIntegrerad(logiskAddress)) {
            LOG.debug("Unit '{}' on Utkast '{}' is NOT integrated", logiskAddress, intygsId);
            exchange.getIn().setHeader(RouteHeaders.INTEGRERAD_ENHET, Boolean.FALSE);
        } else {
            exchange.getIn().setHeader(RouteHeaders.INTEGRERAD_ENHET, Boolean.TRUE);
        }

    }

    private HandelseType convertToHandelseType(String handelseStr) {
        return HandelseType.fromValue(handelseStr);
    }

}

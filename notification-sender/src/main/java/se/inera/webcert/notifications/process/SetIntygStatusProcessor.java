package se.inera.webcert.notifications.process;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.notifications.routes.RouteHeaders;
import se.inera.webcert.notifications.service.WebcertRepositoryService;

public class SetIntygStatusProcessor implements Processor {

    @Autowired
    private WebcertRepositoryService webcertRepositoryService;

    private static final Logger LOG = LoggerFactory.getLogger(SetIntygStatusProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        String intygsId = exchange.getIn().getHeader(RouteHeaders.INTYGS_ID, String.class);

        String status = webcertRepositoryService.getIntygsUtkastStatus(intygsId).toString();

        LOG.debug("Set status: {} on intygsID: {} ", status, intygsId);
        exchange.getIn().setHeader(RouteHeaders.INTYGS_STATUS, status);
    }

}

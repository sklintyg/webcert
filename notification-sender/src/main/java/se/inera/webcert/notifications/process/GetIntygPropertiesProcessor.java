package se.inera.webcert.notifications.process;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.notifications.routes.RouteHeaders;
import se.inera.webcert.notifications.service.WebcertRepositoryService;
import se.inera.webcert.persistence.intyg.model.Intyg;

public class GetIntygPropertiesProcessor implements Processor {

    @Autowired
    private WebcertRepositoryService webcertRepositoryService;

    private static final Logger LOG = LoggerFactory.getLogger(GetIntygPropertiesProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        String intygsId = exchange.getIn().getHeader(RouteHeaders.INTYGS_ID, String.class);

        Intyg intyg = webcertRepositoryService.getIntygsUtkast(intygsId);

        if (intyg == null) {
            LOG.debug("Intyg with intygsID: {} not found in database!", intygsId);
            exchange.getIn().setHeader(RouteHeaders.SAKNAS_I_DB, "SAKNAS_I_DB");
            return;
        }

        String status = intyg.getStatus().toString();
        
        LOG.debug("Set status: {} on intygsID: {} ", status, intygsId);
        exchange.getIn().setHeader(RouteHeaders.INTYGS_STATUS, status);

        //Use enhetsId from the entity as logical address
        String logiskAdress = intyg.getEnhetsId();
        LOG.debug("Set logisk adress: {} on intygsID: {} ", logiskAdress, intygsId);
        exchange.getIn().setHeader(RouteHeaders.LOGISK_ADRESS, logiskAdress);
    }

}

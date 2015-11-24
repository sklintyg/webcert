package se.inera.intyg.webcert.notification_sender.certificatesender.services.mock;

import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.spi.RouteContext;

/**
 * Created by eriklupander on 2015-06-05.
 */
public class MockTransactionPolicy implements org.apache.camel.spi.Policy {
    @Override
    public void beforeWrap(RouteContext routeContext, ProcessorDefinition<?> processorDefinition) {

    }

    @Override
    public Processor wrap(RouteContext routeContext, Processor processor) {
        return null;
    }
}

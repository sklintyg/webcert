/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.logsender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * @author andreaskaltenbach
 */
@Component
public class LogSenderDeleteMe {

    private static final Logger LOG = LoggerFactory.getLogger(LogSenderDeleteMe.class);

//    @Value("${loggtjanst.logicalAddress}")
//    private String logicalAddress;
//
//    @Value("${logsender.bulkSize}")
//    private int bulkSize;
//
//    @Autowired
//    private StoreLogResponderInterface loggTjanstResponder;
//
//    @Autowired
//    @Qualifier("jmsTemplate")
//    private JmsTemplate jmsTemplate;
//
//    @Autowired
//    @Qualifier("nonTransactedJmsTemplate")
//    private JmsTemplate nonTransactedJmsTemplate;
//
//    @Autowired
//    private Queue queue;
//
//    @PostConstruct
//    public void checkConfiguration() {
//        if (bulkSize == 0) {
//            throw new IllegalStateException("'bulkSize' has to be greater than zero");
//        }
//    }
//
//    private int bulkSize() {
//        return nonTransactedJmsTemplate.execute(new SessionCallback<Integer>() {
//            @Override
//            public Integer doInJms(Session session) throws JMSException {
//                QueueBrowser queueBrowser = session.createBrowser(queue);
//                Enumeration queueMessageEnum = queueBrowser.getEnumeration();
//                int count = 0;
//                while (queueMessageEnum.hasMoreElements() && count < bulkSize) {
//                    queueMessageEnum.nextElement();
//                    count++;
//                }
//                return count;
//            }
//        }, true);
//    }
//
//    public void sendLogEntries() {
//
//        final int chunk = bulkSize();
//
//        if (chunk == 0) {
//            LOG.info("Zero messages in logging queue. Nothing will be sent to loggtjänst");
//        } else {
//            Boolean reExecute = jmsTemplate.execute(new JmsToLogSender(chunk), true);
//
//            // there may be messages left on the queue after the first chunk, so reperform the action
//            if (reExecute) {
//                sendLogEntries();
//            }
//        }
//
//    }
//
//    private List<LogType> convert(List<Message> messages) {
//        List<LogType> logTypes = new ArrayList<>();
//        for (Message message : messages) {
//            logTypes.add(convert(message));
//        }
//        return logTypes;
//    }
//
//    /**
//     * For WC 4.1 and RHS 1.0, adapted to handle lists of AbstractLogMessage too.
//     * @param message
//     * @return
//     */
//    private LogType convert(Message message) {
//        try {
//            Object element = ((ObjectMessage) message).getObject();
//
//            if (element instanceof AbstractLogMessage) {
//                AbstractLogMessage logMessage = (AbstractLogMessage) element;
//                return convert(logMessage);
//
//            } else if (element instanceof ArrayList) {
//
//                ArrayList<AbstractLogMessage> logMessages = (ArrayList<AbstractLogMessage>) element;
//
//                // TODO validate: All messages in List must originate from same user and system. Only patient info may differ.
//                return convertFromList(logMessages);
//
//            } else {
//                throw new RuntimeException("Unrecognized message type " + element.getClass().getCanonicalName());
//            }
//
//        } catch (JMSException e) {
//            throw new RuntimeException("Failed to read incoming JMS message", e);
//        }
//    }


//
//
//
//    private void sendLogEntriesToLoggtjanst(List<LogType> logEntries) {
//
//        StoreLogRequestType request = new StoreLogRequestType();
//        request.getLog().addAll(logEntries);
//
//        try {
//            StoreLogResponseType response = loggTjanstResponder.storeLog(logicalAddress, request);
//            switch (response.getResultType().getResultCode()) {
//            case OK:
//            case INFO:
//                break;
//            default:
//                throw new LoggtjanstExecutionException();
//            }
//        } catch (WebServiceException e) {
//            throw new LoggtjanstExecutionException(e);
//        }
//
//    }

//    private final class JmsToLogSender implements SessionCallback<Boolean> {
//        private final int chunk;
//
//        private JmsToLogSender(int chunk) {
//            this.chunk = chunk;
//        }
//
//        @Override
//        public Boolean doInJms(Session session) throws JMSException {
//            LOG.info("Transferring {} log entries to loggtjänst.", chunk);
//
//            MessageConsumer consumer = session.createConsumer(queue);
//
//            List<Message> messages = new ArrayList<>();
//            for (int i = 0; i < chunk; i++) {
//                messages.add(consumer.receive());
//            }
//
//            try {
//                sendLogEntriesToLoggtjanst(convert(messages));
//                session.commit();
//                return true;
//            } catch (LoggtjanstExecutionException e) {
//                LOG.error("Failed to send log entries to loggtjänst, JMS session will be rolled back.", e);
//                session.rollback();
//                return false;
//            }
//        }
//    }
}

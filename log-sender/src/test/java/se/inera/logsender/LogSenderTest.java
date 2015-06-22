package se.inera.logsender;

import static org.custommonkey.xmlunit.DifferenceConstants.NAMESPACE_PREFIX_ID;
import static org.custommonkey.xmlunit.DifferenceConstants.TEXT_VALUE_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.xml.bind.JAXBContext;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.XMLUnit;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.SessionCallback;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Node;

import se.inera.log.messages.AbstractLogMessage;
import se.inera.log.messages.Enhet;
import se.inera.log.messages.IntygPrintMessage;
import se.inera.log.messages.IntygReadMessage;
import se.inera.log.messages.IntygSendMessage;
import se.inera.log.messages.Patient;
import se.riv.ehr.log.store.storelog.rivtabp21.v1.StoreLogResponderInterface;
import se.riv.ehr.log.store.storelogresponder.v1.ObjectFactory;
import se.riv.ehr.log.store.storelogresponder.v1.StoreLogRequestType;
import se.riv.ehr.log.store.storelogresponder.v1.StoreLogResponseType;
import se.riv.ehr.log.store.v1.ResultType;
import se.riv.ehr.log.v1.LogType;
import se.riv.ehr.log.v1.ResultCodeType;


/**
 * @author andreaskaltenbach
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( "classpath:test-context.xml" )
@ActiveProfiles( profiles = "dev" )
@DirtiesContext( classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD )
public class LogSenderTest {

    private static final String LOGICAL_ADDRESS = "SE165565594230-1000";

    @Autowired
    private StoreLogResponderInterface storeLogMock;

    @Autowired
    @Qualifier( "nonTransactedJmsTemplate" )
    private JmsTemplate jmsTemplate;

    @Autowired
    private LogSender logSender;

    @Autowired
    private Queue queue;

    private List<AbstractLogMessage> logEntries = new ArrayList<AbstractLogMessage>() {
        {
            add(populateLogMessage("2013-01-01T10:00", new IntygReadMessage("abc123")));
            add(populateLogMessage("2013-01-02T10:00", new IntygReadMessage("abc123")));
            add(populateLogMessage("2013-01-03T10:00", new IntygPrintMessage("abc123","web")));
            add(populateLogMessage("2013-01-04T10:00", new IntygPrintMessage("abc123","pdf")));
            add(populateLogMessage("2013-01-05T10:00", new IntygSendMessage("abc123", "FK")));
            add(populateLogMessage("2013-01-06T10:00", new IntygSendMessage("abc123", "TS")));
        }
    };

    private Collection<String> logIds() {
        Collection<String> logIds = new HashSet<>();
        for (AbstractLogMessage logEntry : logEntries) {
            logIds.add(logEntry.getLogId());
        }
        return logIds;
    }

    private AbstractLogMessage populateLogMessage(String timestamp, AbstractLogMessage logMessage) {
        logMessage.setSystemId("webcert");
        logMessage.setSystemName("WebCert");
        logMessage.setTimestamp(new LocalDateTime(timestamp));
        logMessage.setUserId("user1");
        logMessage.setUserName("Markus Gran");

        Enhet enhet = new Enhet("enhet1", "Enhet 1", "vg1", "Vårdgivare 1");
        logMessage.setUserCareUnit(enhet);

        Patient patient = new Patient("19121212-1212", "Tolv Tolvasson");
        logMessage.setPatient(patient);
        
        Enhet owner = new Enhet("enhet1", "Enhet 1", "vg1", "Vårdgivare 1");
        logMessage.setResourceOwner(owner);

        return logMessage;
    }

    @Test
    public void testSendingLogMessages() throws InterruptedException {

        ArgumentCaptor<StoreLogRequestType> capture = ArgumentCaptor.forClass(StoreLogRequestType.class);

        for (AbstractLogMessage intygReadMessage : logEntries.subList(0, 3)) {
            sendLogMessage(intygReadMessage);
        }

        when(storeLogMock.storeLog(eq(LOGICAL_ADDRESS), capture.capture())).thenReturn(storeLogResponse(ResultCodeType.OK));

        logSender.sendLogEntries();

        // ensure that all three entries are sent to loggtjänst
        StoreLogRequestType request = capture.getValue();
        assertEquals(3, request.getLog().size());

        List<String> logIds = new ArrayList<String>() {
            {
                add(logEntries.get(0).getLogId());
                add(logEntries.get(1).getLogId());
                add(logEntries.get(2).getLogId());
            }
        };

        for (LogType logType : request.getLog()) {
            assertTrue(logIds.contains(logType.getLogId()));
        }

        // ensure that queue is empty
        assertEquals(0, queueSize());
    }

    @Test
    public void testLogMessageFormat() throws Exception {

        ArgumentCaptor<StoreLogRequestType> capture = ArgumentCaptor.forClass(StoreLogRequestType.class);

        for (AbstractLogMessage intygReadMessage : logEntries.subList(0, 1)) {
            sendLogMessage(intygReadMessage);
        }

        when(storeLogMock.storeLog(eq(LOGICAL_ADDRESS), capture.capture())).thenReturn(storeLogResponse(ResultCodeType.OK));

        logSender.sendLogEntries();

        // ensure that correct XML is sent to loggtjänst
        StoreLogRequestType request = capture.getValue();
        compareStoreLogRequest(request);
    }

    private void compareStoreLogRequest(StoreLogRequestType request) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(StoreLogRequestType.class);

        StringWriter stringWriter = new StringWriter();
        jaxbContext.createMarshaller().marshal(new ObjectFactory().createStoreLogRequest(request), stringWriter);

        String expectation = FileUtils.readFileToString(new ClassPathResource("LogSenderTest/store-log-request.xml").getFile());

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalizeWhitespace(true);

        Diff diff = XMLUnit.compareXML(expectation, stringWriter.toString());

        diff.overrideDifferenceListener(new NamespacePrefixNameIgnoringListener());
        assertTrue(diff.toString(), diff.identical());
    }

    @Test
    public void testSendingAndSplittingInChunks() throws InterruptedException {

        ArgumentCaptor<StoreLogRequestType> capture = ArgumentCaptor.forClass(StoreLogRequestType.class);

        for (AbstractLogMessage logMessage : logEntries) {
            sendLogMessage(logMessage);
        }

        when(storeLogMock.storeLog(eq(LOGICAL_ADDRESS), capture.capture())).thenReturn(storeLogResponse(ResultCodeType.OK));

        logSender.sendLogEntries();

        // ensure that messages are split into two chunks
        List<StoreLogRequestType> requests = capture.getAllValues();
        assertEquals(5, requests.get(0).getLog().size());

        // check that 5 of the 6 log IDs are sent to loggtjänst
        Collection<String> logIds = logIds();
        for (LogType logType : requests.get(0).getLog()) {
            assertTrue(logIds.contains(logType.getLogId()));
            logIds.remove(logType.getLogId());
        }

        // check that the last log ID was sent in a second call to the loggtjänst
        assertEquals(1, requests.get(1).getLog().size());
        assertEquals(logIds.iterator().next(), requests.get(1).getLog().get(0).getLogId());

        // ensure that queue is empty
        assertEquals(0, queueSize());
    }

    @Test
    public void testBulkSendingFailingSecondTime() throws InterruptedException, JMSException {

        for (AbstractLogMessage logMessage : logEntries) {
            sendLogMessage(logMessage);
        }

        when(storeLogMock.storeLog(eq(LOGICAL_ADDRESS), any(StoreLogRequestType.class))).thenReturn(
                storeLogResponse(ResultCodeType.OK)).thenReturn(storeLogResponse(ResultCodeType.ERROR));

        logSender.sendLogEntries();

        // ensure that queue still contains last messages
        assertEquals(1, queueSize());

        // ensure that remaining message is last log entry
        Message message = jmsTemplate.receive();
        ObjectMessage objectMessage = (ObjectMessage) message;
        AbstractLogMessage logMessage = (AbstractLogMessage) objectMessage.getObject();
        assertEquals(logEntries.get(5).getLogId(), logMessage.getLogId());
    }

    @Test
    public void testBulkSendingWithFailingLoggtjanst() throws InterruptedException {

        for (AbstractLogMessage logMessage : logEntries.subList(0, 3)) {
            sendLogMessage(logMessage);
        }

        when(storeLogMock.storeLog(eq(LOGICAL_ADDRESS), any(StoreLogRequestType.class))).thenReturn(
                storeLogResponse(ResultCodeType.ERROR));

        logSender.sendLogEntries();

        // messages should still be in queue
        assertEquals(3, queueSize());
    }

    private int queueSize() {
        return jmsTemplate.execute(new SessionCallback<Integer>() {
            @Override
            public Integer doInJms(Session session) throws JMSException {
                QueueBrowser queueBrowser = session.createBrowser(queue);
                Enumeration queueMessageEnum = queueBrowser.getEnumeration();
                int count = 0;
                while (queueMessageEnum.hasMoreElements()) {
                    queueMessageEnum.nextElement();
                    count++;
                }
                return count;
            }
        }, true);
    }

    private StoreLogResponseType storeLogResponse(ResultCodeType resultCode) {
        StoreLogResponseType response = new StoreLogResponseType();
        response.setResultType(new ResultType());
        response.getResultType().setResultCode(resultCode);
        return response;
    }

    private void sendLogMessage(final AbstractLogMessage intygReadMessage) {
        jmsTemplate.send(queue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createObjectMessage(intygReadMessage);
            }
        });
    }

    private class NamespacePrefixNameIgnoringListener implements DifferenceListener {

        public int differenceFound(Difference difference) {

            switch (difference.getId()) {
                case NAMESPACE_PREFIX_ID:
                    // differences in namespace prefix IDs are ok (eg. 'ns1' vs 'ns2'), as long as the namespace URI is the same
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                case TEXT_VALUE_ID:
                    String nodeName = difference.getTestNodeDetail().getNode().getParentNode().getNodeName();
                    String nodeValue = difference.getTestNodeDetail().getValue();
                    if ("LogId".equals(nodeName) && nodeValue.equals(logEntries.get(0).getLogId())) {
                        return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                    }
                default:
                    return RETURN_ACCEPT_DIFFERENCE;
            }
        }

        public void skippedComparison(Node control, Node test) {
        }
    }
}

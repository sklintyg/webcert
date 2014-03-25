package se.inera.certificate.mc2wc.batch.writer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.ClassPathResource;
import se.inera.certificate.mc2wc.exception.FatalCertificateMigrationException;
import se.inera.certificate.mc2wc.message.MigrationMessage;
import se.inera.certificate.mc2wc.message.MigrationReply;
import se.inera.certificate.mc2wc.message.MigrationResultType;
import se.inera.certificate.mc2wc.rest.MigrationReceiver;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RestServiceItemWriterTest {


    private RestServiceItemWriter writer;

    @Mock
    private StepExecution stepExecution;
    @Mock
    private JobExecution jobExecution;
    @Mock
    private ExecutionContext executionContext;
    @Mock
    private JobParameters jobParameters;
    @Mock
    private MigrationReceiver migrationReceiver;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(jobExecution.getExecutionContext()).thenReturn(executionContext);
        when(jobExecution.getJobParameters()).thenReturn(jobParameters);
        when(stepExecution.getJobExecution()).thenReturn(jobExecution);

        writer = new RestServiceItemWriter();

        writer.setStepExecution(stepExecution);
        writer.setMigrationReceiver(migrationReceiver);
    }

    @Test
    public void normalMigrationMessageSent() throws Exception {

        MigrationMessage message = getMigrationMessageFromTemplate();

        List<MigrationMessage> messages = Arrays.asList(message);

        MigrationReply reply = new MigrationReply();
        reply.setResult(MigrationResultType.OK);

        when(executionContext.getLong(RestServiceItemWriter.CERTIFICATE_WRITE_COUNT, 0)).thenReturn(5l);
        when(migrationReceiver.receive(message)).thenReturn(reply);

        writer.write(messages);

        verify(executionContext).putLong(RestServiceItemWriter.CERTIFICATE_WRITE_COUNT, 6l);
    }

    @Test
    public void testDuplicate() throws Exception {

        MigrationMessage message = getMigrationMessageFromTemplate();

        List<MigrationMessage> messages = Arrays.asList(message);

        MigrationReply reply = new MigrationReply();
        reply.setResult(MigrationResultType.DUPLICATE);

        when(executionContext.getLong(RestServiceItemWriter.CERTIFICATE_WRITE_COUNT, 0)).thenReturn(5l);
        when(executionContext.getLong(RestServiceItemWriter.DUPLICATE_COUNT, 0)).thenReturn(2l);
        when(migrationReceiver.receive(message)).thenReturn(reply);

        writer.write(messages);

        verify(executionContext).putLong(RestServiceItemWriter.CERTIFICATE_WRITE_COUNT, 5l);
        verify(executionContext).putLong(RestServiceItemWriter.DUPLICATE_COUNT, 3l);
    }

    @Test(expected = FatalCertificateMigrationException.class)
    public void testDuplicateOverLimit() throws Exception {

        MigrationMessage message = getMigrationMessageFromTemplate();

        List<MigrationMessage> messages = Arrays.asList(message);

        MigrationReply reply = new MigrationReply();
        reply.setResult(MigrationResultType.DUPLICATE);

        when(executionContext.getLong(RestServiceItemWriter.CERTIFICATE_WRITE_COUNT, 0)).thenReturn(5l);
        when(executionContext.getLong(RestServiceItemWriter.DUPLICATE_COUNT, 0)).thenReturn(100l);
        when(migrationReceiver.receive(message)).thenReturn(reply);

        writer.write(messages);


    }



    public MigrationMessage getMigrationMessageFromTemplate() throws Exception {

        ClassPathResource resource = new ClassPathResource("/xml/migration-message-template.xml");

        JAXBContext jaxbContext = JAXBContext.newInstance(MigrationMessage.class);
        Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();

        return (MigrationMessage) unMarshaller.unmarshal(resource.getInputStream());
    }
}

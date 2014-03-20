package se.inera.certificate.mc2wc.batch.writer;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.inera.certificate.mc2wc.exception.CertificateMigrationException;
import se.inera.certificate.mc2wc.exception.FatalCertificateMigrationException;
import se.inera.certificate.mc2wc.message.MigrationMessage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/spring/rest-client-test-context.xml",
        "classpath:/spring/rest-client-context.xml", "classpath:/spring/beans-context.xml"})
public class RestServiceItemWriterTest {

    @Autowired
    private RestServiceItemWriter writer;

    public RestServiceItemWriterTest() {

    }

    @Test
    public void normalMigrationMessageSent() throws Exception {

        MigrationMessage message = getMigrationMessageFromTemplate();

        List<MigrationMessage> messages = Arrays.asList(message);

        writer.write(messages);
    }

    @Ignore
    @Test(expected = FatalCertificateMigrationException.class)
    public void serverReturnsServerError() throws Exception {

        MigrationMessage message = getMigrationMessageFromTemplate();
        message.getCertificate().setCertificateId(MockMigrationRecieverBean.HTTP_500);

        List<MigrationMessage> messages = Arrays.asList(message);

        writer.write(messages);
    }

    @Ignore
    @Test(expected = CertificateMigrationException.class)
    public void serverReturnsBadRequest() throws Exception {

        MigrationMessage message = getMigrationMessageFromTemplate();
        message.getCertificate().setCertificateId(MockMigrationRecieverBean.HTTP_400);

        List<MigrationMessage> messages = Arrays.asList(message);

        writer.write(messages);
    }

    public MigrationMessage getMigrationMessageFromTemplate() throws Exception {

        ClassPathResource resource = new ClassPathResource("/xml/migration-message-template.xml");

        JAXBContext jaxbContext = JAXBContext.newInstance(MigrationMessage.class);
        Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();

        return (MigrationMessage) unMarshaller.unmarshal(resource.getInputStream());
    }
}

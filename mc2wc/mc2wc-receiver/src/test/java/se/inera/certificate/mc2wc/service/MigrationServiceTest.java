package se.inera.certificate.mc2wc.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import se.inera.certificate.mc2wc.message.MigrationMessage;
import se.inera.certificate.mc2wc.message.MigrationResultType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/spring/repository-context.xml", "classpath:/spring/service-context.xml"})
@ActiveProfiles("dev")
@Transactional
public class MigrationServiceTest {

    @Autowired
    private MigrationService migrationService;

    @Test
    public void testSome() throws Exception {

        MigrationMessage migrationMessage = readTestData("mc2wc-test-1.xml");

        MigrationResultType result = migrationService.processMigrationMessage(migrationMessage);

        assertEquals(MigrationResultType.OK, result);
    }

    private MigrationMessage readTestData(String fileName) throws Exception {

        String filePath = "/testdata/" + fileName;

        ClassPathResource resource = new ClassPathResource(filePath);

        JAXBContext jaxbCtx = JAXBContext.newInstance(MigrationMessage.class);
        Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
        MigrationMessage migrMsg = (MigrationMessage) unmarshaller.unmarshal(resource.getInputStream());

        return migrMsg;
    }
}

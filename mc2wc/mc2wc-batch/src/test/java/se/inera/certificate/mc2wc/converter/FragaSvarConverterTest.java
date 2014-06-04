package se.inera.certificate.mc2wc.converter;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import se.inera.certificate.mc2wc.message.MigrationMessage;
import se.inera.certificate.mc2wc.message.QuestionType;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

public class FragaSvarConverterTest {

    private FragaSvarConverterImpl converter = new FragaSvarConverterImpl();
        
    private static JAXBContext context;

    private static Unmarshaller unmarshaller;
      
    @Test
    public void testFragaSvarConverterWithQuestionAndAnswer() throws Exception {
        
        MigrationMessage mm = unmarshall("/xml/migration-message-with-question-answer.xml");
        QuestionType qa = mm.getQuestions().get(0);
        
        FragaSvar fs = converter.toFragaSvar(qa);
        
        assertNotNull(fs);
        assertNotNull(fs.getAmne());
        assertNotNull(fs.getStatus());
        assertNotNull(fs.getAmne());
        assertNotNull(fs.getSvarsText());
        assertNotNull(fs.getFrageText());
        assertNotNull(fs.getFrageStallare());
        assertNotNull(fs.getVardperson());
        assertNotNull(fs.getFrageSigneringsDatum());
        assertNotNull(fs.getFrageSkickadDatum());
        assertNotNull(fs.getIntygsReferens());
        //assertNotNull(fs.getVardAktorHsaId());
        //assertNotNull(fs.getVardAktorNamn());
    }
        
    private MigrationMessage unmarshall(String testDoc) throws JAXBException, IOException {
        ClassPathResource res = new ClassPathResource(testDoc);
        return (MigrationMessage) unmarshaller.unmarshal(res.getFile());
    }

    @BeforeClass
    public static void initJaxbContext() {
        try {
            context = JAXBContext.newInstance(MigrationMessage.class);
            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context: "
                    + e.getMessage(), e);
        }
    }
}

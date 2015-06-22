package se.inera.webcert.converter;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;


/**
 * @author andreaskaltenbach
 */
public class FragaSvarConverterTest {

    @Test
    public void testConvertQuestion() throws Exception {
        FragaSvar fragaSvar = new FragaSvarConverter().convert(question("FragaSvarConverterTest/question.xml"));
        compareObjectWithReferenceFile(fragaSvar, "FragaSvarConverterTest/question.json");
    }

    @Test
    public void testConvertQuestionLongMeddelandeRubrik() throws Exception {
        FragaSvar fragaSvar = new FragaSvarConverter().convert(question("FragaSvarConverterTest/question_long_meddelande_rubrik.xml"));
        compareObjectWithReferenceFile(fragaSvar, "FragaSvarConverterTest/question_long_meddelande_rubrik.json");
    }

    private void compareObjectWithReferenceFile(Object object, String fileName) throws IOException {
        ObjectMapper objectMapper = new CustomObjectMapper();
        JsonNode tree = objectMapper.valueToTree(object);
        JsonNode expectedTree = objectMapper.readTree(new ClassPathResource(fileName).getInputStream());
        assertEquals("JSON does not match expectation. Resulting JSON is \n" + tree.toString() + "\n", expectedTree,
                tree);
    }

    private QuestionFromFkType question(String fileName) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(QuestionFromFkType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return unmarshaller.unmarshal(
                new StreamSource(new ClassPathResource(fileName).getInputStream()),
                QuestionFromFkType.class).getValue();
    }

}

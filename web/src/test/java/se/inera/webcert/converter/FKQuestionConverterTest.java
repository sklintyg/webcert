package se.inera.webcert.converter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.ObjectFactory;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.QuestionToFkType;
import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringWriter;
import java.io.Writer;

/**
 * This test makes use of Equals and HashCode from JAXB basics. All types must implement
 * this. 
 */
public class FKQuestionConverterTest {

	private FragaSvarConverter fragaSvarConverter = new FragaSvarConverter();
	
	private QuestionFromFkType inflateQuestionFromFK() throws Exception {
		JAXBContext jaxbContext = JAXBContext
				.newInstance(QuestionFromFkType.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		return unmarshaller
				.unmarshal(
						new StreamSource(
								new ClassPathResource(
										"FragaSvarConverterTest/question.xml")
										.getInputStream()),
						QuestionFromFkType.class).getValue();
	}

	private QuestionToFkType inflateQuestionToFK() throws Exception {
		JAXBContext jaxbContext = JAXBContext
				.newInstance(QuestionToFkType.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		return unmarshaller.unmarshal(
				new StreamSource(
						new ClassPathResource(
								"FragaSvarConverterTest/question_to_fk.xml")
								.getInputStream()), QuestionToFkType.class)
				.getValue();
	}

    private String jaxbToXml(QuestionToFkType object) throws JAXBException {
        ObjectFactory objectFactory = new ObjectFactory();
        Writer writer = new StringWriter();

        // Init JAXB context
        JAXBContext jaxbContext = JAXBContext.newInstance(QuestionToFkType.class);
        Marshaller marshaller = jaxbContext.createMarshaller();

        // Create a string representation from JAXB element
        marshaller.marshal(objectFactory.createQuestion(object), writer);

        return writer.toString();
    }

	@Test
	public void testConvertQuestion() throws Exception {

		QuestionFromFkType questionFromFK = inflateQuestionFromFK();
		QuestionToFkType referenceQuestionToFK = inflateQuestionToFK();

		// convert QuestionFromFK to FragaSvar entity
		FragaSvar fragaSvar = fragaSvarConverter.convert(questionFromFK);
		
		// add some data
		fragaSvar.setAmne(Amne.KONTAKT);
		fragaSvar.setInternReferens(321L);

		// convert fragaSvar entity to QuestionToFK
		QuestionToFkType convertedQuestionToFK = FKQuestionConverter.convert(fragaSvar);

        // compare convertedQuestionToFK to reference
        String expected = jaxbToXml(referenceQuestionToFK);
        String actual = jaxbToXml(convertedQuestionToFK);
        assertEquals(expected, actual);
	}

}

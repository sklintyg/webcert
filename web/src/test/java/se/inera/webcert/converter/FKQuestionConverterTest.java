package se.inera.webcert.converter;

import org.junit.Assert;
import org.junit.Test;
import org.jvnet.jaxb2_commons.lang.JAXBEqualsStrategy;
import org.springframework.core.io.ClassPathResource;

import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.QuestionToFkType;
import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

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
		Assert.assertTrue(referenceQuestionToFK.equals(null, null, convertedQuestionToFK, JAXBEqualsStrategy.INSTANCE));
	}

}

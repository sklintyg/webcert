/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.converter;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.ObjectFactory;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.QuestionToFkType;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;

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
        return unmarshaller.unmarshal(new StreamSource(new ClassPathResource("FragaSvarConverterTest/question.xml").getInputStream()),
                QuestionFromFkType.class).getValue();
    }

    private QuestionToFkType inflateQuestionToFK() throws Exception {
        JAXBContext jaxbContext = JAXBContext
                .newInstance(QuestionToFkType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return unmarshaller.unmarshal(new StreamSource(new ClassPathResource("FragaSvarConverterTest/question_to_fk.xml").getInputStream()),
                QuestionToFkType.class).getValue();
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

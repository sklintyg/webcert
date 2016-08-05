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

package se.inera.intyg.webcert.web.integration.validator;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificateanswerresponder.v1.AnswerFromFkType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionType;

/**
 * Created by marced on 05/08/16.
 */
public class QuestionAnswerValidatorTest {

    @Test
    public void testQVPassesValidRequest() {
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        assertEquals(0, QuestionAnswerValidator.validate(request).size());
    }

    @Test
    public void testQVCatchesMissingFraga() {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().setFraga(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesMissingAmne() {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().setAmne(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesMissingLakarutlatandeId() {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getLakarutlatande().setLakarutlatandeId(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesMissingHsaPersonIdRoot() {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getAdressVard().getHosPersonal().getPersonalId().setRoot(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesInvalidHsaPersonIdRoot() {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getAdressVard().getHosPersonal().getPersonalId().setRoot("INVALID");

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesMissingHsaPersonIdExtension() {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getAdressVard().getHosPersonal().getPersonalId().setExtension(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesMissingHsaPersonFullstandigtNamn() {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getAdressVard().getHosPersonal().setFullstandigtNamn(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesInvalidPatientPersonIdRoot() {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getLakarutlatande().getPatient().getPersonId().setRoot("INVALID");

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesMissingPatientPersonIdExtension() {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getLakarutlatande().getPatient().getPersonId().setExtension(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesMissingPatientPersonFullstandigtNamn() {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getLakarutlatande().getPatient().setFullstandigtNamn(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesInvalidHsaEnhetIdRoot() {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getAdressVard().getHosPersonal().getEnhet().getEnhetsId().setRoot("INVALID");

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesMissingHsaEnhetIdExtension() {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getAdressVard().getHosPersonal().getEnhet().getEnhetsId().setExtension(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesMissingHsaEnhetsNamn() {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getAdressVard().getHosPersonal().getEnhet().setEnhetsnamn(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesInvalidVardgivarIdRoot() {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getAdressVard().getHosPersonal().getEnhet().getVardgivare().getVardgivareId().setRoot("INVALID");

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesMissingVardgivarIdExtension() {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getAdressVard().getHosPersonal().getEnhet().getVardgivare().getVardgivareId().setExtension(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testQVCatchesMissingVardgivarNamn() {
        // Arrange
        ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
        request.getQuestion().getAdressVard().getHosPersonal().getEnhet().getVardgivare().setVardgivarnamn(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    // --------- Answer tests
    @Test
    public void testAVPassesValidRequest() {
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        assertEquals(0, QuestionAnswerValidator.validate(request).size());
    }

    @Test
    public void testAVCatchesMissingFraga() {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().setSvar(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesMissingAmne() {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().setAmne(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesMissingLakarutlatandeId() {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getLakarutlatande().setLakarutlatandeId(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesMissingHsaPersonIdRoot() {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getAdressVard().getHosPersonal().getPersonalId().setRoot(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesInvalidHsaPersonIdRoot() {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getAdressVard().getHosPersonal().getPersonalId().setRoot("INVALID");

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesMissingHsaPersonIdExtension() {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getAdressVard().getHosPersonal().getPersonalId().setExtension(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesMissingHsaPersonFullstandigtNamn() {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getAdressVard().getHosPersonal().setFullstandigtNamn(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesInvalidPatientPersonIdRoot() {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getLakarutlatande().getPatient().getPersonId().setRoot("INVALID");

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesMissingPatientPersonIdExtension() {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getLakarutlatande().getPatient().getPersonId().setExtension(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesMissingPatientPersonFullstandigtNamn() {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getLakarutlatande().getPatient().setFullstandigtNamn(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesInvalidHsaEnhetIdRoot() {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getAdressVard().getHosPersonal().getEnhet().getEnhetsId().setRoot("INVALID");

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesMissingHsaEnhetIdExtension() {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getAdressVard().getHosPersonal().getEnhet().getEnhetsId().setExtension(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesMissingHsaEnhetsNamn() {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getAdressVard().getHosPersonal().getEnhet().setEnhetsnamn(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesInvalidVardgivarIdRoot() {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getAdressVard().getHosPersonal().getEnhet().getVardgivare().getVardgivareId().setRoot("INVALID");

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesMissingVardgivarIdExtension() {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getAdressVard().getHosPersonal().getEnhet().getVardgivare().getVardgivareId().setExtension(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testAVCatchesMissingVardgivarNamn() {
        // Arrange
        ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
        request.getAnswer().getAdressVard().getHosPersonal().getEnhet().getVardgivare().setVardgivarnamn(null);

        // Act
        final List<String> result = QuestionAnswerValidator.validate(request);

        // Assert
        assertEquals(1, result.size());
    }

    private ReceiveMedicalCertificateAnswerType createValidAnswerRequest() {
        ReceiveMedicalCertificateAnswerType request = new ReceiveMedicalCertificateAnswerType();

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(AnswerFromFkType.class);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            AnswerFromFkType answer = unmarshaller
                    .unmarshal(new StreamSource(new ClassPathResource("QuestionAnswerValidator/valid-answer-from-fk.xml").getInputStream()),
                            AnswerFromFkType.class)
                    .getValue();
            request.setAnswer(answer);

            return request;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load QuestionRequest template");
        }

    }

    private ReceiveMedicalCertificateQuestionType createValidQuestionRequest() {
        ReceiveMedicalCertificateQuestionType request = new ReceiveMedicalCertificateQuestionType();

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(QuestionFromFkType.class);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            QuestionFromFkType question = unmarshaller
                    .unmarshal(new StreamSource(new ClassPathResource("QuestionAnswerValidator/valid-question-from-fk.xml").getInputStream()),
                            QuestionFromFkType.class)
                    .getValue();
            request.setQuestion(question);

            return request;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load QuestionRequest template");
        }

    }

}

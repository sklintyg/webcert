/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.validators;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import java.util.List;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificateanswerresponder.v1.AnswerFromFkType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionType;
import se.inera.intyg.webcert.web.integration.interactions.receivemedicalcertificate.QuestionAnswerValidator;

/** Created by marced on 05/08/16. */
class QuestionAnswerValidatorTest {

  @Test
  void testQVPassesValidRequest() {
    ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
    assertEquals(0, QuestionAnswerValidator.validate(request).size());
  }

  @Test
  void testQVCatchesMissingFraga() {
    // Arrange
    ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
    request.getQuestion().setFraga(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testQVCatchesMissingAmne() {
    // Arrange
    ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
    request.getQuestion().setAmne(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testQVCatchesMissingLakarutlatandeId() {
    // Arrange
    ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
    request.getQuestion().getLakarutlatande().setLakarutlatandeId(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testQVCatchesMissingHsaPersonIdRoot() {
    // Arrange
    ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
    request.getQuestion().getAdressVard().getHosPersonal().getPersonalId().setRoot(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testQVCatchesInvalidHsaPersonIdRoot() {
    // Arrange
    ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
    request.getQuestion().getAdressVard().getHosPersonal().getPersonalId().setRoot("INVALID");

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testQVCatchesMissingHsaPersonIdExtension() {
    // Arrange
    ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
    request.getQuestion().getAdressVard().getHosPersonal().getPersonalId().setExtension(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testQVCatchesMissingHsaPersonFullstandigtNamn() {
    // Arrange
    ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
    request.getQuestion().getAdressVard().getHosPersonal().setFullstandigtNamn(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testQVCatchesInvalidPatientPersonIdRoot() {
    // Arrange
    ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
    request.getQuestion().getLakarutlatande().getPatient().getPersonId().setRoot("INVALID");

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testQVCatchesMissingPatientPersonIdExtension() {
    // Arrange
    ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
    request.getQuestion().getLakarutlatande().getPatient().getPersonId().setExtension(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testQVCatchesMissingPatientPersonFullstandigtNamn() {
    // Arrange
    ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
    request.getQuestion().getLakarutlatande().getPatient().setFullstandigtNamn(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testQVCatchesInvalidHsaEnhetIdRoot() {
    // Arrange
    ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
    request
        .getQuestion()
        .getAdressVard()
        .getHosPersonal()
        .getEnhet()
        .getEnhetsId()
        .setRoot("INVALID");

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testQVCatchesMissingHsaEnhetIdExtension() {
    // Arrange
    ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
    request
        .getQuestion()
        .getAdressVard()
        .getHosPersonal()
        .getEnhet()
        .getEnhetsId()
        .setExtension(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testQVCatchesMissingHsaEnhetsNamn() {
    // Arrange
    ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
    request.getQuestion().getAdressVard().getHosPersonal().getEnhet().setEnhetsnamn(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testQVCatchesInvalidVardgivarIdRoot() {
    // Arrange
    ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
    request
        .getQuestion()
        .getAdressVard()
        .getHosPersonal()
        .getEnhet()
        .getVardgivare()
        .getVardgivareId()
        .setRoot("INVALID");

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testQVCatchesMissingVardgivarIdExtension() {
    // Arrange
    ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
    request
        .getQuestion()
        .getAdressVard()
        .getHosPersonal()
        .getEnhet()
        .getVardgivare()
        .getVardgivareId()
        .setExtension(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testQVCatchesMissingVardgivarNamn() {
    // Arrange
    ReceiveMedicalCertificateQuestionType request = createValidQuestionRequest();
    request
        .getQuestion()
        .getAdressVard()
        .getHosPersonal()
        .getEnhet()
        .getVardgivare()
        .setVardgivarnamn(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  // --------- Answer tests
  @Test
  void testAVPassesValidRequest() {
    ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
    assertEquals(0, QuestionAnswerValidator.validate(request).size());
  }

  @Test
  void testAVCatchesMissingFraga() {
    // Arrange
    ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
    request.getAnswer().setSvar(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testAVCatchesMissingAmne() {
    // Arrange
    ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
    request.getAnswer().setAmne(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testAVCatchesMissingLakarutlatandeId() {
    // Arrange
    ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
    request.getAnswer().getLakarutlatande().setLakarutlatandeId(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testAVCatchesMissingHsaPersonIdRoot() {
    // Arrange
    ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
    request.getAnswer().getAdressVard().getHosPersonal().getPersonalId().setRoot(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testAVCatchesInvalidHsaPersonIdRoot() {
    // Arrange
    ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
    request.getAnswer().getAdressVard().getHosPersonal().getPersonalId().setRoot("INVALID");

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testAVCatchesMissingHsaPersonIdExtension() {
    // Arrange
    ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
    request.getAnswer().getAdressVard().getHosPersonal().getPersonalId().setExtension(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testAVCatchesMissingHsaPersonFullstandigtNamn() {
    // Arrange
    ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
    request.getAnswer().getAdressVard().getHosPersonal().setFullstandigtNamn(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testAVCatchesInvalidPatientPersonIdRoot() {
    // Arrange
    ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
    request.getAnswer().getLakarutlatande().getPatient().getPersonId().setRoot("INVALID");

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testAVCatchesMissingPatientPersonIdExtension() {
    // Arrange
    ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
    request.getAnswer().getLakarutlatande().getPatient().getPersonId().setExtension(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testAVCatchesMissingPatientPersonFullstandigtNamn() {
    // Arrange
    ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
    request.getAnswer().getLakarutlatande().getPatient().setFullstandigtNamn(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testAVCatchesInvalidHsaEnhetIdRoot() {
    // Arrange
    ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
    request
        .getAnswer()
        .getAdressVard()
        .getHosPersonal()
        .getEnhet()
        .getEnhetsId()
        .setRoot("INVALID");

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testAVCatchesMissingHsaEnhetIdExtension() {
    // Arrange
    ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
    request
        .getAnswer()
        .getAdressVard()
        .getHosPersonal()
        .getEnhet()
        .getEnhetsId()
        .setExtension(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testAVCatchesMissingHsaEnhetsNamn() {
    // Arrange
    ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
    request.getAnswer().getAdressVard().getHosPersonal().getEnhet().setEnhetsnamn(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testAVCatchesInvalidVardgivarIdRoot() {
    // Arrange
    ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
    request
        .getAnswer()
        .getAdressVard()
        .getHosPersonal()
        .getEnhet()
        .getVardgivare()
        .getVardgivareId()
        .setRoot("INVALID");

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testAVCatchesMissingVardgivarIdExtension() {
    // Arrange
    ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
    request
        .getAnswer()
        .getAdressVard()
        .getHosPersonal()
        .getEnhet()
        .getVardgivare()
        .getVardgivareId()
        .setExtension(null);

    // Act
    final List<String> result = QuestionAnswerValidator.validate(request);

    // Assert
    assertEquals(1, result.size());
  }

  @Test
  void testAVCatchesMissingVardgivarNamn() {
    // Arrange
    ReceiveMedicalCertificateAnswerType request = createValidAnswerRequest();
    request
        .getAnswer()
        .getAdressVard()
        .getHosPersonal()
        .getEnhet()
        .getVardgivare()
        .setVardgivarnamn(null);

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

      AnswerFromFkType answer =
          unmarshaller
              .unmarshal(
                  new StreamSource(
                      new ClassPathResource("QuestionAnswerValidator/valid-answer-from-fk.xml")
                          .getInputStream()),
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

      QuestionFromFkType question =
          unmarshaller
              .unmarshal(
                  new StreamSource(
                      new ClassPathResource("QuestionAnswerValidator/valid-question-from-fk.xml")
                          .getInputStream()),
                  QuestionFromFkType.class)
              .getValue();
      request.setQuestion(question);

      return request;
    } catch (Exception e) {
      throw new RuntimeException("Failed to load QuestionRequest template");
    }
  }
}

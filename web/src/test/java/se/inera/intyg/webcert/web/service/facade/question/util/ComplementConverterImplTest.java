/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.facade.question.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.services.texts.CertificateTextProvider;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.MedicinsktArende;
import se.inera.intyg.webcert.web.service.certificate.GetCertificateService;

@ExtendWith(MockitoExtension.class)
class ComplementConverterImplTest {

    @Mock
    private GetCertificateService getCertificateService;

    @Mock
    private IntygModuleRegistry intygModuleRegistry;

    @InjectMocks
    private ComplementConverterImpl complementConverter;

    private Arende complementQuestion;
    private Arende anotherComplementQuestion;
    private List<MedicinsktArende> kompletteringar = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        final var certificateId = "certificateId";
        final var certificateType = "certificateType";
        final var certificateTypeVersion = "certificateTypeVersion";

        complementQuestion = new Arende();
        complementQuestion.setMeddelandeId("complementQuestionId");
        complementQuestion.setIntygsId(certificateId);
        complementQuestion.setIntygTyp(certificateType);
        complementQuestion.setKomplettering(kompletteringar);

        anotherComplementQuestion = new Arende();
        anotherComplementQuestion.setMeddelandeId("anotherComplementQuestionId");
        anotherComplementQuestion.setIntygsId(certificateId);
        anotherComplementQuestion.setIntygTyp(certificateType);
        anotherComplementQuestion.setKomplettering(kompletteringar);

        final var certificateAsUtlatande = mock(Utlatande.class);
        doReturn(certificateAsUtlatande)
            .when(getCertificateService)
            .getCertificateAsUtlatande(certificateId, certificateType);

        doReturn(certificateType)
            .when(certificateAsUtlatande)
            .getTyp();

        doReturn(certificateTypeVersion)
            .when(certificateAsUtlatande)
            .getTextVersion();

        final var moduleApi = mock(ModuleApi.class);
        doReturn(moduleApi)
            .when(intygModuleRegistry)
            .getModuleApi(certificateType, certificateTypeVersion);

        final var jsonPropertiesMap = Map.of("questionId", List.of("jsonProperty"));
        doReturn(jsonPropertiesMap)
            .when(moduleApi)
            .getModuleSpecificArendeParameters(eq(certificateAsUtlatande), anyList());

        final var certificateTextProvider = mock(CertificateTextProvider.class);
        doReturn(certificateTextProvider)
            .when(moduleApi)
            .getTextProvider(certificateType, certificateTypeVersion);

        doReturn("questionText")
            .when(certificateTextProvider)
            .get("questionId");
    }

    @Test
    void shallReturnConvertedComplement() {
        final var medicinsktArende = new MedicinsktArende();
        medicinsktArende.setFrageId("questionId");
        kompletteringar.add(medicinsktArende);

        final var actualComplements = complementConverter.convert(complementQuestion);

        assertTrue(actualComplements.length == 1, "Should contain one converted complement");
    }

    @Test
    void shallReturnConvertedComplementWithQuestionId() {
        final var medicinsktArende = new MedicinsktArende();
        medicinsktArende.setFrageId("questionId");
        kompletteringar.add(medicinsktArende);

        final var actualComplements = complementConverter.convert(complementQuestion);

        assertEquals(medicinsktArende.getFrageId(), actualComplements[0].getQuestionId());
    }

    @Test
    void shallReturnConvertedComplementWithQuestionText() {
        final var medicinsktArende = new MedicinsktArende();
        medicinsktArende.setFrageId("questionId");
        kompletteringar.add(medicinsktArende);

        final var actualComplements = complementConverter.convert(complementQuestion);

        assertEquals("questionText", actualComplements[0].getQuestionText());
    }

    @Test
    void shallReturnConvertedComplementWithValueId() {
        final var medicinsktArende = new MedicinsktArende();
        medicinsktArende.setFrageId("questionId");
        kompletteringar.add(medicinsktArende);

        final var actualComplements = complementConverter.convert(complementQuestion);

        assertEquals("jsonProperty", actualComplements[0].getValueId());
    }

    @Test
    void shallReturnConvertedComplementWithMessage() {
        final var medicinsktArende = new MedicinsktArende();
        medicinsktArende.setFrageId("questionId");
        medicinsktArende.setText("Meddelande till kompletteringen");
        kompletteringar.add(medicinsktArende);

        final var actualComplements = complementConverter.convert(complementQuestion);

        assertEquals(medicinsktArende.getText(), actualComplements[0].getMessage());
    }

    @Test
    void shallReturnConvertedComplements() {
        final var medicinsktArende = new MedicinsktArende();
        medicinsktArende.setFrageId("questionId");
        kompletteringar.add(medicinsktArende);

        final var actualComplements = complementConverter.convert(List.of(complementQuestion, anotherComplementQuestion));

        assertTrue(actualComplements.get("complementQuestionId").length == 1, "Should contain one converted complement");
        assertTrue(actualComplements.get("anotherComplementQuestionId").length == 1, "Should contain one converted complement");
    }

    @Test
    void shallReturnConvertedComplementsWithQuestionId() {
        final var medicinsktArende = new MedicinsktArende();
        medicinsktArende.setFrageId("questionId");
        kompletteringar.add(medicinsktArende);

        final var actualComplements = complementConverter.convert(List.of(complementQuestion, anotherComplementQuestion));

        assertEquals(medicinsktArende.getFrageId(), actualComplements.get("complementQuestionId")[0].getQuestionId());
        assertEquals(medicinsktArende.getFrageId(), actualComplements.get("anotherComplementQuestionId")[0].getQuestionId());
    }

    @Test
    void shallReturnConvertedComplementsWithQuestionText() {
        final var medicinsktArende = new MedicinsktArende();
        medicinsktArende.setFrageId("questionId");
        kompletteringar.add(medicinsktArende);

        final var actualComplements = complementConverter.convert(List.of(complementQuestion, anotherComplementQuestion));

        assertEquals("questionText", actualComplements.get("complementQuestionId")[0].getQuestionText());
        assertEquals("questionText", actualComplements.get("anotherComplementQuestionId")[0].getQuestionText());
    }

    @Test
    void shallReturnConvertedComplementsWithValueId() {
        final var medicinsktArende = new MedicinsktArende();
        medicinsktArende.setFrageId("questionId");
        kompletteringar.add(medicinsktArende);

        final var actualComplements = complementConverter.convert(List.of(complementQuestion, anotherComplementQuestion));

        assertEquals("jsonProperty", actualComplements.get("complementQuestionId")[0].getValueId());
        assertEquals("jsonProperty", actualComplements.get("anotherComplementQuestionId")[0].getValueId());
    }

    @Test
    void shallReturnConvertedComplementsWithMessage() {
        final var medicinsktArende = new MedicinsktArende();
        medicinsktArende.setFrageId("questionId");
        medicinsktArende.setText("Meddelande till kompletteringen");
        kompletteringar.add(medicinsktArende);

        final var actualComplements = complementConverter.convert(List.of(complementQuestion, anotherComplementQuestion));

        assertEquals(medicinsktArende.getText(), actualComplements.get("complementQuestionId")[0].getMessage());
        assertEquals(medicinsktArende.getText(), actualComplements.get("anotherComplementQuestionId")[0].getMessage());
    }
}

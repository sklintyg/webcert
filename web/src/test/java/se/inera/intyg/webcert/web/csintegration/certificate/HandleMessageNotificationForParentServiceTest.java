package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;

@ExtendWith(MockitoExtension.class)
class HandleMessageNotificationForParentServiceTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String NOT_FK = "notFk";
    private static final String FK = "Försäkringskassan";
    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;
    @Mock
    PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    @InjectMocks
    HandleMessageNotificationForParentService handleMessageNotificationForParentService;

    private static Stream<Arguments> invalidRelations() {
        return Stream.of(
            Arguments.of(CertificateRelations.builder().build()),
            Arguments.of(
                CertificateRelations.builder().parent(
                        CertificateRelation.builder()
                            .type(CertificateRelationType.COPIED)
                            .build()
                    )
                    .build()
            )
        );
    }

    @NullSource
    @MethodSource("invalidRelations")
    @ParameterizedTest
    void shouldNotPublishEventIfRelationsIsInvalid(CertificateRelations relations) {
        handleMessageNotificationForParentService.notify(relations);
        verifyNoInteractions(publishCertificateStatusUpdateService);
    }

    @Test
    void shouldNotPublishEventIfQuestionIsNotRecieved() {
        final var certificateRelations = CertificateRelations.builder()
            .parent(
                CertificateRelation.builder()
                    .type(CertificateRelationType.COMPLEMENTED)
                    .certificateId(CERTIFICATE_ID)
                    .build()
            )
            .build();

        final var questions = List.of(
            Question.builder()
                .author(NOT_FK)
                .build(),
            Question.builder()
                .author(NOT_FK)
                .build()
        );

        when(csIntegrationService.getQuestions(CERTIFICATE_ID)).thenReturn(questions);

        handleMessageNotificationForParentService.notify(certificateRelations);
        verifyNoInteractions(publishCertificateStatusUpdateService);
    }

    @Test
    void shouldPublishEventIfQuestionIsRecieved() {
        final var certificateRelations = CertificateRelations.builder()
            .parent(
                CertificateRelation.builder()
                    .type(CertificateRelationType.COMPLEMENTED)
                    .certificateId(CERTIFICATE_ID)
                    .build()
            )
            .build();

        final var questions = List.of(
            Question.builder()
                .author(FK)
                .build(),
            Question.builder()
                .author(FK)
                .build()
        );

        final var certificate = mock(Certificate.class);

        when(csIntegrationService.getQuestions(CERTIFICATE_ID)).thenReturn(questions);
        when(csIntegrationService.getCertificate(eq(CERTIFICATE_ID), any())).thenReturn(certificate);

        handleMessageNotificationForParentService.notify(certificateRelations);
        verify(publishCertificateStatusUpdateService, times(2)).publish(certificate, HandelsekodEnum.NYFRFM);
    }
}
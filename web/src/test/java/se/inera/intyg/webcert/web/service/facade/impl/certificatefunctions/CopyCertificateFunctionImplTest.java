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

package se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.CertificateFacadeTestHelper;
import se.inera.intyg.webcert.web.service.facade.CertificateTypeMessageService;
import se.inera.intyg.webcert.web.service.facade.impl.CertificateMessage;
import se.inera.intyg.webcert.web.service.facade.impl.CertificateMessageType;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class CopyCertificateFunctionImplTest {

    @Mock
    private CertificateTypeMessageService certificateTypeMessageService;

    @Mock
    private UtkastService utkastService;

    @InjectMocks
    private CopyCertificateFunctionImpl copyCertificateFunction;

    @Test
    void shallIncludeCopyCertificateIfLocked() {
        final var expected = ResourceLinkDTO.create(
            ResourceLinkTypeDTO.COPY_CERTIFICATE,
            "Kopiera",
            "Skapar en redigerbar kopia av utkastet på den enheten du är inloggad på.",
            "<p>"
                + "Genom att kopiera ett låst intygsutkast skapas ett nytt utkast med samma information som i det ursprungliga "
                + "låsta utkastet. Du kan redigera utkastet innan du signerar det. Det ursprungliga låsta utkastet finns kvar."
                + "</p>"
                + "<br/>"
                + "<p>Det nya utkastet skapas på den enhet du är inloggad på.</p>",
            true
        );

        final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.LOCKED);

        final var actual = copyCertificateFunction.get(certificate).orElseThrow();
        assertEquals(expected, actual);
    }

    @Test
    void shallIncludeCopyContinueCertificate() {
        final var expected = ResourceLinkDTO.create(
            ResourceLinkTypeDTO.COPY_CERTIFICATE_CONTINUE,
            "Kopiera",
            "Skapar en redigerbar kopia av utkastet på den enheten du är inloggad på.",
            true
        );

        final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.LOCKED);
        CertificateRelation copied = CertificateRelation.builder().type(CertificateRelationType.COPIED)
            .status(CertificateStatus.UNSIGNED).build();
        final var children = new CertificateRelation[]{copied};
        certificate.getMetadata().setRelations(CertificateRelations.builder().children(children).build());

        final var actual = copyCertificateFunction.get(certificate).orElseThrow();
        assertEquals(expected, actual);
    }

    @Test
    void shallNotIncludeAnyCopyCertificateIfCertificateIsNotLocked() {
        final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);

        final var actual = copyCertificateFunction.get(certificate);
        assertTrue(actual.isEmpty());
    }

    @Test
    void shallIncludeDisabledCopyCertificateIfLockedAndSpecificMessageReturned() {
        final var message = new CertificateMessage(CertificateMessageType.CERTIFICATE_ON_DIFFERENT_CARE_PROVIDER,
            "Specific message for copy description");
        final var expected = ResourceLinkDTO.create(
            ResourceLinkTypeDTO.COPY_CERTIFICATE,
            "Kopiera",
            message.getMessage(),
            false
        );

        doReturn(Optional.of(message)).when(certificateTypeMessageService)
            .get(DbModuleEntryPoint.MODULE_ID, Personnummer.createPersonnummer("191212121212").orElseThrow());

        final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.LOCKED);

        final var actual = copyCertificateFunction.get(certificate).orElseThrow();
        assertEquals(expected, actual);
    }

    @Test
    void shallIncludeEnabledCopyCertificateIfLockedAndDraftOnDifferentCareProviderMesssageType() {
        final var message = new CertificateMessage(CertificateMessageType.DRAFT_ON_DIFFERENT_CARE_PROVIDER,
            "Specific message for copy description");
        final var expected = ResourceLinkDTO.create(
            ResourceLinkTypeDTO.COPY_CERTIFICATE,
            "Kopiera",
            "Skapar en redigerbar kopia av utkastet på den enheten du är inloggad på.",
            String.format("<div class='ic-alert ic-alert--status ic-alert--observe'>\n"
                    + "<i class='ic-alert__icon ic-observe-icon'></i><p>%s</p></div><br/>"
                    + "<p>"
                    + "Genom att kopiera ett låst intygsutkast skapas ett nytt utkast med samma information som i det ursprungliga "
                    + "låsta utkastet. Du kan redigera utkastet innan du signerar det. Det ursprungliga låsta utkastet finns kvar."
                    + "</p>"
                    + "<br/>"
                    + "<p>Det nya utkastet skapas på den enhet du är inloggad på.</p>",
                message.getMessage()),
            true
        );

        doReturn(Optional.of(message)).when(certificateTypeMessageService)
            .get(DbModuleEntryPoint.MODULE_ID, Personnummer.createPersonnummer("191212121212").orElseThrow());

        final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.LOCKED);

        final var actual = copyCertificateFunction.get(certificate).orElseThrow();
        assertEquals(expected, actual);
    }

    @Test
    void shallIncludeEnabledCopyCertificateIfLockedAndCertificateOnDifferentCareProviderMesssageTypeIfDoi() {
        final var message = new CertificateMessage(CertificateMessageType.CERTIFICATE_ON_DIFFERENT_CARE_PROVIDER,
            "Specific message for copy description");
        final var expected = ResourceLinkDTO.create(
            ResourceLinkTypeDTO.COPY_CERTIFICATE,
            "Kopiera",
            "Skapar en redigerbar kopia av utkastet på den enheten du är inloggad på.",
            String.format("<div class='ic-alert ic-alert--status ic-alert--observe'>\n"
                    + "<i class='ic-alert__icon ic-observe-icon'></i><p>%s</p></div><br/>"
                    + "<p>"
                    + "Genom att kopiera ett låst intygsutkast skapas ett nytt utkast med samma information som i det ursprungliga "
                    + "låsta utkastet. Du kan redigera utkastet innan du signerar det. Det ursprungliga låsta utkastet finns kvar."
                    + "</p>"
                    + "<br/>"
                    + "<p>Det nya utkastet skapas på den enhet du är inloggad på.</p>",
                message.getMessage()),
            true
        );

        doReturn(Optional.of(message)).when(certificateTypeMessageService)
            .get(DoiModuleEntryPoint.MODULE_ID, Personnummer.createPersonnummer("191212121212").orElseThrow());

        final var certificate = CertificateFacadeTestHelper.createCertificate(DoiModuleEntryPoint.MODULE_ID, CertificateStatus.LOCKED);

        final var actual = copyCertificateFunction.get(certificate).orElseThrow();
        assertEquals(expected, actual);
    }
}

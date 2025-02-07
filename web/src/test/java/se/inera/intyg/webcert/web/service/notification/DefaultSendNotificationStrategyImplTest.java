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
package se.inera.intyg.webcert.web.service.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;

@RunWith(MockitoJUnitRunner.class)
public class DefaultSendNotificationStrategyImplTest {

    @Mock
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    @InjectMocks
    private DefaultSendNotificationStrategyImpl defaultSendNotificationStrategy;

    @Test
    public void testDecideNotificationForIntygBasedOnDraft() {
        final var unitId = "unitId";
        final var certificateType = "certificateType";
        final var draft = mock(Utkast.class);
        doReturn(unitId).when(draft).getEnhetsId();
        doReturn(certificateType).when(draft).getIntygsTyp();

        final var schemaVersion = SchemaVersion.VERSION_3;
        final var optionalSchemaVersion = Optional.of(schemaVersion);

        doReturn(optionalSchemaVersion).when(integreradeEnheterRegistry).getSchemaVersion(unitId, certificateType);

        final var actualOptionalSchemaVersion = defaultSendNotificationStrategy.decideNotificationForIntyg(draft);

        assertNotNull(actualOptionalSchemaVersion);
        assertEquals(optionalSchemaVersion.isPresent(), actualOptionalSchemaVersion.isPresent());
        assertEquals(optionalSchemaVersion.get(), actualOptionalSchemaVersion.get());
    }

    @Test
    public void testDecideNotificationForIntygBasedOnDraftEmpty() {
        final var unitId = "unitId";
        final var certificateType = "certificateType";
        final var draft = mock(Utkast.class);
        doReturn(unitId).when(draft).getEnhetsId();
        doReturn(certificateType).when(draft).getIntygsTyp();

        final var optionalSchemaVersion = Optional.empty();

        doReturn(optionalSchemaVersion).when(integreradeEnheterRegistry).getSchemaVersion(unitId, certificateType);

        final var actualOptionalSchemaVersion = defaultSendNotificationStrategy.decideNotificationForIntyg(draft);

        assertNotNull(actualOptionalSchemaVersion);
        assertEquals(optionalSchemaVersion.isEmpty(), actualOptionalSchemaVersion.isEmpty());
    }

    @Test
    public void testDecideNotificationForIntygBasedOnCertificate() {
        final var unitId = "unitId";
        final var certificateType = "certificateType";
        final var certificate = mock(Utlatande.class);
        final var baseData = mock(GrundData.class);
        doReturn(baseData).when(certificate).getGrundData();
        final var createdBy = mock(HoSPersonal.class);
        doReturn(createdBy).when(baseData).getSkapadAv();
        final var careUnit = mock(Vardenhet.class);
        doReturn(careUnit).when(createdBy).getVardenhet();
        doReturn(unitId).when(careUnit).getEnhetsid();
        doReturn(certificateType).when(certificate).getTyp();

        final var schemaVersion = SchemaVersion.VERSION_3;
        final var optionalSchemaVersion = Optional.of(schemaVersion);

        doReturn(optionalSchemaVersion).when(integreradeEnheterRegistry).getSchemaVersion(unitId, certificateType);

        final var actualOptionalSchemaVersion = defaultSendNotificationStrategy.decideNotificationForIntyg(certificate);

        assertNotNull(actualOptionalSchemaVersion);
        assertEquals(optionalSchemaVersion.isPresent(), actualOptionalSchemaVersion.isPresent());
        assertEquals(optionalSchemaVersion.get(), actualOptionalSchemaVersion.get());
    }

    @Test
    public void testDecideNotificationForIntygBasedOnCertificateEmpty() {
        final var unitId = "unitId";
        final var certificateType = "certificateType";
        final var certificate = mock(Utlatande.class);
        final var baseData = mock(GrundData.class);
        doReturn(baseData).when(certificate).getGrundData();
        final var createdBy = mock(HoSPersonal.class);
        doReturn(createdBy).when(baseData).getSkapadAv();
        final var careUnit = mock(Vardenhet.class);
        doReturn(careUnit).when(createdBy).getVardenhet();
        doReturn(unitId).when(careUnit).getEnhetsid();
        doReturn(certificateType).when(certificate).getTyp();

        final var optionalSchemaVersion = Optional.empty();

        doReturn(optionalSchemaVersion).when(integreradeEnheterRegistry).getSchemaVersion(unitId, certificateType);

        final var actualOptionalSchemaVersion = defaultSendNotificationStrategy.decideNotificationForIntyg(certificate);

        assertNotNull(actualOptionalSchemaVersion);
        assertEquals(optionalSchemaVersion.isEmpty(), actualOptionalSchemaVersion.isEmpty());
    }
}

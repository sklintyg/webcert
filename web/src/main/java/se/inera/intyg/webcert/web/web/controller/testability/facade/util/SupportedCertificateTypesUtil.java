/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.web.controller.testability.facade.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.af00213.support.Af00213EntryPoint;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CertificateType;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateFillType;

@Component
public class SupportedCertificateTypesUtil {

    public List<CertificateType> get() {
        final var certificateTypes = new ArrayList<CertificateType>();
        certificateTypes.add(
            new CertificateType(
                Af00213EntryPoint.ISSUER_TYPE_ID,
                Af00213EntryPoint.MODULE_ID,
                Af00213EntryPoint.MODULE_NAME,
                Collections.singletonList("1.0"),
                Arrays.asList(CertificateStatus.UNSIGNED, CertificateStatus.SIGNED, CertificateStatus.LOCKED),
                Arrays.asList(CreateCertificateFillType.EMPTY, CreateCertificateFillType.MINIMAL)
            )
        );
        certificateTypes.add(
            new CertificateType(
                LisjpEntryPoint.ISSUER_TYPE_ID,
                LisjpEntryPoint.MODULE_ID,
                LisjpEntryPoint.MODULE_NAME,
                Arrays.asList("1.0", "1.1", "1.2"),
                Arrays.asList(CertificateStatus.UNSIGNED, CertificateStatus.SIGNED, CertificateStatus.LOCKED),
                Arrays.asList(CreateCertificateFillType.EMPTY, CreateCertificateFillType.MINIMAL)
            )
        );
        certificateTypes.add(
            new CertificateType(
                Ag7804EntryPoint.ISSUER_TYPE_ID,
                Ag7804EntryPoint.MODULE_ID,
                Ag7804EntryPoint.MODULE_NAME,
                Arrays.asList("1.0", "1.1"),
                Arrays.asList(CertificateStatus.UNSIGNED, CertificateStatus.SIGNED, CertificateStatus.LOCKED),
                Arrays.asList(CreateCertificateFillType.EMPTY, CreateCertificateFillType.MINIMAL)
            )
        );
        return certificateTypes;
    }
}

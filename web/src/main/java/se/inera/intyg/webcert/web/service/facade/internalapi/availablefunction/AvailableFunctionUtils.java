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

package se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction;

import java.util.Arrays;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValue;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueBoolean;

public class AvailableFunctionUtils {

    private AvailableFunctionUtils() {

    }

    public static boolean isReplacedOrComplemented(CertificateRelations relations) {
        if (relations == null || relations.getChildren() == null) {
            return false;
        }

        final var typesToFilter = Arrays.asList(CertificateRelationType.COMPLEMENTED, CertificateRelationType.REPLACED);
        return Arrays.stream(relations.getChildren())
            .filter(child -> typesToFilter.contains(child.getType()))
            .anyMatch(child -> CertificateStatus.SIGNED.equals(child.getStatus()));
    }

    public static boolean isCertificateOfType(Certificate certificate, String type) {
        return certificate.getMetadata().getType().equals(type);
    }

    public static CertificateDataValue getQuestionValue(Certificate certificate, String id) {
        return certificate.getData().get(id).getValue();
    }

    public static boolean hasQuestion(Certificate certificate, String id) {
        return certificate.getData().containsKey(id);
    }

    public static boolean isBooleanValueTrue(CertificateDataValueBoolean value) {
        return value != null && value.getSelected() != null && value.getSelected();
    }

    public static boolean isBooleanValueNullOrFalse(CertificateDataValueBoolean value) {
        return value == null || value.getSelected() == null || !value.getSelected();
    }

}

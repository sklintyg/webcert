package se.inera.intyg.webcert.web.service.facade.util;

import java.util.List;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

public abstract class CertificateStatusConverter {

    public static CertificateStatus getStatus(boolean isRevoked, UtkastStatus status) {
        if (isRevoked) {
            if (status == UtkastStatus.DRAFT_LOCKED) {
                return CertificateStatus.LOCKED_REVOKED;
            } else {
                return CertificateStatus.REVOKED;
            }
        }

        switch (status) {
            case SIGNED:
                return CertificateStatus.SIGNED;
            case DRAFT_LOCKED:
                return CertificateStatus.LOCKED;
            case DRAFT_COMPLETE:
            case DRAFT_INCOMPLETE:
                return CertificateStatus.UNSIGNED;
            default:
                throw new IllegalArgumentException("Cannot map the status: " + status);
        }
    }

    public static CertificateStatus getStatus(List<Status> statuses) {
        if (statuses.stream().anyMatch(status -> status.getType() == CertificateState.CANCELLED)) {
            return CertificateStatus.REVOKED;
        }
        return CertificateStatus.SIGNED;
    }

    public static boolean isRevoked(Utkast certificate) {
        return certificate.getAterkalladDatum() != null;
    }
}

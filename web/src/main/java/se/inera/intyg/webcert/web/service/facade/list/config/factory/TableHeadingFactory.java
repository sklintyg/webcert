package se.inera.intyg.webcert.web.service.facade.list.config.factory;

import se.inera.intyg.webcert.web.service.facade.list.config.dto.CertificateListItemValueType;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListColumnType;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.TableHeading;

public class TableHeadingFactory {
    public static TableHeading text(ListColumnType type) {
        return new TableHeading(type, type.getName(), CertificateListItemValueType.TEXT);
    }

    public static TableHeading date(ListColumnType type) {
        return new TableHeading(type, type.getName(), CertificateListItemValueType.DATE);
    }

    public static TableHeading patientInfo(ListColumnType type) {
        return new TableHeading(type, type.getName(), CertificateListItemValueType.PATIENT_INFO);
    }

    public static TableHeading forwarded(ListColumnType type) {
        return new TableHeading(type, type.getName(), CertificateListItemValueType.FORWARD);
    }

    public static TableHeading openButton(ListColumnType type) {
        return new TableHeading(type, type.getName(), CertificateListItemValueType.OPEN_BUTTON);
    }
}

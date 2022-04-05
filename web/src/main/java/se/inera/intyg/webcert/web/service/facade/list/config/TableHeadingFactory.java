package se.inera.intyg.webcert.web.service.facade.list.config;

public class TableHeadingFactory {
    public static TableHeadingDTO text(ListColumnTypeDTO type) {
        return new TableHeadingDTO(type, type.getName(), CertificateListItemValueType.TEXT);
    }

    public static TableHeadingDTO date(ListColumnTypeDTO type) {
        return new TableHeadingDTO(type, type.getName(), CertificateListItemValueType.DATE);
    }

    public static TableHeadingDTO patientInfo(ListColumnTypeDTO type) {
        return new TableHeadingDTO(type, type.getName(), CertificateListItemValueType.PATIENT_INFO);
    }

    public static TableHeadingDTO forwarded(ListColumnTypeDTO type) {
        return new TableHeadingDTO(type, type.getName(), CertificateListItemValueType.FORWARD);
    }

    public static TableHeadingDTO openButton(ListColumnTypeDTO type) {
        return new TableHeadingDTO(type, type.getName(), CertificateListItemValueType.OPEN_BUTTON);
    }
}

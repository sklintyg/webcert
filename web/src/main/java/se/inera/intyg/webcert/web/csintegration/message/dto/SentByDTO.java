package se.inera.intyg.webcert.web.csintegration.message.dto;

import lombok.Getter;

@Getter
public enum SentByDTO {
    FK("FKASSA"), WC("HSVARD");

    private final String code;

    SentByDTO(String code) {
        this.code = code;
    }

    public static SentByDTO getByCode(String code) {
        for (SentByDTO sentByDTO : values()) {
            if (sentByDTO.getCode().equals(code)) {
                return sentByDTO;
            }
        }
        throw new IllegalArgumentException(
            String.format("No matching code found for '%s'", code)
        );
    }
}

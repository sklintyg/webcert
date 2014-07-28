package se.inera.webcert.service.intyg.dto;

import se.inera.certificate.model.Utlatande;

import com.fasterxml.jackson.annotation.JsonRawValue;

public class IntygContentHolder {

    @JsonRawValue
    private final String contents;

    private final IntygMetadata metaData;

    private final Utlatande externalModel;

    public IntygContentHolder(String contents, IntygMetadata metaData) {
        super();
        this.contents = contents;
        this.metaData = metaData;
        this.externalModel = null;
    }

    public IntygContentHolder(String contents, Utlatande externalModel, IntygMetadata metaData) {
        super();
        this.contents = contents;
        this.metaData = metaData;
        this.externalModel = externalModel;
    }

    public String getContents() {
        return contents;
    }

    public IntygMetadata getMetaData() {
        return metaData;
    }

    public Utlatande getExternalModel() {
        return externalModel;
    }

}

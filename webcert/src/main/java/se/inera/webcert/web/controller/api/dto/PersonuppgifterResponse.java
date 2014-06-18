package se.inera.webcert.web.controller.api.dto;

public class PersonuppgifterResponse {

    public enum Status {
        FOUND, NOT_FOUND
    }

    private Status status;
    private Personuppgifter personuppgifter;

    public PersonuppgifterResponse(Status status) {
        this.status = status;
    }

    public PersonuppgifterResponse(Status status, Personuppgifter personuppgifter) {
        this.status = status;
        this.personuppgifter = personuppgifter;
    }

    public Status getStatus() {
        return status;
    }

    public Personuppgifter getPersonuppgifter() {
        return personuppgifter;
    }
}

package se.inera.webcert.web.controller.api.dto;

import se.inera.webcert.pu.model.Person;

public class PersonuppgifterResponse {

    public enum Status {
        FOUND, NOT_FOUND
    }

    private Status status;
    private Person person;

    public PersonuppgifterResponse(Status status) {
        this.status = status;
    }

    public PersonuppgifterResponse(Status status, Person person) {
        this.status = status;
        this.person = person;
    }

    public Status getStatus() {
        return status;
    }

    public Person getPerson() {
        return person;
    }
}

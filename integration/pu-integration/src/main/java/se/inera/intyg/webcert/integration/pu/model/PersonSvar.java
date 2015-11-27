package se.inera.intyg.webcert.integration.pu.model;

public class PersonSvar {

    public enum Status {
        FOUND, NOT_FOUND, ERROR
    }

    private final Person person;
    private final Status status;

    public PersonSvar(Person person, Status status) {
        this.person = person;
        this.status = status;
    }

    public PersonSvar(PersonSvar personSvar) {
        this.person = personSvar.person;
        this.status = personSvar.status;
    }

    public Person getPerson() {
        return person;
    }

    public Status getStatus() {
        return status;
    }
}

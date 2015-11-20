package se.inera.intyg.webcert.web.auth.eleg;

/**
 * Fake container for approx. CGI SAML ticket attributes.
 *
 * Created by eriklupander on 2015-06-16.
 */
public class FakeElegCredentials {

    // Subject_SerialNumber
    private String personId;

    // Subject_GivenName
    private String firstName;

    // Subject_Surname
    private String lastName;

    private boolean privatLakare;

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isPrivatLakare() {
        return privatLakare;
    }

    public void setPrivatLakare(boolean privatLakare) {
        this.privatLakare = privatLakare;
    }
}

package se.inera.intyg.webcert.common.common;

/**
 * Defines a number of global constants in Webcert, e.g. constants that may be used across several subprojects.
 *
 * Created by eriklupander on 2015-05-22.
 */
public final class Constants {

    private Constants() {
    }

    /** Messaging. */
    public static final String STORE_MESSAGE = "STORE";
    public static final String SEND_MESSAGE = "SEND";
    public static final String REVOKE_MESSAGE = "REVOKE";
    public static final String MESSAGE_TYPE = "MESSAGE_TYPE";

    public static final String INTYGS_ID = "INTYGS_ID";
    public static final String INTYGS_TYP = "INTYGS_TYP";
    public static final String LOGICAL_ADDRESS = "LOGICAL_ADDRESS";
    public static final String PERSON_ID = "PERSON_ID";
    public static final String RECIPIENT = "RECIPIENT";

    public static final String JMSX_GROUP_ID = "JMSXGroupID";
    public static final String JMS_REDELIVERED = "JMSRedelivered";

}

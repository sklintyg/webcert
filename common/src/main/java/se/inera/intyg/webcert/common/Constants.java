/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.common;

/**
 * Defines a number of global constants in Webcert, e.g. constants that may be used across several subprojects.
 *
 * Created by eriklupander on 2015-05-22.
 */
public final class Constants {

    /** Messaging. */
    public static final String STORE_MESSAGE = "STORE";
    public static final String SEND_MESSAGE = "SEND";
    public static final String REVOKE_MESSAGE = "REVOKE";
    public static final String SEND_MESSAGE_TO_RECIPIENT = "SEND_MESSAGE_TO_RECIPIENT";
    public static final String REGISTER_APPROVED_RECEIVERS_MESSAGE = "REGISTER_APPROVED_RECEIVERS";
    public static final String MESSAGE_TYPE = "MESSAGE_TYPE";

    public static final String INTYGS_ID = "INTYGS_ID";
    public static final String INTYGS_TYP = "INTYGS_TYP";
    public static final String LOGICAL_ADDRESS = "LOGICAL_ADDRESS";
    public static final String PERSON_ID = "PERSON_ID";
    public static final String RECIPIENT = "RECIPIENT";

    public static final String JMSX_GROUP_ID = "JMSXGroupID";
    public static final String JMSX_GROUP_SEQ = "JMSXGroupSeq";
    public static final String JMS_REDELIVERED = "JMSRedelivered";

    public static final String DELAY_MESSAGE = "DELAY_MESSAGE";

    private Constants() {
    }

}

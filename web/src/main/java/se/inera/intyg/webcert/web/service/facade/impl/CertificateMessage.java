/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.impl;

import java.util.Objects;

public class CertificateMessage {

    private final CertificateMessageType messageType;
    private final String message;

    public CertificateMessage(CertificateMessageType messageType, String message) {
        this.messageType = messageType;
        this.message = message;
    }

    public CertificateMessageType getMessageType() {
        return messageType;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CertificateMessage that = (CertificateMessage) o;
        return messageType == that.messageType && message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageType, message);
    }

    @Override
    public String toString() {
        return "CertificateMessage{"
            + "messageType=" + messageType
            + ", message='" + message + '\''
            + '}';
    }
}

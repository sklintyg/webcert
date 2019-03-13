/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.common.client.converter;

import javax.xml.bind.JAXBElement;

import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.ObjectFactory;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientType;

public final class SendMessageToRecipientTypeConverter {

    private SendMessageToRecipientTypeConverter() {
    }

    public static String toXml(SendMessageToRecipientType request) {
        JAXBElement<SendMessageToRecipientType> requestElement = new ObjectFactory()
                .createSendMessageToRecipient(request);
        return XmlMarshallerHelper.marshal(requestElement);
    }

    public static SendMessageToRecipientType fromXml(String xml) {
        JAXBElement<SendMessageToRecipientType> unmarshalledObject = XmlMarshallerHelper.unmarshal(xml);
        return unmarshalledObject.getValue();
    }
}

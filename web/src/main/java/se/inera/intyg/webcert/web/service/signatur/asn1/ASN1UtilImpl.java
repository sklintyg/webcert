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
package se.inera.intyg.webcert.web.service.signatur.asn1;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.pkcs.ContentInfo;
import org.bouncycastle.asn1.pkcs.SignedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Provides a mechanism to parse the value of a x.520 DN serial from
 * an ASN.1 container. Typical use is to extract information from a
 * NetID signature.
 *
 * Created by eriklupander on 2015-09-04.
 */
@Component
public class ASN1UtilImpl implements ASN1Util {

    private static final Logger LOG = LoggerFactory.getLogger(ASN1UtilImpl.class);

    @Override
    public String getValue(String identifier, InputStream asn1Signature) {
        ByteArrayInputStream bais = null;
        ASN1InputStream asn1InputStream = null;
        try {
            bais = convertStream(asn1Signature);
            asn1InputStream = new ASN1InputStream(bais);
            DERObject obj = asn1InputStream.readObject();
            ContentInfo contentInfo = ContentInfo.getInstance(obj);

            // Extract certificates
            SignedData signedData = SignedData.getInstance(contentInfo.getContent());
            return findInCertificate(identifier, (DERObject) signedData.getCertificates().getObjectAt(0));
        } catch (IOException e) {
            LOG.error("Error parsing signature: {}", e.getMessage());
            throw new IllegalStateException(e);
        } finally {
            IOUtils.closeQuietly(bais);
            IOUtils.closeQuietly(asn1InputStream);
        }
    }

    private ByteArrayInputStream convertStream(InputStream signatureIn) throws IOException {
        byte[] bytes = IOUtils.toByteArray(signatureIn);
        IOUtils.closeQuietly(signatureIn);
        byte[] decoded = Base64.decodeBase64(bytes);
        return new ByteArrayInputStream(decoded);
    }

    private String findInCertificate(String identifier, DERObject certObj) {
        String value = null;
        if (certObj instanceof DERSet) {
            value = handleDERSet(identifier, (DERSet) certObj);
        }
        if (certObj instanceof DERSequence) {
            value = handleDERSequence(identifier, (DERSequence) certObj);
        }
        return value;
    }

    private String handleDERSequence(String identifier, DERSequence seq) {
        String value = null;
        for (int a = 0; a < seq.size() && value == null; a++) {
            DERObject obj = seq.getObjectAt(a).getDERObject();
            if (obj instanceof ASN1ObjectIdentifier) {
                ASN1ObjectIdentifier objectIdentifier = (ASN1ObjectIdentifier) obj;
                if (objectIdentifier.getId().equals(identifier)) {
                    value = seq.getObjectAt(a + 1).toString();
                    break;
                }
            } else {
                value = findInCertificate(identifier, obj);
            }
        }
        return value;
    }

    private String handleDERSet(String identifier, DERSet set) {
        String value = null;
        for (int a = 0; a < set.size() && value == null; a++) {
            DERObject obj = set.getObjectAt(a).getDERObject();
            value = findInCertificate(identifier, obj);
        }
        return value;
    }
}

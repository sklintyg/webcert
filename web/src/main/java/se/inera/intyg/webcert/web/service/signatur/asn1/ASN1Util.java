package se.inera.intyg.webcert.web.service.signatur.asn1;

import java.io.InputStream;

/**
 * A declarative interface for accessing data within an ASN.1 container.
 *
 * Created by eriklupander on 2015-09-04.
 */
public interface ASN1Util {

    String parsePersonId(InputStream asn1Signature);

    String parseHsaId(InputStream asn1Signature);
}

package se.inera.intyg.webcert.web.service.signatur.asn1;

/**
 * Created by eriklupander on 2017-06-02.
 */
public class ASN1Validator {
    public boolean validate() {
//        CertificateFactory cf = CertificateFactory.getInstance("X.509");
//
//        // Get ContentInfo
//        // byte[] signature = ... // PKCS#7 signature bytes
//        InputStream signatureIn = new ClassPathResource("netid-siths-sig.txt").getInputStream();
//        byte[] bytes = IOUtils.toByteArray(signatureIn);
//        byte[] decoded = Base64.decodeBase64(bytes);
//        InputStream signatureIn2 = new ByteArrayInputStream(decoded);
//        DERObject obj = new ASN1InputStream(signatureIn2).readObject();
//        ContentInfo contentInfo = ContentInfo.getInstance(obj);
//
//        // Extract certificates
//        SignedData signedData = SignedData.getInstance(contentInfo.getContent());
//        Enumeration certificates = signedData.getCertificates().getObjects();
//
//        // Build certificate path
//        List certList = new ArrayList();
//        while (certificates.hasMoreElements()) {
//            DERObject certObj = (DERObject) certificates.nextElement();
//            dumpCert(certObj);
//            InputStream in = new ByteArrayInputStream(certObj.getDEREncoded());
//            certList.add(cf.generateCertificate(in));
//        }
//        CertPath certPath = cf.generateCertPath(certList);
//        Certificate certificate = certPath.getCertificates().get(0);
//  //      assertNotNull(get(certificate));
//
//        // Load key store
//        // String keyStorePath = ...
//        KeyStore keyStore = KeyStore.getInstance("JKS");
//        keyStore.load(new FileInputStream("/Users/eriklupander/intyg/webcert-konfiguration/test/certifikat/truststore.jks"),
//                "password".toCharArray());
//
//        // Set validation parameters
//        // TrustAnchor trustAnchor = new TrustAnchor();
//        PKIXParameters params = new PKIXParameters(keyStore);
//        params.setRevocationEnabled(false); // to avoid exception on empty CRL
//
//        // Validate certificate path
//        CertPathValidator validator = CertPathValidator.getInstance("PKIX");
//        try {
//            CertPathValidatorResult result = validator.validate(certPath, params);
//        } catch (CertPathValidatorException e) {
//            e.printStackTrace();
//        } catch (InvalidAlgorithmParameterException e) {
//            e.printStackTrace();
//        }
        return true;
    }
}

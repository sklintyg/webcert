# XML Digital Signature i Webcert

Från Webcert 6.1 så har signeringarna i Webcert gjorts om från grunden. Vi får skilja på Signatur-typ och Signeringsmetod:

### Signeringsmetoder
- NetiD-plugin
    - SITHS-kort
    - EFOS-kort
    - Telia e-legitimation
- Global Relaying Protocol (CGI)
    - BankID
    - Mobilt BankID
- NetiD Access Server (NIAS)
    - EFOS-kort
    - SITHS-kort
    - Telia e-leg?

### Signaturtyper
- XML Digital Signature
- PKCS#7
- Legacy

##### XML Digital Signature
Från Webcert 6.1 är detta vår huvudsakliga signeringsmetod, där varje intyg som skickas till Intygstjänsten medelst RegisterCertificate v3.2 eller senare och som signeras mha NetiD-plugin eller NetiD Access Server.

Kortfattat så förses XML-kroppen för ett RegisterCertificate med ett nytt <Signature>-element som är barn till <Intyg>-elementet:

    <?xml version="1.0" encoding="UTF-8"?>
    <ns2:RegisterCertificate xmlns:ns2="urn:riv:cl....">
        <ns2:intyg>
            <intygs-id>
                <ns3:root>TSTNMT2321000156-1077</ns3:root>
                <ns3:extension>9f02dd2f-f57c-4a73-8190-2fe602cd6e27</ns3:extension>
            </intygs-id>
            ...
            <svar id="1">
                ...
            </svar>
            <Signature>
                <ns2:SignedInfo>
                    ...
                </ns2:SignedInfo>
            </Signature>
        </ns2:intyg>
    </ns2:RegisterCertificate>

En XMLDSig-signatur består av ett antal element som alla är mer eller mindre viktiga för att kunna komma fram till en validerbar signatur:

- En _digest_ av intygsinnehållet.
- En _digest_ SignedInfo-elementet som beskriver algoritmer och hur XML-dokumentet signaturen befinner sig i skall transformeras för att komma fram till en kanonisk XML-representation av det man vill signera.
- Ett KeyInfo-element som innehåller den publika nyckeln tillhörande den privata nyckel som signerade intyg.

###### Kanonisering av ett intyg

Inom intygstjänster har vi tagit fram en serie av transformeringar och XPath-uttryck som används inom signaturens <Transforms>-element för att omvandla en godtycklig XML som innehåller ett _<intyg ns2="...">..<Signature/></intyg>_ till en _portabel_ och _kanonisk_ XML:

1. Ange att det rör sig om en _Enveloped_ signature, vilket innebär att själva <Signature>-elementet undantas från det signerade innehållet. Annars hade man givetvis hamnat i en hönan & ägget-situation. Sker mha:

    
    <ns2:Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/>
    
2. Städa bort samtliga namespaces ur dokumentet. Detta sker mha en _XSLT-transformation_ som kör local(.) på samtliga element. Detta tar bort såväl namespace-prefix som namespaces.


    TODO XSLT

3. Plocka ut _enbart_ <intyg>-elementet ur dokumentet baserat på dess underliggande intygs-id. Detta sker mha XPath Filter 2.0 och _intersect_ enligt: 


    <ns2:Transform Algorithm="http://www.w3.org/2002/06/xmldsig-filter2"><XPath Filter="intersect">//extension[text()='9f02dd2f-f57c-4a73-8190-2fe602cd6e27']/../..</XPath></ns2:Transform>

4. Plocka bort oönskade metadata-element av oönskad karaktär ur <intyg>. Detta är saker som tyvärr ligger i <intyg> men som egentligen är av metadata-karaktär. Relationer, Statusar, skickadTidpunkt och (ev.) eventuellt förekommande patientnamn/adresser.


    <ns2:Transform Algorithm="http://www.w3.org/2002/06/xmldsig-filter2"><XPath Filter="subtract">//*[local-name() = 'skickatTidpunkt']|//*[local-name() = 'relation']|//*[local-name() = 'status']</XPath></ns2:Transform>
    
5. Slutligen skall XML:en som kvarstår kanoniseras XML-mässigt mha standardiserad c14n-exclusive:

    
    <ns2:Transform Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/></ns2:Transforms>
    
Dessutom måste **samtliga** radbrytningar tas bort. 

Ovanstående fem steg transformerar alltså motsv:

    <ns2:RegisterCertificate xmlns:ns2="urn:riv:cl....">
            <ns2:intyg>
                <intygs-id>
                    <ns3:root>TSTNMT2321000156-1077</ns3:root>
                    <ns3:extension>9f02dd2f-f57c-4a73-8190-2fe602cd6e27</ns3:extension>
                </intygs-id>
                <relation></relation>
                <skickadTidpunkt>2018-06-18T13:37:00</skickadTidpunkt>
                <svar id="1">
                    ...
                </svar>
                <Signature>
                    <ns2:SignedInfo>
                        ...
                    </ns2:SignedInfo>
                </Signature>
            </ns2:intyg>
        </ns2:RegisterCertificate>
        
till:

    <intyg><intygs-id><root>TSTNMT2321000156-1077</root><extension>9f02dd2f-f57c-4a73-8190-2fe602cd6e27</extension></intygs-id><relation></relation><skickadTidpunkt>2018-06-18T13:37:00</skickadTidpunkt><svar id="1">...</svar></intyg>
 
Ovanstående XML (givetvis inkl allt jag inte tagit med av utrymmesskäl) är det som man sedan kör en SHA-256 digest av och placerar i signaturens <DigestValue>-element.

När valideringen av en Signatur kör så försöker valideringskod enligt XMLDSig-specen köra transformeringarna i punkt 1-5 ovan på dokumentet som innehåller en signatur, och komma fram till _exakt_ samma digest-värde som vi själva angivit i signaturen.

Del 2 av en validering är själva signaturen. Signaturer fungerar av tre beståndsdelar:

1. Det man signerar på, i det här fallet signaturens <SignedInfo>-element som _i sig_ innehåller det <DigestValue> vi räknade fram ovan, samtliga transforms och angivna algoritmer för digest, signatur etc.
2. En privat nyckel som står för själva signaturen
3. En algoritm, i vårt fall _http://www.w3.org/2001/04/xmldsig-more#rsa-sha256_.
      
Värdet från ovanstående stoppas in i signaturens <SignatureValue>-element. Den tillhörade publika nyckeln stoppas in i form av ett X509-certifikat i signaturens <KeyInfo>-element.
      
                       
##### PKCS#7
Detta är egentligen ingen signatur utan ett Authenticate-svar från BankID-systemet (GRP). Innehåller en ASN.1-kodad "container" med bl.a. Subject.SerialNumber som identifierar personnumret för den som signerade. Används _endast_ av Privatläkare.

##### Legacy
Detta är samtliga existerande signaturer utförda av landstingsläkare (SITHS-kort). Denna typ av signatur har enbart skrivits som en blob till tabellen SIGNATUR i Base64-kodat format. Även signaturer utförda med Telia e-legitimation av privatläkare tillhör den här typen.

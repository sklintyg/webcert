# Webcert
Webcert är en webbtjänst för att författa intyg samt ställa frågor och svar kring dem.

## Kom igång
Här hittar du grundläggande instruktioner för hur man kommer igång med projektet. Mer detaljerade instruktioner för att sätta upp sin utvecklingsmiljö och liknande hittar du på projektets [Wiki för utveckling](https://github.com/sklintyg/common/wiki).

### Bygg projektet
Webcert byggs med hjälp av Maven enligt följande:

    $ git clone https://github.com/sklintyg/webcert.git
    $ cd webcert
    $ ./gradlew clean build install -PcodeQuality
``

### Starta webbapplikationen
Webbapplikationen kan startas med Jetty enligt följande:

    $ cd webcert
    $ ./gradlew appRun
    $ open http://localhost:9088/welcome.html

Detta startar Webcert med stubbar för alla externa tjänster som Webcert använder. För att köra både Mina intyg och Webcert samtidigt behöver [Intygstjänsten](https://github.com/sklintyg/intygstjanst) startas före Mina intyg och Webcert.

### Starta webbapplikationen i debugläge
För att starta applikationen i debugläge används:

    $ cd webcert
    $ ./gradlew appRunDebug
    
Applikationen kommer då att starta upp med debugPort = **5007**. Det är denna port du ska använda när du sätter upp din 
debug-konfiguration i din utvecklingsmiljö.

### Starta specifik version
Man kan även starta Webcert i ett läge där endast de funktioner som är tillgängliga i en viss version är tillgängliga.

    $ ./gradlew appRun -Pv3.0

### Visa databasen
Man kan även komma åt H2-databasen som startas:

    $ open http://localhost:9090/

För att komma åt Webcert eller Intygsdatabasen fyll i JDBC URL'n :

WebCert 

    JDBC URL : jdbc:h2:tcp://localhost:9094/mem:dataSource

Intyg

    JDBC URL : jdbc:h2:tcp://localhost:9092/mem:dataSource

### Köra flera Webcert-instanser för dev-ändamål
(Detta avsnitt var skrivet specifikt för Maven, behöver uppdateras med gradle-instruktioner)

### Köra lokal webcert med fungerande testinloggning med BankID / Mobilt BankID
OBS! Kräver antingen kortläsare + kort samt testmodifierad BankID säkerhetsprogram _eller_ testklient för Mobilt BankID på telefon/platta med installerat testcertifikat

##### Testklient BankID
https://www.bankid.com/assets/bankid/rp/how-to-get-bankid-for-test-v1.4.pdf

##### Testklient Mobilt BankID
1. Gå hit: https://www.bankid.com/bankid-i-dina-tjanster/rp-info
2. Ladda hem APK: https://www.bankid.com/assets/bankid/rp/BankID_7.5.0_BGC_CUSTOMERTEST.apk (obs, länken ändras över tid)
3. Mejla APKn till din telefon eller motsvarande, installera. Du lär behöva ha satt telefonen i läget att man får installera okänd mjukvara.
4. https://www.bankid.com/assets/bankid/rp/how-to-get-bankid-for-test-v1.5.pdf är rörig värre, men där beskrivs hur man skaffar ett testcertifikat.

Ungefär så här:

1. Gå till https://demo.bankid.com/
2. Logga in med ditt _riktiga_ BankID / Mobila BankID
3. Välj Issue test BankID

![bild1](docs/images/bankid1.png)

4. Knappa in Frida Kranstege 197705232382

![bild2](docs/images/bankid2.png)

5. Följ instruktionerna. Du kommer behöva öppna Mobilt BankID-appen på testtelefonen, knappa in Fridas (eller annan hittepå-persons) personnummer samt den kod som visas på skärmen. Välj en kod (jag kör på 123654) och sen är det faktiskt klart!

##### Konfigurera Webcert

1. Se till att du har /webcert-konfiguration clonat och avkrypterat.

2. Öppna web/build.gradle och kommentera bort hela stycket med:

    jvmArgs = ["-Dcatalina.base=${buildDir}/catalina.base",
                   "-Dspring.profiles.active=dev,caching-enabled",
                   "-Dresources.folder=${projectDir}/../src/main/resources",
                   "-Dcredentials.file=${projectDir}/webcert-credentials.properties",
                   "-Dconfig.file=${projectDir}/webcert-dev.properties",
                   "-Dlogback.file=${projectDir}/webcert-logback.xml",
                   "-DuseMinifiedJavaScript=${minified}",
                   "-Djetty.port=9088"]
                   
Ersätt ovanstående med nedanstående, byt ut _/Users/myuser/intyg_ mot egen absolut sökväg:

    jvmArgs = ["-Dcatalina.base=${buildDir}/catalina.base",
                   "-Dspring.profiles.active=dev,caching-enabled,wc-security-test",
                   "-Dconfig.folder=/Users/myuser/intyg/webcert-konfiguration/test/",
                   "-Dresources.folder=${projectDir}/../src/main/resources",
                   "-Dcredentials.file=/Users/myuser/intyg/webcert-konfiguration/test/credentials.properties",
                   "-Dconfig.file=/Users/myuser/intyg/webcert-konfiguration/test/webcert.properties",
                   "-Dlogback.file=/Users/myuser/intyg/webcert-konfiguration/test/webcert-logback.xml",
                   "-DuseMinifiedJavaScript=${minified}",
                   "-Djetty.port=9088",
                   "-Dwebcert.stubs.port=9088"]
                   
3. Modifiera /webcert-konfiguration/test/sp-eleg.xml så AssertionConsumerService Location pekar på ditt lokala LAN-ip. Ta reda på det mha _ifconfig_ eller motsvarande. I det här exemplet har jag LAN-ip 192.168.0.180

Ändra sp-eleg.xml så Location pekar på din lokala webcert via LAN-ip. Glöm inte porten. Kör du med _grunt server_ peka gärna på 9089:

    <?xml version="1.0" encoding="UTF-8"?>
    <md:EntityDescriptor xmlns:md="urn:oasis:names:tc:SAML:2.0:metadata" ID="webcert.intygstjanster.inera.se" entityID="eleg">
      <md:SPSSODescriptor AuthnRequestsSigned="false" WantAssertionsSigned="false" protocolSupportEnumeration="urn:oasis:names:tc:SAML:2.0:protocol">
    
        <md:SingleLogoutService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST" Location="http://192.168.0.180:9089/saml/SingleLogout/alias/eleg"/>
        <md:SingleLogoutService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect" Location="http://192.168.0.180:9089/saml/SingleLogout/alias/eleg"/>
        <md:NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress</md:NameIDFormat>
        <md:NameIDFormat>urn:oasis:names:tc:SAML:2.0:nameid-format:transient</md:NameIDFormat>
        <md:NameIDFormat>urn:oasis:names:tc:SAML:2.0:nameid-format:persistent</md:NameIDFormat>
        <md:NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified</md:NameIDFormat>
        <md:NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName</md:NameIDFormat>
        <md:AssertionConsumerService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST" Location="http://192.168.0.180:9089/saml/SSO/alias/eleg" index="0" isDefault="true"/>
        <md:AssertionConsumerService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Artifact" Location="http://192.168.0.180:9089/saml/SSO/alias/eleg" index="1"/>
      </md:SPSSODescriptor>
    </md:EntityDescriptor>
    
4. Starta webcert från /webcert/web

    ./gradlew appRun
    
5. Knappa in LAN-adressen till Webcert i din webbläsares adressfält, t.ex: http://192.168.0.180:9089/

Klicka på "E-legitimation" och logga in mha BankID eller Mobilt BankID

### Testa SAML-inloggning via extern test-IDP
Det går att konfigurera Webcert så man kan logga in via formuläret på https://stubidp.sustainsys.com/

![bild sustainsys](docs/images/sustainsys.png)

Man behöver i så fall konfigurera Webcert att nyttja sustainsys stub-idp:

##### Kör lokal redis

På mac i valfritt terminalfönster:

    > redis-server
    
##### Kör lokal ActiveMQ (valfritt)
För att bli av med onödig loggning kan man starta en ActiveMQ lokalt:

    > apache-activemq-5.13.0/bin> ./activemq start

##### web/build.gradle
Ändra gretty-configen

    jvmArgs = ["-Dcatalina.base=${buildDir}/catalina.base",
           "-Dspring.profiles.active=test,wc-all-stubs,caching-enabled,wc-security-test,testability-api",
           "-Dresources.folder=${projectDir}/../src/main/resources",
           "-Dcredentials.file=${projectDir}/../../webcert-konfiguration/authtest/credentials.properties",
           "-Dconfig.file=${projectDir}/../../webcert-konfiguration/authtest/webcert.properties",
           "-Dconfig.folder=${projectDir}/../../webcert-konfiguration/authtest/",
           "-Dlogback.file=classpath:logback-dev.xml",
           "-DuseMinifiedJavaScript=${minified}",
           "-Dwebcert.stubs.port=9088",
           "-Ddb.httpPort=9090",
           "-Djetty.port=${httpPort}"]
           
##### Checka ut speciellt konfig-repo
I webcert-konfiguration

    git checkout feature/INTYG-7292
    
Det skall ligga en /authtest mapp där. Eventuellt behöver du köra git-crypt unlock för att certifikaten skall bli OK.

##### Aktivera sustainsys i securityContext.xml
Kopiera in följande nedanför SITHS-blocket av samma sort:

     <bean class="org.springframework.security.saml.metadata.ExtendedMetadataDelegate">
        <constructor-arg>
          <bean class="org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider">
            <constructor-arg>
              <value type="java.io.File">${config.dir}/sp-sustainsys.xml</value>
            </constructor-arg>
            <property name="parserPool" ref="parserPool"/>
          </bean>
        </constructor-arg>
        <property name="metadataTrustCheck" value="false"/>

        <constructor-arg>
          <bean class="org.springframework.security.saml.metadata.ExtendedMetadata">
            <property name="alias" value="sustainsys"/>
            <property name="local" value="true"/>
            <property name="securityProfile" value="metaiop"/>
            <property name="sslSecurityProfile" value="metaiop"/>
            <property name="signMetadata" value="true"/>
            <property name="signingKey" value="${sakerhetstjanst.saml.keystore.alias}"/>
            <property name="encryptionKey" value="${sakerhetstjanst.saml.keystore.alias}"/>
            <property name="requireArtifactResolveSigned" value="false"/>
            <property name="requireLogoutRequestSigned" value="false"/>
            <property name="requireLogoutResponseSigned" value="false"/>
          </bean>
        </constructor-arg>
      </bean>

      <bean class="org.springframework.security.saml.metadata.ExtendedMetadataDelegate">
        <constructor-arg>
          <bean class="org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider">
            <constructor-arg>
              <value type="java.io.File">${config.dir}/idp-sustainsys.xml</value>
            </constructor-arg>
            <property name="parserPool" ref="parserPool"/>
          </bean>
        </constructor-arg>
        <constructor-arg>
          <bean class="org.springframework.security.saml.metadata.ExtendedMetadata">
            <property name="alias" value="sustainsys"/>
          </bean>
        </constructor-arg>

        <property name="metadataTrustCheck" value="false"/>
      </bean>

### Restassured

Restassured-tester kan köras från roten av /webcert

    # Alla testklasser i ett paket
    ./gradlew restAssured --tests se.inera.intyg.webcert.web.integration.integrationtest.*
    
    # Alla metoder i en testklass
    ./gradlew restAssured --tests *ApiControllerIT
    
    # Enskild metod i testklass
    ./gradlew restAssured --tests *ApiControllerIT.testArchive

### Ny fråga

- Lägga till i AutoValue-utlåtande klassen
- Lägga till JSON-properties i RespConstants.java som matchar ID.
- Lägga till entry under rätt kategori för UE-ramverk, dvs _ag114UtkastConfigFactory.v1.js_
- Lägga till fält i utkast.v1.model.js
- Lägga till rätt texter i texterMU_AG114_v1.0.xml i webcert
- TransportToInternal.java#setSvar skall i case-sats hantera XML -> Utlåtande
- UtlatandeToIntyg.java#getSvar skall ha kod för att konvertera från Utlåtande till XML.
- Lägga till fältet i src/test/resources/ JSON och XML exempelfiler (för att tester skall fungera)
- Lägga till fältet och dess valideringsregler i InternalDraftValidatorImpl.java
- (Lägga till entry under rätt kategori för UV-ramverk, dvs _ag114ViewConfig.v1.factory.js_)
- (Lägga till schematron-validering för frågan)

## Licens
Copyright (C) 2014 Inera AB (http://www.inera.se)

Webcert is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

Webcert is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.

Se även [LICENSE.md](https://github.com/sklintyg/common/blob/master/LICENSE.md).

# Webcert
Webcert är en webbtjänst för att författa intyg samt ställa frågor och svar kring dem.

## Kom igång
Här hittar du grundläggande instruktioner för hur man kommer igång med projektet. Mer detaljerade instruktioner för att sätta upp sin utvecklingsmiljö och liknande hittar du på projektets [Wiki för utveckling](https://github.com/sklintyg/common/wiki) samt [devops/develop README-filen](https://github.com/sklintyg/devops/tree/release/2021-1/develop/README.md)

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

TODO: Säkerställ att incheckad konfiguration med SP-metadata fungerar utan åtgärd från utvecklare.

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
Copyright (C) 2025 Inera AB (http://www.inera.se)

Webcert is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

Webcert is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.

Se även [LICENSE.md](LICENSE.md).

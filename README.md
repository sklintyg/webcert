# Webcert
Webcert är en webbtjänst för att författa intyg samt ställa frågor och svar kring dem.

## Kom igång
Här hittar du grundläggande instruktioner för hur man kommer igång med projektet. Mer detaljerade instruktioner för att sätta upp sin utvecklingsmiljö och liknande hittar du på projektets [Wiki för utveckling](https://github.com/sklintyg/common/wiki).

### Bygg projektet
Webcert byggs med hjälp av Maven enligt följande:
```
$ git clone https://github.com/sklintyg/webcert.git

$ cd webcert
$ ./gradlew clean build install -PcodeQuality
```

### Starta webbapplikationen
Webbapplikationen kan startas med Jetty enligt följande:
```
$ cd webcert
$ ./gradlew appRun
$ open http://localhost:9088/welcome.jsp
```

Detta startar Webcert med stubbar för alla externa tjänster som Webcert använder. För att köra både Mina intyg och Webcert samtidigt behöver [Intygstjänsten](https://github.com/sklintyg/intygstjanst) startas före Mina intyg och Webcert.

### Starta specifik version
Man kan även starta Webcert i ett läge där endast de funktioner som är tillgängliga i en viss version är tillgängliga.
```
$ ./gradlew appRun -Pv3.0
```

### Visa databasen
Man kan även komma åt H2-databasen som startas:
```
$ open http://localhost:9090/
```

För att komma åt webcert eller intyggs databasen fyll i JDBC URL'n :

WebCert 
```
JDBC URL : jdbc:h2:tcp://localhost:9094/mem:dataSource
```

Intyg
```
JDBC URL : jdbc:h2:tcp://localhost:9092/mem:dataSource
```

### Kör FitNesse
För att köra FitNesse-testerna måste man starta FitNesse wiki samt att Intygstjänsten och Webcert är igång:
```
$ cd webcert
$ ./gradlew fitnessWiki
$ open http://localhost:9126/WebCert.AutomatiseradeTester
```

### Köra flera Webcert-instanser för dev-ändamål
(Detta avsnitt var skrivet specifikt för Maven, behöver uppdateras med gradle-instruktioner)

## Licens
Copyright (C) 2014 Inera AB (http://www.inera.se)

Webcert is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

Webcert is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.

Se även [LICENSE.md](https://github.com/sklintyg/common/blob/master/LICENSE.md).

# Webcert
Webcert är en webbtjänst för att författa intyg samt ställa frågor och svar kring dem.

## Kom igång
Här hittar du grundläggande instruktioner för hur man kommer igång med projektet. Mer detaljerade instruktioner för att sätta upp sin utvecklingsmiljö och liknande hittar du på projektets [Wiki för utveckling](https://github.com/sklintyg/common/wiki).

### Bygg projektet
Webcert byggs med hjälp av Maven enligt följande:
```
$ git clone https://github.com/sklintyg/webcert.git

$ cd webcert
$ mvn install
```

### Starta webbapplikationen
Webbapplikationen kan startas med Jetty enligt följande:
```
$ cd web
$ mvn jetty:run
$ open http://localhost:9088/welcome.jsp
```

Detta startar Webcert med stubbar för alla externa tjänster som Webcert använder. För att köra både Mina intyg och Webcert samtidigt behöver [Intygstjänsten](https://github.com/sklintyg/intygstjanst) startas före Mina intyg och Webcert.

### Starta specifik version
Man kan även starta Webcert i ett läge där endast de funktioner som är tillgängliga i en viss version är tillgängliga.
```
$ mvn jetty:run -P v3.0
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
För att köra FitNesse-testerna måste man starta FitNesse wiki:
```
$ cd ../specifications
$ mvn verify -Pwiki
$ open http://localhost:9126/
```

### Köra flera Webcert-instanser för dev-ändamål
Det är möjligt att starta flera lokala Webcert-instanser men man får se till att separera ett antal portar. Notera att databas _ej_ kommer delas.

1. Bygg webcert, t.ex. genom mvn clean install
2. Öppna ytterligare ett konsolfönster så du har två stycken totalt, byt bibliotek för båda till webcert/web.
3. I fönster 1, starta webcert som vanligt på de vanliga portarna
```
$ mvn jetty:run
```

4. I fönster 2, starta webcert med alternativa portar
```
$ mvn jetty:run -Djetty.port=2088 -Dh2.tcp.port=2094 -Dh2.web.port=2090
```

Nu borde två parallella jetty-instanser körandes Webcert vara igång. Kan vara användbart för att exempelvis testa av saker som kan påverkas av att vi kör multipla noder i produktion.

## Licens
Copyright (C) 2014 Inera AB (http://www.inera.se)

Webcert is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

Webcert is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.

Se även [LICENSE.md](https://github.com/sklintyg/common/blob/master/LICENSE.md).

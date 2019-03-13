# Cypress
[Cypress](https://www.cypress.io/ "cypress.io") är ett open source-verktyg för frontendtestning. Utveckligen och exekveringen av testfall görs normalt i Windows men schemalagda körningar sker i Docker-containers i Linux på [Jenkins](https://ci.inera.se/job/Intyg/ "Intyg i Nationell Jenkins").

## Köra end-to-end tester i Cypress

### 1) Installera Cypress
Stå i roten på webcert. Se till att node och npm är installerade:
```sh
webcert> node -v
webcert> npm -v
```
Rekommenderad lägsta version för node är 10 och för npm är 6.
```sh
webcert> npm install --save-dev cypress
```

### 2) Konfigurera URL till WebCert-applikationen
Redigera filen `cypress.json` med ett textredigeringsprogram. I exemplet nedan används notepad.
```sh
webcert> notepad cypress.json
```
Leta upp raden med `"baseUrl"` och uppdatera URL:en att peka mot testmiljön.

### 3) Köra Cypress-testerna
##### 3.1) Köra Cypress i normal webbläsare
Starta Cypress-gui genom
```sh
webcert> node_modules/.bin/cypress open
```
Här kan man köra enskilda sviter eller alla på en gång. Testerna exekveras i en synlig webbläsare.

###### 3.2) Köra Cypress Headlessly
Det går att köra alla Cypress-tester headlessly (d.v.s. utan synlig webbläsare). Det är så testerna exekveras i Jenkins. Då används den i Cypress inbyggda webbläsaren Electron. Varje testsvit genererar en mp4-videofil där hela exekveringen kan ses, dessa sparas i `test/cypress/videos`
```sh
webcert> node_modules/.bin/cypress run
```
Exempel på vad som är ändrat från standardvärden är sökvägarna till kataloger eftersom cypress-filerna inte ligger direkt under `<projektrot>/cypress`.

## Cypress filstruktur
Generellt kan sägas att konfigurationsfilen för Cypress ligger i projektroten, och resten av filerna ligger under `test/cypress/`.

### Konfiguration
Cypress konfigureras via en JSON-fil i projektroten:
```sh
webcert> cypress.json
```
I denna fil kan man åsidosätta alla standardvärden som cypress använder sig av. För mer detaljer, se [Cypress configuration](https://docs.cypress.io/guides/references/configuration.html#Options).

### Testfall
Cypress terminologi för testfallsfil är 'spec' och specs ligger i vad som kallas för 'integration folder'.
Testfallen ligger därför i underkatalogen `test/cypress/integration/`.

### Fixtures
Konstant data sparas i något som kallas fixturer, se [Cypress Fixtures](https://docs.cypress.io/api/commands/fixture.html) för mer information.
Dessa filer ligger under `test/cypress/fixtures/`. Här kan data sparas t.ex. per läkare, patient och vårdenhet och detta kan läsas in i testfallen.

### Stödfiler
Det finns stödfiler i `test/cypress/support/` där egengjorda Cypress-commands och funktioner kan läggas. Fördelen med detta är att t.ex. kod som exekveras av olika testfallsfiler ('specs') kan läggas på ett ställe istället för att dupliceras.
Informationen om stödfiler är spridd på flera ställen, bland annat [Cypress Custom Commands](https://docs.cypress.io/api/cypress-api/custom-commands.html) och [Cypress Support file](https://docs.cypress.io/guides/core-concepts/writing-and-organizing-tests.html#Support-file).

Här finns som standard filerna `index.js` och `commands.js`. Den förstnämnda innehåller vilka filer som ska importeras och den sistnämnda innehåller själva kommandona. Dessutom finns fler filer skapade här som innehåller funktioner och kommandon som är koncentererade till t.ex. en specifik typ av intyg.

### Plugins
Från [Cypress Plugins](https://docs.cypress.io/guides/tooling/plugins-guide.html): "Plugins enable you to tap into, modify, or extend the internal behavior of Cypress.". I skrivande stund används inte plugins.

### Bilder och filmer
Cypress sparar testfallsexekveringen i form av filmer om testfallen körs i headless-läget (se punkt 3 ovan, "Köra Cypress-testerna"). Dessa sparas under `test/cypress/videos/`.
Videos sparas inte när man kör i normalt läge med synlig webbläsare.
Om testfallet körs headlessly och det går fel så sparas en skärmdump vid det exakta tillfället när felet inträffar. Detta sparas under `test/cypress/screenshots/`.
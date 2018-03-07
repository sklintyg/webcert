## För att köra end-to-end tester i chrome (protractor) mha gradle:

##### 1) Kör igång IT
```sh
intygstjanst>./gradlew appRun
```

##### 2) Kör igång Webcert
```sh
webcert>./gradlew appRun
```

##### 3) Kör igång grunt server (förutsätter att du installerat grunt globalt med npm)
```sh
webcert/web>grunt server
```

##### 4) Starta upp testerna i ett nytt webbläsarfönster. Chrome används som default och behöver då vara installerat. Fungerar utan vidare i skrivande stund med senaste versionen av Chrome på macos (el capitan).
```sh
webcert>./gradlew protractorTests
```

## För att slippa ändra version på webcert-testtools och köra npm install vid varje ändring så behöver följande kommandon köras:

 ```sh
 cd webcertTestTools/
 npm link
 cd ..
 npm link webcert-testtools
```

Om npm link strular kan man även köra detta som en en-radare om man vill vara säker på att använda den test-tools du nyss ändrat i..
```sh
webcert/test>rm -rf node_modules/webcert-testtools && npm install && grunt
```

Då ändringar har gjorts i dessa moduler så bör man ändra versionsnummer för paketet innan incheckning


## Kör protractor på macOS med docker, utan att behöva installera firefox lokalt

Testat med el capitan.

Installera docker för macOS från (https://store.docker.com/editions/community/docker-ce-desktop-mac)


### Utför sedan följande steg:

##### 1) Starta Intygstjänst lokalt
```sh
intygtjanst>./gradlew appRun
```

##### 2) Starta Webcert lokalt
```sh
webcert>./gradlew :appRun
```

Starta även upp grunt server, om du vill att protractortesterna ska gå direkt mot dina källkodsfiler för frontend, via
```sh
webcert/web>grunt server
```

##### 3) Ändra envConfig.json så att Protractortesterna körs mot din riktiga maskins IP-adress
Detta är en workaround för macOS då dockercontainrar på macOS körs via en virtuell maskin med linux som i sin tur kör Docker. På Linux bör man kunna peka mot localhost istället.

Hitta ditt nätverkskorts (wifi/ethernet) IP-adress med hjälp av 'ifconfig'
Om du vill köra Webcert med grunt server, använd port 9089, annars använd port 9088 som vanligt för Webcert

Ändra webcert/test/webcertTestTools/envConfig.json. Se diff:en nedan:
```sh
{
     "dev": {
-        "WEBCERT_URL": "http://localhost:9089/",
+        "WEBCERT_URL": "http://<IP>:<PORT>/",
         "MINAINTYG_URL": "https://minaintyg.inera.nordicmedtest.se",
-        "INTYGTJANST_URL": "http://localhost:8080/inera-certificate",
-        "SELENIUM_ADDRESS": ""
+        "INTYGTJANST_URL": "http://<IP>:8080/inera-certificate",
+        "SELENIUM_ADDRESS": "http://127.0.0.1:4444/wd/hub"
     },
```

##### 4) Starta dockercontainer med selenium
```sh
>docker run --shm-size=1800M -p 4444:4444 -e SE_OPTS="-browserTimeout 60 -sessionTimeout 60" selenium/standalone-firefox:2.48.2
```

##### 5) Kör igång protractortester
```sh
webcert>./gradlew protractorTests
```


### För att se webbläsaren medan testerna körs
Det behövs en mindre ändring av Dockerkommandot:
```sh
>docker run --shm-size=1800M -p 4444:4444 - p 5900:5900 -e SE_OPTS="-browserTimeout 60 -sessionTimeout 60" selenium/standalone-firefox-debug:2.48.2
```

Debug-containern har en vnc-server uppe, som du sedan kopplar upp dig till på port 5900. På macOS så kan det göras med den inbyggda vnc-clienten som följande:
```sh
>open vnc://127.0.0.1:5900
```

lösenordet för vnc är "*secret*"


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

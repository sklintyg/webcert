# Automatiska acceptanstest

### Installera dessa
* [Java] - Krävs för selenium-webdriver
* [Node]
* [Npm]


### Klona och ladda ner npm-paket
 ```sh
$ git clone https://github.com/sklintyg/webcert.git
$ cd test
$ npm install
```
  
### Kör tester
 ```sh
 grunt acc
```

### Kör tester på ip20-mijö
 ```sh
 grunt acc:ip20
```

### Kör tester på ip20-mijö som är taggade med @smoke
 ```sh
 grunt acc:ip20 --tags='@smoke'
```

### Köra testfall med taggar

#### För att använda logiskt OCH med taggar, använd --tags och ange en sträng med kommaseparerade taggar. Exempel i ip20-mijö 
för att köra alla tester som är taggade med @smoke OCH inte @notReady
 ```sh
 grunt acc:ip20 --tags='@smoke,~@notReady'
```

#### För att använda logiskt ELLER med taggar, använd --tags och ange en sträng med mellanslagsseparerade taggar. Exempel i 
ip20-mijö som är taggade med @smoke ELLER som är taggade med @behorighet.
 ```sh
 grunt acc:ip20 --tags='@smoke @behorighet'
```

#### För att blanda logiskt ELLER och logiskt OCH med taggar, använd --tags och ange en sträng med mellanslagsseparerade och kommaseparade taggar. Exempel i 
ip20-mijö som är taggade med @smoke OCH inte är taggade med ~@notReady ELLER som är taggade med @behorighet.
 ```sh
 grunt acc:ip20 --tags='@smoke,~@notReady @behorighet'
```
### Exekvera testfall på flera noder och instanser parallellt.
Default är att alla tester utförs sekventiellt på 1 nod med 1 browser-instans. 

För att exekvera testerna parallellt på selenium-noden(noderna) och därmed potentiellt snabba upp exekveringstiden anges flaggan 'gridnodeinstances' till grunt-kommandot. Värdet på denna flagga är maximalt antal browser-instanser (totalt) som du vill exekvera tester på. Vid tillfället då det här skrevs har vi ett grid uppsatt med tre selenium-noder som maximalt kan ha 5 instanser var. I exemplet nedan använder vi oss därmed av maximalt antar instanser.
```sh
DATABASE_PASSWORD=xxxxxxxx grunt acc:ip30 --gridnodeinstances=15
``` 
För att få reda på maximalt antar browser-instanser i aktuellt konfiguration, gå till selenium-hubben i en browser (t.ex. http://selenium1.nordicmedtest.se:4444/grid/console för aktuell grid-uppsättning vid skrivtillfället) och inspektera respektive uppsatt nods konfiguration.

### Köra lokal Selenium-server
För att inte köra mot Selenium-hubben utan istället köra mot en lokal uppsättning, lägg till flaggan --local-selenium:
```sh
 grunt acc:ip30 --local-selenium
```
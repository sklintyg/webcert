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
ip20-mijö som är taggade med @smoke ELLER som är taggade med  @behorighet.
 ```sh
 grunt acc:ip20 --tags='@smoke,~@notReady @behorighet'
```

#### För att blanda logiskt ELLER och logiskt OCH med taggar, använd --tags och ange en sträng med mellanslagsseparerade och kommaseparade taggar. Exempel i 
ip20-mijö som är taggade med @smoke OCH inte är taggade med ~@notReady ELLER som är taggade med @behorighet.
 ```sh
 grunt acc:ip20 --tags='@smoke,~@notReady @behorighet'
```

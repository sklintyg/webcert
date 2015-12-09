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
 grunt acc:ip20:@smoke
```
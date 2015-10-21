# Webcert prestandatest

# Seedning av testdata
I mappen src/test/resources finns två csv-filer. En med intyg och en med testpersonnummer från skatteverket. (ca 16000 st)

Seedning av frågor sker genom följande simulering:
mvn test -DsimulationClass=se.inera.webcert.InjiceraFraga

# Hur startar jag en simulering

Från command-line fungerar följande:

- Fråga/Svar
  mvn test -DsimulationClass=se.inera.webcert.FragaSvar
  
- Skriv, signera och skicka intyg
  mvn test -DsimulationClass=se.inera.webcert.SkrivSigneraSkickaIntyg
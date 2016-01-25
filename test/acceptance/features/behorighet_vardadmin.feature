# language: sv
@behorighet
Egenskap: Fråga/Svar genererar rätt statusuppdateringar 

Scenario: Vårdadministratör kopierar ett intyg
   Givet att jag är inloggad som vårdadministratör
   Och går in på Sök/skriv intyg
   Och jag väljer patienten "19121212-1212"
   Och jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
   Så är kopieraknappen tillgänglig

   



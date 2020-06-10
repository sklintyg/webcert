#OBS
Dessa xsd:er har sina import-taggar omskrivna. Attributet schemaLocation är omskrivet för att peka på den lokala filen i denna mapp.

Detta på grund av buggar i JAXBs XJC-verktyg som förhindrar oss att använda en catalog-fil tillsammans med bindings.

En bättre lösning på detta bör implementeras på sikt.
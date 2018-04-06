# language: sv
@NOTREADY @FALTVALIDERING @LISJP 
Egenskap: Fältvalidering för LISJP

Bakgrund:
    Givet att jag är inloggad som läkare
    Och jag går in på en patient
    Och jag går in på att skapa ett "Läkarintyg för sjukpenning" intyg

@F.VAL-044
Scenario: Intyget kan inte signeras om slut är före startdatum
    När jag anger slutdatum som är tidigare än startdatum
    Och jag klickar på signera-knappen
    Så ska "4" valideringsfel visas med texten "Startdatum får inte vara efter slutdatum."

@F.VAL-005
Scenario: Datum får inte ligga för långt fram eller bak i tiden
    När jag anger start- och slutdatum för långt bort i tiden
    Och jag klickar på signera-knappen
    Så ska "8" valideringsfel visas mex texten "Datum får inte ligga för långt fram eller tillbaka i tiden."

@F.VAL-010
Scenario: Sjukskrivningsperiod med överlappande datum får inte anges
    När jag anger överlappande start- och slutdatum
    Och jag klickar på knappen
    Så ska "8" valideringsfel visas mex texten "Sjukskrivningsperiod med överlappande datum har angetts."

@F.VAL-042
Scenario: Period mer än 6 månader ska varnas för
    När jag anger start- och slutdatum med mer än 6 månaders mellanrum
    Och jag klickar på signera-knappen
    Så ska "1" varningsmeddelande med texten "Det datum du angett innebär en period på mer än 6 månader. Du bör kontrollera att tidsperioderna är korrekta." visas

@F.VAL-043
Scenario: Startdatum en vecka före dagens datum
    När jag anger startdatum mer än en vecka före dagens datum
    Och jag klickar på signera-knappen
    Så ska "4" varningsmeddelanden med texten "Det startdatum du angett är mer än en vecka före dagens datum. Du bör kontrollera att tidsperioderna är korrekta." visas



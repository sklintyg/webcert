# language: sv
@FALTVALIDERING @LISJP 
Egenskap: Fältvalidering för LISJP

Bakgrund:
    Givet att jag är inloggad som läkare
    Och jag går in på testpatienten för "fältvalidering"
    Och jag går in på att skapa ett "Läkarintyg för sjukpenning" intyg

@F.VAL-005
Scenario: Datum får inte ligga för långt fram eller bak i tiden
    När jag anger start- och slutdatum för långt bort i tiden
    Och jag klickar på signera-knappen
    Så ska "8" valideringsfel visas med texten "Datum får inte ligga för långt fram eller tillbaka i tiden."

@F.VAL-010
Scenario: Sjukskrivningsperiod med överlappande datum får inte anges
    När jag anger överlappande start- och slutdatum
    Och jag klickar på signera-knappen
    Så ska "1" valideringsfel visas med texten "Sjukskrivningsperiod med överlappande datum har angetts."

@F.VAL-022 
@F.VAL-025
Scenario: Minst en sjukskrivningsperiod samt en åtgärd måste väljas.
    När jag klickar på signera-knappen
    Så ska "1" valideringsfel visas med texten "Minst en sjukskrivningsperiod måste anges."
    Så ska "1" valideringsfel visas med texten "Åtgärder måste väljas eller Inte aktuellt."

@F.VAL-031
Scenario: Ogiltiga datum får inte anges
    När jag anger ogiltiga datum
    Och jag klickar på signera-knappen
    Så ska "8" valideringsfel visas med texten "Ogiltigt datum."

@F.VAL-042
Scenario: Period mer än 6 månader ska varnas för
    När jag anger start- och slutdatum med mer än 6 månaders mellanrum
    Så ska "1" varningsmeddelande visas med texten "Det datum du angett innebär en period på mer än 6 månader. Du bör kontrollera att tidsperioderna är korrekta."

@F.VAL-043
Scenario: Startdatum en vecka före dagens datum
    När jag anger startdatum mer än en vecka före dagens datum
    Så ska "1" varningsmeddelanden visas med texten "Det startdatum du angett är mer än en vecka före dagens datum. Du bör kontrollera att tidsperioderna är korrekta."

@F.VAL-044
Scenario: Intyget kan inte signeras om slut är före startdatum
    När jag anger slutdatum som är tidigare än startdatum
    Och jag klickar på signera-knappen
    Så ska "4" valideringsfel visas med texten "Startdatum får inte vara efter slutdatum."

@F.VAL-046 @WAITINGFORFIX @INTYG-6323
Scenario: Undersökningsdatum i framtiden ska ge varning
    När jag anger undersökningsdatum i framtiden
	Och jag klickar på signera-knappen
    Så ska "4" varningsmeddelande visas med texten "Observera att du valt ett datum framåt i tiden."



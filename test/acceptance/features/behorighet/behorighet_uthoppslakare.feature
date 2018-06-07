# language: sv
@BEHÖRIGHET @UTHOPP
Egenskap: Behörigheter för en uthoppsläkare

Bakgrund: Inloggad som uthoppsläkare
   Givet att jag är inloggad som uthoppsläkare

@TESTABILITY
Scenario: Inloggad som uthoppsläkare
   Så ska jag ha rollen "LAKARE"
   Och jag ska ha origin "UTHOPP"

@SIGNERA @LISJP
Scenario: Kan signera intyg på lisjp
   När jag går in på en patient
   Och jag går in på att skapa ett "Läkarintyg för sjukpenning" intyg
   Och jag fyller i alla nödvändiga fält för intyget
   Så är signeraknappen tillgänglig
   När jag signerar intyget
   Så ska jag se den data jag angett för intyget

@FÖRNYA @LISJP
Scenario: Kan inte förnya intyg Läkarintyg för sjukpenning
   När går in på Sök/skriv intyg
   Och jag går in på en patient
   Och jag går in på ett "Läkarintyg för sjukpenning" med status "Signerat"
   Så ska det inte finnas en knapp för att förnya intyget

@FÖRNYA @TS @KORKORTSBEHORIGHET
Scenario: Kan inte förnya Transportstyrelsens läkarintyg högre körkortsbehörighet
   När går in på Sök/skriv intyg
   Och jag går in på en patient
   Och jag går in på ett "Transportstyrelsens läkarintyg högre körkortsbehörighet" med status "Signerat"
   Så ska det inte finnas en knapp för att förnya intyget

@FÖRNYA @TS @DIABETES
Scenario: Kan inte förnya Transportstyrelsens läkarintyg diabetes intyg
   När går in på Sök/skriv intyg
   Och jag går in på en patient
   Och jag går in på ett "Transportstyrelsens läkarintyg diabetes" med status "Signerat"
   Så ska det inte finnas en knapp för att förnya intyget

@MAKULERA
Scenario: Kan inte makulera intyg
   När jag skickar ett "Läkarintyg FK 7263" intyg till Intygstjänsten
   Och jag skickar intyget direkt till Försäkringskassan
   Och jag går in på intyget via uthoppslänk
   Så ska makuleraknappen inte vara tillgänglig

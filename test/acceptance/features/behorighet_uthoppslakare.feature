# language: sv
@behorighet @notReady
Egenskap: Behörigheter för en uthoppsläkare

Scenario: Inloggad som uthoppsläkare
   Givet att jag är inloggad som uthoppsläkare
   Så ska jag ha rollen "LAKARE"
   Och jag ska ha origin "UTHOPP"

Scenario: En uthoppsläkare ska kunna signera intyg
   Givet att jag är inloggad som uthoppsläkare
   När jag väljer patienten "19971019-2387"
   Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
   Och jag fyller i alla nödvändiga fält för intyget
   Så är signeraknappen tillgänglig
   När jag signerar intyget
   Så ska jag se den data jag angett för intyget

Scenario: En uthoppsläkare ska inte kunna makulera intyg
   Givet att jag är inloggad som uthoppsläkare
   När jag väljer patienten "19971019-2387"
   Och jag går in på ett "Läkarintyg FK 7263" med status "Mottaget"
   Så ska makuleraknappen inte vara tillgänglig
@impl
Scenario: En uthoppsläkare ska inte kunna filtrera osignerade intyg på läkare
   Givet att jag är inloggad som uthoppsläkare
   När går in på Ej signerade utkast
   Och väljer att visa sökfilter 
   Så ska sökfiltret Sparat av inte vara tillgängligt



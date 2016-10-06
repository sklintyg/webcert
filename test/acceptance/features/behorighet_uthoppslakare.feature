# language: sv
@behorighet @uthopp
Egenskap: Behörigheter för en uthoppsläkare

Bakgrund: Inloggad som uthoppsläkare
   Givet att jag är inloggad som uthoppsläkare

Scenario: Inloggad som uthoppsläkare
   Så ska jag ha rollen "LAKARE"
   Och jag ska ha origin "UTHOPP"

@fk7263 @signera
Scenario: Kan signera intyg
   När jag går in på en patient
   Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
   Och jag fyller i alla nödvändiga fält för intyget
   Så är signeraknappen tillgänglig
   När jag signerar intyget
   Så ska jag se den data jag angett för intyget

Scenario: Kan inte kopiera intyg Läkarintyg FK 7263
   När går in på Sök/skriv intyg
   Och jag går in på en patient
   Och jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
   Så är kopieraknappen inte tillgänglig

@kopiera @ts @bas
Scenario: Kan inte kopiera Transportstyrelsens läkarintyg
   När går in på Sök/skriv intyg
   Och jag går in på en patient
   Och jag går in på ett "Transportstyrelsens läkarintyg" med status "Signerat"
   Så är kopieraknappen inte tillgänglig

@kopiera @ts @diabetes
Scenario: Kan inte kopiera Transportstyrelsens läkarintyg, diabetes intyg
   När går in på Sök/skriv intyg
   Och jag går in på en patient
   Och jag går in på ett "Transportstyrelsens läkarintyg, diabetes" med status "Signerat"
   Så är kopieraknappen inte tillgänglig

@makulera @fk7263
Scenario: Kan inte makulera intyg
   När jag går in på en patient
   Och jag går in på ett "Läkarintyg FK 7263" med status "Mottaget"
   Så ska makuleraknappen inte vara tillgänglig

Scenario: Kan inte filtrera osignerade intyg på läkare
   När går in på Ej signerade utkast
   Och väljer att visa sökfilter
   Så ska sökfiltret Sparat av inte vara tillgängligt

@fråga-från-fk
Scenario: Ska kunna svara på frågor från Försäkringskassan
   När går in på Sök/skriv intyg
   Och jag går in på en patient
   När jag går in på ett "Läkarintyg FK 7263" med status "Mottaget"
   Och Försäkringskassan har ställt en "Avstamningsmote" fråga om intyget
   Och jag svarar på frågan
   Så kan jag se mitt svar under hanterade frågor

@nyttIntyg
Scenario: Ska få varning vid svar med nytt intyg
   När jag går in på en patient
   När jag går in på ett "Läkarintyg FK 7263" med status "Mottaget"
   När Försäkringskassan ställer en "Komplettering_av_lakarintyg" fråga om intyget
   Och jag går in på intyget via uthoppslänk
   Så ska jag se kompletteringsfrågan på intygs-sidan
   Och jag ska inte kunna komplettera med nytt intyg från webcert
   Och ska kompletteringsdialogen innehålla texten "Besvara kompletteringsbegäran"
   
@komplettering
Scenario: Ska kunna besvara komplettering med textmeddelande
   När jag går in på en patient
   När jag går in på ett "Läkarintyg FK 7263" med status "Mottaget"
   När Försäkringskassan ställer en "Komplettering_av_lakarintyg" fråga om intyget
   Och jag går in på intyget via uthoppslänk
   Så ska jag se kompletteringsfrågan på intygs-sidan
   Och jag ska kunna svara med textmeddelande

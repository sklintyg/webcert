# language: sv
@behorighet
Egenskap: Behörigheter för en uthoppsläkare

Scenario: Inloggad som uthoppsläkare
   Givet att jag är inloggad som uthoppsläkare
   Så ska jag ha rollen "LAKARE"
   Och jag ska ha origin "UTHOPP"

Scenario: Kan signera intyg
   Givet att jag är inloggad som uthoppsläkare
   När jag väljer patienten "19971019-2387"
   Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
   Och jag fyller i alla nödvändiga fält för intyget
   Så är signeraknappen tillgänglig
   När jag signerar intyget
   Så ska jag se den data jag angett för intyget

Scenario: Kan inte kopiera intyg Läkarintyg FK 7263 
   Givet att jag är inloggad som uthoppsläkare
   Och går in på Sök/skriv intyg
   Och jag väljer patienten "19971019-2387"
   Och jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
   Så är kopieraknappen inte tillgänglig

@notReady
Scenario: Kan inte kopiera Transportstyrelsens läkarintyg 
   Givet att jag är inloggad som uthoppsläkare
   Och går in på Sök/skriv intyg
   Och jag väljer patienten "19971019-2387"
   Och jag går in på ett "Transportstyrelsens läkarintyg" med status "Signerat"
   Så är kopieraknappen inte tillgänglig

@notReady
Scenario: Kan inte kopiera Transportstyrelsens läkarintyg, diabetes intyg
   Givet att jag är inloggad som uthoppsläkare
   Och går in på Sök/skriv intyg
   Och jag väljer patienten "19971019-2387"
   Och jag går in på ett "Transportstyrelsens läkarintyg, diabetes" med status "Signerat"
   Så är kopieraknappen inte tillgänglig

Scenario: Kan inte makulera intyg
   Givet att jag är inloggad som uthoppsläkare
   När jag väljer patienten "19971019-2387"
   Och jag går in på ett "Läkarintyg FK 7263" med status "Mottaget"
   Så ska makuleraknappen inte vara tillgänglig

Scenario: Kan inte filtrera osignerade intyg på läkare
   Givet att jag är inloggad som uthoppsläkare
   När går in på Ej signerade utkast
   Och väljer att visa sökfilter 
   Så ska sökfiltret Sparat av inte vara tillgängligt

Scenario: Ska kunna svara på frågor från Försäkringskassan
   Givet att jag är inloggad som uthoppsläkare
   Och går in på Sök/skriv intyg
   Och jag väljer patienten "19121212-1212"
   När jag går in på ett "Läkarintyg FK 7263" med status "Mottaget"
   Och Försäkringskassan har ställt en "Komplettering_av_lakarintyg" fråga om intyget
   Och jag svarar på frågan
   Så kan jag se mitt svar under hanterade frågor


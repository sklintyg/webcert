# language: sv
@meddelanden @fk7263
Egenskap: Försäkringskassan kan skicka frågor på sjukintyg Fk7263

Bakgrund: Jag har skickat en CreateDraft till Webcert.
   Givet att jag är inloggad som djupintegrerad läkare
   Och att vårdsystemet skapat ett intygsutkast för "Läkarintyg FK 7263"
   Och jag går in på intygsutkastet via djupintegrationslänk
   Och jag fyller i alla nödvändiga fält för intyget
   Och jag signerar intyget
   Och jag skickar intyget till Försäkringskassan

@avstammningsmote
Scenario: Avstämningsmöte
   När Försäkringskassan ställer en "Avstamningsmote" fråga om intyget
   Så ska statusuppdatering "HAN6" skickas till vårdsystemet. Totalt: "1"

   Och jag går in på intygsutkastet via djupintegrationslänk
   Och jag svarar på frågan
   Så ska statusuppdatering "HAN9" skickas till vårdsystemet. Totalt: "1"

@paminnelse
Scenario: HAN Påminnelse
   När Försäkringskassan ställer en "Paminnelse" fråga om intyget
   Så ska statusuppdatering "HAN6" skickas till vårdsystemet. Totalt: "1"

   Och jag går in på intygsutkastet via djupintegrationslänk
   Och jag markerar frågan från Försäkringskassan som hanterad
   Så ska statusuppdatering "HAN9" skickas till vårdsystemet. Totalt: "1"

@kontakt
Scenario: Kontakt
   När Försäkringskassan ställer en "Kontakt" fråga om intyget
   Så ska statusuppdatering "HAN6" skickas till vårdsystemet. Totalt: "1"

   Och jag går in på intygsutkastet via djupintegrationslänk
   Och jag markerar frågan från Försäkringskassan som hanterad
   Så ska statusuppdatering "HAN9" skickas till vårdsystemet. Totalt: "1"

@arbetstidsforlaggnings
Scenario: Arbetstidsförläggning
   När Försäkringskassan ställer en "Arbetstidsforlaggning" fråga om intyget
   Så ska statusuppdatering "HAN6" skickas till vårdsystemet. Totalt: "1"

   Och jag går in på intygsutkastet via djupintegrationslänk
   Och jag markerar frågan från Försäkringskassan som hanterad
   Så ska statusuppdatering "HAN9" skickas till vårdsystemet. Totalt: "1"

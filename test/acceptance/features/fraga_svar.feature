# language: sv
@fragasvar
Egenskap: Försäkringskassan kan skicka frågor på sjukintyg

Bakgrund: Jag har skickat en CreateDraft till Webcert.
   Givet att vårdsystemet skickat ett intygsutkast
          
Scenario: FK skickar fråga (komplettering av läkarintyg)
   Givet att jag är inloggad som läkare
   Och jag går in på intygsutkastet via djupintegrationslänk
   Så ska intygsutkastets status vara "Utkastet är sparat, men obligatoriska uppgifter saknas."

   Och när jag skickat ett signerat intyg till Försäkringskassan
   Så ska statusuppdatering "HAN3" skickas till vårdsystemet. Totalt: "1"

   Och när Försäkringskassan ställer en fråga om intyget - "Komplettering_av_lakarintyg"
   Så ska statusuppdatering "HAN6" skickas till vårdsystemet. Totalt: "1"

   Och jag går in på intygsutkastet via djupintegrationslänk
   Och när jag svarar på frågan
   Så ska statusuppdatering "HAN9" skickas till vårdsystemet. Totalt: "1"

Scenario: FK skickar fråga (avstämningsmöte)
   Givet att jag är inloggad som läkare
   Och jag går in på intygsutkastet via djupintegrationslänk
   Så ska intygsutkastets status vara "Utkastet är sparat, men obligatoriska uppgifter saknas."

   Och när jag skickat ett signerat intyg till Försäkringskassan
   Så ska statusuppdatering "HAN3" skickas till vårdsystemet. Totalt: "1"

   Och när Försäkringskassan ställer en fråga om intyget - "Avstamningsmote"
   Så ska statusuppdatering "HAN6" skickas till vårdsystemet. Totalt: "1"

   Och jag går in på intygsutkastet via djupintegrationslänk
   Och när jag svarar på frågan
   Så ska statusuppdatering "HAN9" skickas till vårdsystemet. Totalt: "1"

Scenario: FK skickar fråga (påminnelse)
   Givet att jag är inloggad som läkare
   Och jag går in på intygsutkastet via djupintegrationslänk
   Så ska intygsutkastets status vara "Utkastet är sparat, men obligatoriska uppgifter saknas."

   Och när jag skickat ett signerat intyg till Försäkringskassan
   Så ska statusuppdatering "HAN3" skickas till vårdsystemet. Totalt: "1"

   Och när Försäkringskassan ställer en fråga om intyget - "Paminnelse"
   Så ska statusuppdatering "HAN6" skickas till vårdsystemet. Totalt: "1"

   Och jag går in på intygsutkastet via djupintegrationslänk
   Och när jag markerar frågan från Försäkringskassan som hanterad
   Så ska statusuppdatering "HAN9" skickas till vårdsystemet. Totalt: "1"

Scenario: FK skickar fråga (kontakt)
   Givet att jag är inloggad som läkare
   Och jag går in på intygsutkastet via djupintegrationslänk
   Så ska intygsutkastets status vara "Utkastet är sparat, men obligatoriska uppgifter saknas."

   Och när jag skickat ett signerat intyg till Försäkringskassan
   Så ska statusuppdatering "HAN3" skickas till vårdsystemet. Totalt: "1"

   Och när Försäkringskassan ställer en fråga om intyget - "Kontakt"
   Så ska statusuppdatering "HAN6" skickas till vårdsystemet. Totalt: "1"

   Och jag går in på intygsutkastet via djupintegrationslänk
   Och när jag markerar frågan från Försäkringskassan som hanterad
   Så ska statusuppdatering "HAN9" skickas till vårdsystemet. Totalt: "1"

@dret
Scenario: FK skickar fråga (arbetstidsförläggning)
   Givet att jag är inloggad som läkare
   Och jag går in på intygsutkastet via djupintegrationslänk
   Så ska intygsutkastets status vara "Utkastet är sparat, men obligatoriska uppgifter saknas."

   Och när jag skickat ett signerat intyg till Försäkringskassan
   Så ska statusuppdatering "HAN3" skickas till vårdsystemet. Totalt: "1"

   Och när Försäkringskassan ställer en fråga om intyget - "Arbetstidsforlaggning"
   Så ska statusuppdatering "HAN6" skickas till vårdsystemet. Totalt: "1"

   Och jag går in på intygsutkastet via djupintegrationslänk
   Och när jag markerar frågan från Försäkringskassan som hanterad
   Så ska statusuppdatering "HAN9" skickas till vårdsystemet. Totalt: "1"



          

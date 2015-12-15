# language: sv
@soap @notReady
Egenskap: Kontrollera att webcerts hanterar CertificateStatusUpdate korrekt

Bakgrund: Jag har skickat en CreateDraft till Webcert.
   Givet att vårdsystemet skickat ett intygsutkast

@soap
Scenario: Ta emot en statusuppdatering
   Givet att jag är inloggad som läkare
   Och jag går in på intygsutkastet via djupintegrationslänk
   Så ska intygsutkastets status vara "Utkastet är sparat, men obligatoriska uppgifter saknas."
   Och när jag fyller i fältet "Min undersökning av patienten"
   Så ska statusuppdatering "HAN1" skickas till vårdsystemet."
   Och när jag fyller i fältet "ICD-10"
   Så ska statusuppdatering "HAN1" skickas till vårdsystemet."
   Och när jag fyller i fältet "Funktionsnedsättning"
   Så ska statusuppdatering "HAN1" skickas till vårdsystemet."
   Och när jag fyller i fältet "Aktivitetsbegränsning"
   Så ska statusuppdatering "HAN1" skickas till vårdsystemet."
   Och när jag fyller i fältet "Arbete"
   Så ska statusuppdatering "HAN1" skickas till vårdsystemet."
   Och när jag fyller i fältet "Arbetsförmåga"
   Så ska statusuppdatering "HAN1" skickas till vårdsystemet."
   Och signerar intyget
   Så ska statusuppdatering "HAN2" skickas till vårdsystemet."

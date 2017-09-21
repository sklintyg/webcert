#language: sv
@paminnelse @smi
Egenskap: Som läkare vill jag att påminnelser syns tydligt och leder mig till ursprungsfrågan

Bakgrund: Jag har skickat en CreateDraft:2 till Webcert.
   Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"

Scenario: Påminnelse - KOMPLT, PAMINN & NYFRFM
    Och att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    Och jag går in på intygsutkastet via djupintegrationslänk
    När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag skickar intyget till Försäkringskassan

    Och Försäkringskassan ställer en "KOMPLT" fråga om intyget
    Och Försäkringskassan ställer en "PAMINN" fråga om intyget
    Så ska statusuppdatering "NYFRFM" skickas till vårdsystemet. Totalt: "2"
    Och ska jag se påminnelsen på intygssidan

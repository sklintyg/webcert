# language: sv
@statusuppdateringar @lisu
Egenskap: Statusuppdateringar för LISU

Bakgrund: Jag har skickat en CreateDraft:2 till Webcert.
   Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1004"
   Och att vårdsystemet skapat ett intygsutkast för "Läkarintyg för sjukpenning utökat"
   Och jag går in på intygsutkastet via djupintegrationslänk

@skicka-till-fk
Scenario: Statusuppdateringar då intyg skickas till Försäkringskassan
   Så ska statusuppdatering "SKAPAT" skickas till vårdsystemet. Totalt: "1"

   När jag fyller i alla nödvändiga fält för intyget
   Och jag signerar intyget
   Så ska statusuppdatering "SIGNAT" skickas till vårdsystemet. Totalt: "1"

   När jag skickar intyget till Försäkringskassan
   Så ska statusuppdatering "SKICKA" skickas till vårdsystemet. Totalt: "1"

@makulera
Scenario: Statusuppdateringar då intyg makuleras
    När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag skickar intyget till Försäkringskassan

    När jag makulerar intyget
    Så ska statusuppdatering "MAKULE" skickas till vårdsystemet. Totalt: "1"

@radera
Scenario: Statusuppdateringar då intyg raderas
    När jag fyller i alla nödvändiga fält för intyget
    Och jag raderar intyget
    Så ska statusuppdatering "RADERA" skickas till vårdsystemet. Totalt: "1"

@fråga-från-fk
Scenario: Statusuppdateringar vid fråga från FK
    När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag skickar intyget till Försäkringskassan
    Så ska statusuppdatering "SKICKA" skickas till vårdsystemet. Totalt: "1"

    När Försäkringskassan skickar ett "Kontakt" meddelande på intyget
    Så ska statusuppdatering "NYFRFM" skickas till vårdsystemet. Totalt: "1"

    När jag går in på intygsutkastet via djupintegrationslänk
    Och jag svarar på frågan
    Så ska statusuppdatering "HANFRA" skickas till vårdsystemet. Totalt: "1"

@fråga-till-fk
Scenario: Statusuppdateringar vid fråga till FK
    När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag skickar intyget till Försäkringskassan

    Så ska statusuppdatering "SKICKA" skickas till vårdsystemet. Totalt: "1"

    Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag skickar en fråga med ämnet "Kontakt" till Försäkringskassan
    Så ska statusuppdatering "NYFRTM" skickas till vårdsystemet. Totalt: "1"

    Och Försäkringskassan skickar ett svar
    Så ska statusuppdatering "NYSVFM" skickas till vårdsystemet. Totalt: "1"

#    Och jag markerar svaret från Försäkringskassan som hanterat
#    Så ska statusuppdatering "HAN10" skickas till vårdsystemet. Totalt: "1"

# @komplettering @notReady
# Scenario: Statusuppdateringar vid komplettering med nytt intyg
    # När jag fyller i alla nödvändiga fält för intyget
    # Och jag signerar intyget
    # Och jag skickar intyget till Försäkringskassan

    # Och Försäkringskassan ställer en "Komplettering_av_lakarintyg" fråga om intyget # Gammal
    # Och Försäkringskassan skickar ett "komplettering_av_lakarintyg" meddelande på intyget # Ny
    # Så ska statusuppdatering "NYFRFM" skickas till vårdsystemet. Totalt: "1"

    # När jag går in på intygsutkastet via djupintegrationslänk
    # Och jag väljer att svara med ett nytt intyg
    # Så ska jag se kompletteringsfrågan på utkast-sidan

    # När jag signerar intyget
    # Och jag skickar intyget till Försäkringskassan
    # Så ska statusuppdatering "SKAPAT" skickas till vårdsystemet. Totalt: "1"
    # Och ska intygets status vara "Intyget är signerat"
    #Och ska 1 statusuppdatering "HANFRA" skickas för det ursprungliga intyget

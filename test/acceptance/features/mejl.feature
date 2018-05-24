# language: sv
@MAIL @NOTREADY
Egenskap: Mejlnotifieringar vid nya ärenden

Bakgrund: Jag har skickat en CreateDraft till Webcert.
   Givet att jag är inloggad som läkare
   När jag skickar ett "Läkarintyg FK 7263" intyg till Intygstjänsten
   Och jag skickar intyget direkt till Försäkringskassan

@AVSTAMNINGMÖTE @NOTREADY
Scenario: Vid fråga från FK
   När Försäkringskassan ställer en "Avstamningsmote" fråga om intyget
   Så ska jag få ett mejl med ämnet "Ny fråga från Försäkringskassan"

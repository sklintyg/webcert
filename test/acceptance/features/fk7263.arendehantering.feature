# language: sv
@arendehantering @fk7263
Egenskap: FK7263 Ärendehantering

Bakgrund: Jag har skickat ett intyg till försäkringskassan
	Givet att jag är inloggad som läkare
	När jag går in på en patient
   	Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
   	Och jag skickar intyget till Försäkringskassan

@ej-hanterad
Scenario: Hantera fråga dialog
   När Försäkringskassan ställer en "Avstamningsmote" fråga om intyget
   Och Försäkringskassan ställer en "Paminnelse" fråga om intyget
   Och jag går till sidan Frågor och svar
   Och jag väljer åtgärden "Visa alla ej hanterade"
   Så ska det finnas en rad med texten "Markera som hanterad" för frågan
   När jag väljer att visa intyget som har en fråga att hantera
   Och jag lämnar intygssidan
   Så ska jag få dialogen "Markera besvarade frågor som hanterade"

   När jag väljer valet att markera som hanterade
   Och jag väljer åtgärden "Visa alla ej hanterade"
   Så ska den tidigare raden inte finnas kvar i tabellen för Frågor och svar

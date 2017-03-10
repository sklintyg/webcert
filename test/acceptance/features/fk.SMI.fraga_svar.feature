#language: sv
@notReady
Egenskap: Försäkringskassan kan skicka frågor på sjukintyg LUSE

Bakgrund: Jag befinner mig på webcerts förstasida
   Givet att jag är inloggad som läkare
   När jag går in på en patient

Scenario: FK skickar fråga på "LUSE"

   	När jag går in på att skapa ett "Läkarutlåtande för sjukersättning" intyg
   	Och jag fyller i alla nödvändiga fält för intyget
   	Och jag signerar intyget
   	Och jag skickar intyget till Försäkringskassan
	Och Försäkringskassan ställer en "AVSTMN" fråga om intyget

# language: sv
@arendehantering @fk7263
Egenskap: FK7263 Ärendehantering

Bakgrund: Jag är inloggad och inne på ett skickat intyg
      Givet att jag är inloggad som läkare "Karin Persson"
      Och jag går in på en patient
      Och jag går in på ett "Läkarintyg FK 7263" med status "Mottaget"

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

@filter 
Scenario: Filtrera frågor på läkare
   När Försäkringskassan ställer en "Kontakt" fråga om intyget
   Och jag går till sidan Frågor och svar

   Givet att jag är inloggad som läkare "Johan Johansson"
   När jag går in på en patient
   När jag går in på ett "Läkarintyg FK 7263" med status "Mottaget"
   Och Försäkringskassan ställer en "Kontakt" fråga om intyget
   
   Och jag går till sidan Frågor och svar
   Så ska jag se flera frågor

   När jag väljer att filtrera på läkare "Karin Persson"
   Så ska jag bara se frågor på intyg signerade av "Karin Persson"

@befintlig-fråga
Scenario: Skicka fråga till Försäkringskassan genom att gå in på en befintlig fråga
      När Försäkringskassan ställer en "Kontakt" fråga om intyget
      Och jag går till sidan Frågor och svar
      Och jag väljer att visa intyget med frågan

      När jag skickar en fråga med slumpat ämne till Försäkringskassan

      Så ska ett info-meddelande visa "Frågan är skickad till Försäkringskassan"
      Och ska jag se min fråga under ohanterade frågor



   

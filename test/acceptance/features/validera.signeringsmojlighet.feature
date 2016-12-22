# language: sv
Egenskap: Ofullständiga intyg ska ej kunna signeras

Bakgrund: Jag befinner mig på webcerts förstasida
   Givet att jag är inloggad som läkare
   När jag går in på en patient

@kristi
Scenario: Det ska inte gå att signera ofullständigt  Läkarintyg FK 7263   
  Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
  Och jag fyller i alla obligatoriska  fält för intyget
  Och jag raderar ett  slumpat obligatoriskt fält
  Så ska signera-knappen inte vara klickbar

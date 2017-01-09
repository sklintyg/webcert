# language: sv
@inkomplett
Egenskap: Ofullständiga intyg ska ej kunna signeras

Bakgrund: Jag befinner mig på webcerts förstasida
   Givet att jag är inloggad som läkare
   När jag går in på en patient

@fk7263
Scenario: Det ska inte gå att signera ofullständigt  Läkarintyg FK 7263   
  Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
  Och jag fyller i alla obligatoriska  fält för intyget
  Och jag raderar ett  slumpat obligatoriskt fält
  Och jag klickar på signera-knappen
  Så ska jag se en rubrik med texten "Utkastet saknar uppgifter i följande avsnitt"
  Och ska jag se en lista med vad som saknas
@kristi
Scenario: Det ska inte gå att signera  Läkarintyg FK 7263 om Prognos Går ej att bedöma är vald men beskrivning saknas
 Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
 Och jag fyller i alla obligatoriska  fält för intyget
 Och jag kryssar i Prognos Går ej att bedöma utan beskrivning
 Och jag klickar på signera-knappen
 Så ska jag se en rubrik med texten "Utkastet saknar uppgifter i följande avsnitt"
 Och ska jag se en lista med vad som saknas

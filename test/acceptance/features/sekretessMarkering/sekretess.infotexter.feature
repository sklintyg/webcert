# language: sv
@sekretess
Egenskap: Sekretessmarkerad patient

@varningsmeddelande
Scenario: Varningmeddelanden när man går in på patient
   Givet att jag är inloggad som läkare
   Och jag går in på en patient med sekretessmarkering
   När jag skickar ett "Läkarintyg FK 7263" intyg till Intygstjänsten
   Så ska det finnas en knapp med texten "Förnya"
   Så ska det finnas en knapp med texten "Makulera"
   Så ska en varningsruta innehålla texten "Patienten har en sekretessmarkering."
   Så ska en varningsruta innehålla texten "På grund av sekretessmarkeringen går det inte att förnya intyg."




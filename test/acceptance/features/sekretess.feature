# language: sv
@sekretess
Egenskap: Sekretessmarkerad patient

Bakgrund: Jag loggar in som läkare och går in på patient med sekretessmarkering.
   Givet att jag är inloggad som djupintegrerad läkare
   Och jag går in på en patient med sekretessmarkering

@varningsmeddelande
Scenario: Varningmeddelanden när man går in på patient
    Så ska en varningsruta innehålla texten "Patienten har en sekretessmarkering."
    Och ska en varningsruta innehålla texten "På grund av sekretessmarkeringen går det inte att kopiera intyg."

# language: sv
@sekretess
Egenskap: Sekretess för LISU och LUSE intygen

Bakgrund: Jag loggar in som läkare och går in på vårdtagare med sekretessmarkering.
   Givet att jag är inloggad som djupintegrerad läkare
   Och jag går in på en patient med sekretessmarkering

@varningsmeddelande-sekretessmarkering
Scenario: Varningmeddelande när man går in på patient
    Så ska en varningsruta innehålla texten "Patienten har en sekretessmarkering."
    Och ska en varningsruta innehålla texten "På grund av sekretessmarkeringen går det inte att kopiera intyg."

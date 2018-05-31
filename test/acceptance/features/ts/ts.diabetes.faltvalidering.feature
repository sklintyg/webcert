# language: sv
@FALTVALIDERING @TS_DIABETES 
Egenskap: Fältvalidering för TS Diabetes

@F.VAL-028
@F.VAL-029
Scenario:
    Givet att jag är inloggad som läkare
    Och jag går in på testpatienten för "fältvalidering"
    Och jag går in på att skapa ett "Transportstyrelsens läkarintyg diabetes" intyg
    När jag klickar på signera-knappen
    Så ska "1" valideringsfel visas med texten "Minst en behandling måste väljas."
    När jag kryssar i "Insulin"
    Så ska "1" valideringsfel visas med texten "År då behandling med insulin påbörjades måste anges."

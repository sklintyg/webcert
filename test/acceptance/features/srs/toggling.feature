#language: sv

@SRS
Egenskap: SRS ska kunna slås av och på för olika vårdenheter

@SRS-US-004 @featureToggling
Scenario: SRS ska kunna vara avaktiverad för en vårdenhet
    Givet att jag är djupintegrerat inloggad som läkare på vårdenhet "utan SRS"
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "finns i SRS"
    Så ska knappen för SRS vara i läge "gömd"


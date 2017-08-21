#language: sv

@notReady
@SRS
Egenskap: SRS ska kunna slås av och på för olika vårdenheter

@SRS-US-004
Scenario: SRS ska kunna vara avaktiverad för en vårdenhet
    Givet att jag är inloggad som läkare på en vårdenhet där SRS är aktiverat
    När jag befinner mig på ett nyskapat FK7263 intyg
    Så ska knappen för SRS inte finnas

@SRS-US-004
Scenario: SRS ska kunna vara aktiverad för en vårdenhet
    Givet att jag är inloggad som läkare på en vårdenhet där SRS inte är aktiverat
    När jag går in på ett nyskapat FK7263 intyg
    Så ska knappen för SRS finnas
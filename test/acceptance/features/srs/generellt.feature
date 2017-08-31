#language: sv

@notReady
@SRS
Egenskap: SRS ska kunna slås av och på för olika vårdenheter

@SRS-US-004
Scenario: SRS ska kunna vara avaktiverad för en vårdenhet
    Givet att jag är inloggad som läkare på en vårdenhet där SRS är aktiverat
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som finns i SRS
    Så ska knappen för SRS inte vara synlig

@SRS-US-004
Scenario: SRS ska kunna vara aktiverad för en vårdenhet
    Givet att jag är inloggad som läkare på en vårdenhet där SRS inte är aktiverat
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som finns i SRS
    Så ska knappen för SRS vara synlig
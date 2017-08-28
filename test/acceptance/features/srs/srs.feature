#language: sv

@notReady
@SRS
Egenskap: Webcert visa information från SRS i form av prediktioner på sjukskrivningslängd

Bakgrund: 
    Givet att jag är inloggad som läkare på vårdenhet med SRS
    

@SRS-US-W011
Scenario: När samtycke är givet och ytterligare frågor besvarade ska information från SRS visas.
    Givet att jag valt en patient som inte har givit samtycke till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som inte finns i SRS
    När jag klickar på knappen för SRS
    Så ska en fråga om samtycke visas
    När jag anger att patienten samtycker till SRS
    Och jag fyller i ytterligare frågor för SRS
    Så ska åtgärdsförslag från SRS-tjänsten visas
    När jag trycker på fliken "Statistik"
    Så ska statistik från SRS-tjänsten visas


@SRS-US-W01
Scenario: SRS-knappen ska bara visas när diagnos som har stöd för SRS är ifylld
    Givet att jag valt en patient som har givit samtycke till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som finns i SRS
    Så ska knappen för SRS visas i läge "stängd"
    När jag fyller i diagnoskod som inte finns i SRS
    Så ska knappen för SRS inte visas

Scenario: När en patient redan har givit sitt samtycke ska detta synas.
    Givet att patienten har givit samtycke för SRS
    Och jag har fyllt i diagnoskod
    När jag klickar på knappen för SRS
    Så ska en fråga om samtycke visas
    Och frågan om samtycke ska redan vara ifylld som "Ja"

Scenario:
    Givet att patienten har givit samtycke för SRS
    Och jag har fyllt i diagnoskod
    När jag klickar på knappen för SRS
    Och jag fyller i ytterligare frågor för SRS

@SRS-US-W01
Scenario: Användaren ska kunna visa och dölja UI-komponenter som hör till SRS
    Givet att jag valt en patient som har givit samtycke till SRS
    När jag fyller i diagnoskod som finns i SRS
    Så ska knappen för SRS visas i läge "stängd"
    När jag klickar på knappen för SRS
    Så ska knappen för SRS visas i läge "öppen"
    Och ska visas en frågepanel för SRS
    När jag klickar på texten "visa mindre"
    Så ska frågepanelen för SRS minimeras
    När jag klickar på texten "visa mer"
    Så ska frågepanelen för SRS maximeras

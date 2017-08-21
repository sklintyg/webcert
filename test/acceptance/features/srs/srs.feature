#language: sv

@notReady
@SRS
Egenskap: Webcert visa information från SRS i form av prediktioner på sjukskrivningslängd

Bakgrund: 
    Givet jag är inloggad som läkare på en vårdenhet som har SRS påslaget
    Och jag befinner mig på ett nyskapat FK7263 intyg

Scenario: Vissa fält ska ha tillgång till SRS-funktionalitet

@SRS-US-005
Scenario: När samtycke är givet och ytterligare frågor besvarade ska information från SRS visas.
    Givet att patienten inte har givit samtycke för SRS
    Och jag har fyllt i diagnoskod
    När jag klickar på knappen för SRS
    Så ska en fråga om samtycke visas
    När jag anger att patienten samtycker till SRS
    Och jag fyller i ytterligare frågor för SRS
    Så ska åtgärdsförslag från SRS-tjänsten visas
    När jag trycker på fliken "Statistik"
    Så ska statistik från SRS-tjänsten visas

Scenario: När en patient redan har givit sitt samtycke ska detta synas.
    Givet att patienten har givit samtycke för SRS
    Och jag har fyllt i diagnoskod
    När jag klickar på knappen för SRS
    Så ska en fråga om samtycke visas
    Och frågan om samtycke ska redan vara ifylld som "Ja"

Scenario: När en patient redan har vägrat ge samtycke ska detta synas.
    Givet att patienten tidigare har vägrat ge samtycke för SRS
    Och jag har fyllt i diagnoskod
    När jag klickar på knappen för SRS
    Så ska en fråga om samtycke visas
    Och frågan om samtycke ska redan vara ifylld som "Nej"

@SRS-W01.D04
@SRS-W01.D02
Scenario: Användaren ska kunna visa och dölja UI-komponenter som hör till SRS
    När jag fyllt i diagnoskod
    Så ska knappen för SRS visas i läge "stängd"
    När jag klickar på knappen för SRS
    Så ska knappen för SRS visas i läge "öppen"
    Och ska visas en frågepanel för SRS
    När jag klickar på texten "visa mindre"
    Så ska frågepanelen för SRS minimeras
    När jag klickar på texten "visa mer"
    Så ska frågepanelen för SRS maximeras

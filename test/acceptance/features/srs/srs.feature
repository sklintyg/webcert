#language: sv

@SRS
Egenskap: Webcert visa information från SRS i form av prediktioner på sjukskrivningslängd, statistikbilder och åtgärdsförslag

Bakgrund: 
    Givet att jag är djupintegrerat inloggad som läkare på vårdenhet "med SRS"
    

@SRS-US-W01
@SRS-US-W02
@SRS-US-W03
@SRS-US-W04
Scenario: När samtycke är givet och ytterligare frågor besvarade ska information från SRS visas.
    Givet en patient som "inte har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "finns i SRS"
    Och jag klickar på knappen för SRS
    Så ska en fråga om samtycke visas
    När jag anger att patienten samtycker till SRS
    Och jag fyller i ytterligare svar för SRS
    Och jag trycker på knappen "Visa"
    Så ska prediktion från SRS-tjänsten visas
    Och ska åtgärdsförslag från SRS-tjänsten visas
    När jag trycker på fliken "Statistik"
    Så ska en statistikbild från SRS-tjänsten visas


@SRS-US-W01
Scenario: SRS-knappen ska bara visas när diagnos som har stöd för SRS är ifylld
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "finns i SRS"
    Så ska knappen för SRS vara i läge "stängd"
    När jag fyller i diagnoskod som "inte finns i SRS"
    Så ska knappen för SRS vara i läge "gömd"

@SRS-US-W01
Scenario: Samtycken som patienter har givit ska lagras
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    Och jag fyllt i diagnoskod som "finns i SRS"
    När jag klickar på knappen för SRS
    Så ska en fråga om samtycke visas
    Och frågan om samtycke ska vara förifylld med "Ja"

@SRS-US-W01
Scenario: Patient som inte givit samtycke ska ha samtyckesfrågan förifyllt som "nej"
    Givet en patient som "inte har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    Och jag fyllt i diagnoskod som "finns i SRS"
    När jag klickar på knappen för SRS
    Så ska en fråga om samtycke visas
    Och frågan om samtycke ska vara förifylld med "Nej"

@SRS-US-W01
Scenario: Användaren ska kunna visa och dölja UI-komponenter som hör till SRS
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "finns i SRS"
    Så ska knappen för SRS vara i läge "stängd"
    När jag klickar på knappen för SRS
    Och ska en frågepanel för SRS "visas"
    Och ska en pil med texten "Visa mindre" visas
    När jag klickar på pilen
    Så ska frågepanelen för SRS vara "minimerad"
    Och ska en pil med texten "Visa mer" visas
    När jag klickar på pilen
    Så ska frågepanelen för SRS vara "maximerad"

@SRS-US-W02
@notReady
Scenario: Användaren ska kunna ta del av åtgärdsförslag från SRS
    Givet att jag befinner mig på ett nyskapat Läkarintyg FK 7263 för en patient som "har givit samtycke" till SRS
    När jag fyller i diagnoskod som "finns i SRS"
    Och jag klickar på knappen för SRS
    Och jag klickar på knappen för att skicka förfrågan till SRS
    Så ska tänk-på-att-åtgärder visas
    Och ska rekommenderade åtgärder visas


@SRS-US-W02
@notReady
Scenario: När åtgärdsförslag inte kan ges ska korrekt felmeddelande visas
    Givet att jag befinner mig på ett nyskapat Läkarintyg FK 7263 för en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som finns i SRS men med tekniskt fel
    Och jag fyller i ytterligare frågor för SRS
    Så ska felmeddelandet "Tekniskt fel" visas

@SRS-US-W02
@notReady
Scenario: När åtgärdsförslag saknas ska korrekt felmeddelande visas
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som finns i SRS men där åtgärdsförslag saknas
    Och jag fyller i ytterligare frågor för SRS
    Så ska felmeddelandet "Åtgärdsförslag saknas" visas

@SRS-US-W04
Scenario: När jag byter svar i diagnosspecifika frågor ska ny information från SRS visas


@SRS-US-W04
Scenario: När prediktion för en viss diagnoskod saknas ska användaren informeras.
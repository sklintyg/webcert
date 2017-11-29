#language: sv

@SRS
Egenskap: Webcert visa information från SRS i form av prediktioner på sjukskrivningslängd, statistikbilder och åtgärdsförslag

Bakgrund:
    Givet att jag är djupintegrerat inloggad som läkare på vårdenhet "med SRS"


@SRS-US-W01 @allmänt
@SRS-US-W02 @åtgärder
@SRS-US-W03 @statistik
@SRS-US-W04 @prediktion
Scenario: När samtycke är givet och ytterligare frågor besvarade ska information från SRS visas.
    Givet en patient som "inte har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "finns i SRS"
    Och jag klickar på knappen för SRS
    Så ska en fråga om samtycke visas
    När jag anger att patienten samtycker till SRS
    Och jag fyller i ytterligare svar för SRS
    Och jag klickar på pilen
    Och jag trycker på knappen "Visa"
    Så ska prediktion från SRS-tjänsten visas
    Och ska åtgärdsförslag från SRS-tjänsten visas
    När jag trycker på fliken "Statistik"
    Så ska en statistikbild från SRS-tjänsten visas för en diagnoskod som "finns i SRS"
	#Testet går igenom men ingen bild visas.


@SRS-US-W01 @allmänt
Scenario: SRS-knappen ska bara visas när diagnos som har stöd för SRS är ifylld
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "finns i SRS"
    Så ska knappen för SRS vara i läge "stängd"
    När jag fyller i diagnoskod som "inte finns i SRS"
    Så ska knappen för SRS vara i läge "gömd"

@SRS-US-W01 @allmänt
Scenario: Samtycken som patienter har givit ska lagras
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    Och jag fyllt i diagnoskod som "finns i SRS"
    När jag klickar på knappen för SRS
    Så ska en fråga om samtycke visas
    Och frågan om samtycke ska vara förifylld med "Ja"

@SRS-US-W01 @allmänt
Scenario: Patient som inte givit samtycke ska ha samtyckesfrågan förifyllt som "nej"
    Givet en patient som "inte har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    Och jag fyllt i diagnoskod som "finns i SRS"
    När jag klickar på knappen för SRS
    Så ska en fråga om samtycke visas
    Och frågan om samtycke ska inte vara förifylld med "Nej"

@SRS-US-W01 @allmänt
Scenario: Användaren ska kunna visa och dölja UI-komponenter som hör till SRS
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "finns i SRS"
    Så ska knappen för SRS vara i läge "stängd"
    När jag klickar på knappen för SRS
    Och ska en frågepanel för SRS "visas"
    När jag klickar på pilen
    Så ska frågepanelen för SRS vara "maximerad"
    När jag klickar på pilen för att minimera
    Så ska frågepanelen för SRS vara "minimerad"

@SRS-US-W02 @åtgärder @OBS-åtgärder @REK-åtgärder
Scenario: Användaren ska kunna ta del av åtgärdsförslag från SRS
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "har åtgärder"
    Och jag klickar på knappen för SRS
    Så ska REK-åtgärder från "åtgärdslista 1" visas
    Och ska OBS-åtgärder från "åtgärdslista 2" visas


@SRS-US-W02 @åtgärder
Scenario: När åtgärdsförslag inte kan ges ska korrekt felmeddelande visas
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "saknar åtgärder"
    Och jag klickar på knappen för SRS
    Så ska felmeddelandet "finns ingen SRS-information för detta fält" visas

@SRS-US-W03 @statistik
Scenario: När statistikbild för en viss diagnoskod saknas ska användaren informeras.
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "saknar statistik"
    Och jag klickar på knappen för SRS
    Och jag trycker på fliken "Statistik"
    Så ska felmeddelandet "finns ingen SRS-information för detta fält" visas

@SRS-US-W04 @prediktion
Scenario: När prediktion inte kan ges ska korrekt felmeddelande visas
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "saknar prediktion"
    Och jag klickar på knappen för SRS
    Och jag klickar på pilen
    Och jag trycker på knappen "Visa"
    Så ska felmeddelandet "finns ingen SRS-information för detta fält" visas

@SRS-US-W04 @prediktion
Scenario: Prediktion ska kunna visa ingen förhöjd risk
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "inte har förhöjd risk"
    Och jag klickar på knappen för SRS
    Och jag klickar på pilen
    Och jag trycker på knappen "Visa"
    Så ska jag få prediktion "Ingen förhöjd risk"

@SRS-US-W04 @prediktion @highRisk
Scenario: Prediktion ska kunna visa förhöjd risk
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "har förhöjd risk"
    Och jag klickar på knappen för SRS
    Och jag trycker på knappen "Visa"
    Så ska jag varnas om att "Förhöjd risk"

@SRS-US-W06 @hjälpinformation @samtycke
Scenario: Som användare vill jag få hjälpinformation (samtycke)
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "finns i SRS"
    Och jag klickar på knappen för SRS
    Och jag klickar på knappen "?" vid samtycke
    Och jag klickar på knappen "Läs mer" vid samtycke
    Så ska en ny sida öppnas och urlen innehålla "samtycke"

@SRS-US-W06 @hjälpinformation @prediktionsmodell
Scenario: Som användare vill jag få hjälpinformation (prediktionsmodell)
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "finns i SRS"
    Och jag klickar på knappen för SRS
    Och jag klickar på pilen
    Och jag trycker på knappen "Visa"
    Och jag klickar på knappen "?" vid prediktionsmeddelandet
    Och jag klickar på knappen "Läs mer" vid prediktionsmeddelandet
    Så ska en ny sida öppnas och urlen innehålla "prediktionsmodell"

@SRS-US-W06 @hjälpinformation @åtgärder
Scenario: Som användare vill jag få hjälpinformation (åtgärder)
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "finns i SRS"
    Och jag klickar på knappen för SRS
    Och jag klickar på knappen "Läs mer" vid åtgärder
    Så ska en ny sida öppnas och urlen innehålla diagnoskod som "finns i SRS"

@SRS-US-W06 @hjälpinformation @statistik
Scenario: Som användare vill jag få hjälpinformation (statistik)
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "finns i SRS"
    Och jag klickar på knappen för SRS
    Och jag trycker på fliken "Statistik"
    Och jag klickar på knappen "Läs mer" vid statistik
    Så ska en ny sida öppnas och urlen innehålla diagnoskod som "finns i SRS" med postfix "-statistik"

# language: sv
@healthcheck
Egenskap: Healthcheck-sida

Scenario: Kontrollera applikationens status
	När jag går in på healthcheck-sidan

	Så ska status för "Koppling databas" vara "OK"
	Och ska status för "Koppling ActiveMQ" vara "OK"
	Och ska status för "Koppling HSA" vara "Ej implementerat"
	Och ska status för "Koppling till Intygstjänst" vara "OK"
	Och ska status för "Koppling till Privatläkarportal" vara "OK"
	Och ska status för "Antal signerade intyg som ska skickas till Intygstjänst" vara "OK"

	
# language: sv
@BACKOFFICE
Egenskap: Skapa statistik-data

Bakgrund: Inloggad som läkare
	Givet att jag är inloggad som läkare
	
@CLEANUP
Scenariomall: [cleanup] - Ta bort intyg och utkast för <Patient>
   Givet att jag har raderat alla intyg för "<Patient>" via testAPI
   Och att jag har raderat alla utkast för "<Patient>" via testAPI

Exempel:
	| Patient |
	| 20000115-2388 |
	| 19831120-9285 |
	| 19900312-2398 |
	| 20160209-2387 |
	| 20151026-2395 |
	| 19000717-9815 |
	| 19900424-2385 |
	| 19900424-2393 |
	| 19900425-2384 |
	| 19900425-2392 |
	| 19900426-2383 |
	| 19900426-2391 |
	| 19900427-2382 |
	| 19900427-2390 |
	| 19900428-2381 |
	| 19900428-2399 |
	| 19990810-2388 |
	| 19990811-2395 |
	| 19991219-2391 |
	| 19991216-2386 |
	| 19440178-6530 |
# Webcert Notification-stub

Notification-stub är tänkt att användas för att stubba svar på notifieringar som skickas till djupintegrerade
journalsystem (```CertificateStatusUpdateForCare v3```) och kan användas för att emulera fel.

## Profiles
Stubben aktiveras med någon av följande profiler '```wc-notificationsender-stub```', '```wc-all-stubs```' eller '```dev```').
 
## URLer

Notification-stubben har fem endpoints
    
##### GET: http://localhost:${server.port}/services/api/notification-api/notifieringar/v3

Listar innehållet på samtliga notifieringar som skickats via notifierings-stubben.
Producerar JSON.

##### GET: http://localhost:${server.port}/services/api/notification-api/notifieringar/v3/stats

Listar information om notifieringar (tidpunkt och typ) för de intyg skickats via notifierings-stubben.

##### POST: http://localhost:${server.port}/services/api/notification-api/clear

Rensar informationen om notifieringar som skickats via notifierings-stubben. 

##### GET: http://localhost:${server.port}/services/api/notification-api/notifieringar/v3/emulateError

Hämtar vilket fel som emuleras av stubben. 

##### GET: http://localhost:${server.port}/services/api/notification-api/notifieringar/v3/emulateError/{errorCode}
    
Sätter vilket fel som ska emuleras av stubben. Försöker man sätta ett värde som inte finns definierat  

```{errorCode}``` kan vara något av följande:

| errorCode | Beskrivning |
| --- | --- |
| 0 | _(default)_ Inget fel kommer att emuleras. |
| 1 | Fel B. Only for ANDRAT notifications. |
| 2 | Response TechError null. |
| 3 | TechError Unspecified Service. |
| 4 | Thowing ```RuntimeException``` that should result in a 500 Server Error. |
| 5 | Fel B. For all notifications. |
    
# Licens

Copyright (C) 2018 Inera AB (http://www.inera.se)

Webcert free software: you can redistribute it and/or modify it under the terms of the GNU LESSER GENERAL PUBLIC LICENSE as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

Webcert is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU LESSER GENERAL PUBLIC LICENSE for more details.

Se även [LICENSE.md](https://github.com/sklintyg/common/blob/master/LICENSE.md).
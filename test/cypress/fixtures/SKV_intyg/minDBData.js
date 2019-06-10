skv = {
    "identitet": "Pass",
    "dödsuppgifter": {
        "dödsdatum": {
            "säkert": false,
            //Datum sätts i funktionen "sektionDödsdatumDödsplats" i dbIntyg.js
        },
        "dödsplats": "Karlstad",
        "påträffades": {
            //En av dessa ska vara true
            "sjukhus": true,
            "ordinärt": false,
            "särskilt": false,
            "okänd": false
        }
    },
    "aktivitetsbegränsning": {
        "ja": true,
        "text": "Kan inte utföra normala arbetsuppgifter. Behöver nya, mindre fysiska, uppgifter."
    },
    "utredning": {
        "ja": true,
        "text": "Beroende på om medicinerna verkar som förväntat eller inte så kan Arbetsförmedlingens planering behöva skjutas på"
    },
    "påverkan": {
        "ja": true,
        "text": "Olika typer av lyft kan förvärra skadan. Bör åtminstone till en början om möjligt sitta med mer administrativa uppgifter"
    },
    "övrigt": {
        "ja": true,
        "text": "Personen i fråga är gjord av is"
    }
}

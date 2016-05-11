javascript: (function() {
    $("input[id$='date_undersokningAvPatienten_3']").click();
    $("input[id$='date_kannedomOmPatient_8']").click();
    $("#underlagFinnsNo").prop("checked", true);
    $("#underlagFinnsNo").click();
    $("#sjukdomsforlopp").val("Har blivit sjuk, är nu ännu sjukare");
    $("#sjukdomsforlopp").change();
    $("#diagnoseCode").val("A00").change();
    $("#diagnoseDescription").val("Kolera").change();
    $("#diagnosgrund").val("I hemmet").change();
    $("#nyBedomningDiagnosgrundNo").prop("checked", true);
    $("#nyBedomningDiagnosgrundNo").click();
    $("#funktionsnedsattningIntellektuell").val("instabil").change();
    $("#").val("");
    $("#funktionsnedsattningKommunikation").val("Mycket liten måttligt stor nedsättning").change();
    $("#aktivitetsbegransning").val("Kan knappt röra sig").change();
    $("#medicinskaForutsattningarForArbete").val("I det närmaste obefintlig").change();
})();

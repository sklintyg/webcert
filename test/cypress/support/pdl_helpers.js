export function pdlEvent(env, actType, actArgs, actLevel, userIdArg, assignment, titleArg, vgId_mod, vgNamn_mod, veId_mod, 
    veNamn_mod, patientIdArg, careProviderIdArg, careProviderNameArg, careUnitIdArg, careUnitNameArg) {

    var loggHandelse = {
        activity: {
            activityType: actType,
            activityLevel: actLevel,
            activityArgs: actArgs,
        },
        user: {
            userId: userIdArg,
            assignment: assignment,
            title: titleArg,
            careProvider: {
                careProviderId: vgId_mod,
                careProviderName: vgNamn_mod
            },
            careUnit: {
                careUnitId: veId_mod,
                careUnitName: veNamn_mod
            }
        },
        resources: {
            resource: {
                patient: {
                    patientId: patientIdArg
                },
                careProvider: {
                    careProviderId: careProviderIdArg,
                    careProviderName: careProviderNameArg
                },
                careUnit: {
                    careUnitId: careUnitIdArg,
                    careUnitName: careUnitNameArg
                }
            }
        }
    }
    return loggHandelse;
}

export const enumHandelse = {
    LÄSA: "Läsa",
    SKRIVA: "Skriva",
    SIGNERA: "Signera",
    UTSKRIFT: "Utskrift"
};

export const enumHandelseArgument = {
    FKASSA: "Intyg skickat till mottagare FKASSA",
    LÄSASJF: "Läsning i enlighet med sammanhållen journalföring",
    UTSKRIFTSJF: "Intyg utskrivet. Läsning i enlighet med sammanhållen journalföring",
    UTSKRIFT: "Intyg utskrivet"
};
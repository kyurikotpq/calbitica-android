package com.calbitica.app.Models.Habitica;

public class HabiticaInfo {
//    {
//        "data": {
//            "profile": {
//                  "name": "Calbitica Test User"
//            },
//            "stats": {
//                  "lvl": 15,
//                  "class": "Rogue",
//                  "hp": 40,
//                  "exp": 317,
//                  "mp": 49,
//                  "gp": 483,
//                  "maxHealth": 50,
//                  "toNextLevel": 350,
//                  "maxMP": 52
//            },
//            "preferences": {
//                  "hair": {
//                      "color": "red",
//                      "base": 3,
//                      "bangs": 1,
//                      "beard": 0,
//                      "mustache": 0,
//                      "flower": 1
//                  },
//                  "tasks": {
//                      "groupByChallenge": false,
//                      "confirmScoreNotes": false
//                  },
//                  "size": "slim",
//                  "skin": "915533",
//                  "shirt": "blue",
//                  "chair": "none",
//                  "sleep": true,
//                  "disableClasses": false,
//                  "background": "purple"
//            },
//            "party": {
//                "quest": {
//                    "progress": {
//                        "up": 82.47907323419412,
//                        "down": 0,
//                        "collectedItems": 24
//                    },
//                    "RSVPNeeded": false,
//                    "key": null,
//                    "completed": null
//                },
//                "order": "level",
//                "orderAscending": "ascending",
//                "_id": "9ca76996-3bc3-435a-9bcd-d417791fed3f"
//            }
//        }
//    }

    private Profile profile;
    private Stats stats;
    private Preferences preferences;
    private Party party;

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }
}

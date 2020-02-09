package com.calbitica.app.Models.Habitica;

public class HabiticaQuestResponse {
    private Boolean accept;
    private String groupID;

    public Boolean getAccept() {
        return accept;
    }

    public void setAccept(Boolean accept) {
        this.accept = accept;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public HabiticaQuestResponse(Boolean accept, String groupID) {
        this.accept = accept;
        this.groupID = groupID;
    }
}

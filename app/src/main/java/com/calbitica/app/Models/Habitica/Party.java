package com.calbitica.app.Models.Habitica;

public class Party {
    private Quest quest;
    private String order;
    private String orderAscending;
    private String _id;

    public Quest getQuest() {
        return quest;
    }

    public void setQuest(Quest quest) {
        this.quest = quest;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getOrderAscending() {
        return orderAscending;
    }

    public void setOrderAscending(String orderAscending) {
        this.orderAscending = orderAscending;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }
}

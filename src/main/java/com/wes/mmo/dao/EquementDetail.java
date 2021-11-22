package com.wes.mmo.dao;

import javafx.beans.property.SimpleStringProperty;

public class EquementDetail {

    private SimpleStringProperty id;
    private SimpleStringProperty name;
    private SimpleStringProperty indexUrl;
    private SimpleStringProperty nowUser;
    private SimpleStringProperty address;
    private SimpleStringProperty contacts;
    private SimpleStringProperty order = new SimpleStringProperty("预定");

    public EquementDetail(String id, String name, String indexUrl, String nowUser, String address, String contacts){
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.indexUrl = new SimpleStringProperty(indexUrl);
        this.nowUser = new SimpleStringProperty(nowUser);
        this.address = new SimpleStringProperty(address);
        this.contacts = new SimpleStringProperty(contacts);
    }

    public String getId() {
        return id.get();
    }

    public String getName() {
        return name.get();
    }

    public String getIndexUrl() {
        return indexUrl.get();
    }

    public String getNowUser() {
        return nowUser.get();
    }

    public String getContacts() {
        return contacts.get();
    }

    public String getAddress() {
        return address.get();
    }

    public String getOrderUrl(){
        return indexUrl.get() + ".reserv";
    }

    public String getOrder() {
        return order.get();
    }
}
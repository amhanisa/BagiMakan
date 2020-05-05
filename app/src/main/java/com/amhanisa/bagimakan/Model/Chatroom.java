package com.amhanisa.bagimakan.Model;

public class Chatroom {

    private String roomId;
    private String partnerName;

    public Chatroom() {
    }

    public Chatroom(String roomId, String partnerName) {
        this.roomId = roomId;
        this.partnerName = partnerName;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }
}

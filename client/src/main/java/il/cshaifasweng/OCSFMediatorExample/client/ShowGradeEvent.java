package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Message;

public class ShowGradeEvent {
    private Message message;

    public Message getMessage() {
        return message;
    }

    public ShowGradeEvent(Message message) {
        this.message = message;
    }
}

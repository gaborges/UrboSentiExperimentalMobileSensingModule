package urbosenti.core.device.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Conversation {

    private int id;
    private String description;
    private Date finishedTime;
    private List<AgentMessage> messages;

    public Conversation(int id, String description) {
        this.id = id;
        this.description = description;
        this.messages = new ArrayList();
    }

    public Conversation() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<AgentMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<AgentMessage> messages) {
        this.messages = messages;
    }

    public Date getFinishedTime() {
        return finishedTime;
    }

    public void setFinishedTime(Date finishedTime) {
        this.finishedTime = finishedTime;
    }

}

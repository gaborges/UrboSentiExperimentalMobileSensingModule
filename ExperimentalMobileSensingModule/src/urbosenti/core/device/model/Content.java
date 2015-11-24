package urbosenti.core.device.model;

import java.util.Date;

public class Content {

    private int id;
    private java.lang.Object value;
    private Date time;
    private double score;
    private Instance monitoredInstance;
    private Parameter parameter;
    private AgentMessage message;

    public Content(int id, java.lang.Object value, Date time, double score) {
        this.id = id;
        this.value = value;
        this.time = time;
        this.score = score;
    }

    public Content(Object value, Date time) {
        this.value = value;
        this.time = time;
    }

    public Content(Object value) {
        this.value = value;
        this.time = new Date();
    }

    public Content() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public java.lang.Object getValue() {
        return value;
    }

    public void setValue(java.lang.Object value) {
        this.value = value;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Instance getMonitoredInstance() {
        return monitoredInstance;
    }

    public void setMonitoredInstance(Instance monitoredInstance) {
        this.monitoredInstance = monitoredInstance;
    }

    public AgentMessage getMessage() {
        return message;
    }

    public void setMessage(AgentMessage message) {
        this.message = message;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    public static Object parseContent(DataType dataType, Object value) {
        if (value == null || value.equals("null")) {
            return null;
        }
        switch (dataType.getId()) {
            case 1://<dataType id="1" initialValue="0">byte</dataType>
                return Byte.parseByte(value.toString());
            case 2: // <dataType id="2" initialValue="0">short</dataType>
                return Short.parseShort(value.toString());
            case 3: // <dataType id="3" initialValue="0">int</dataType>
                return Integer.parseInt(value.toString());
            case 4: // <dataType id="4" initialValue="0">long</dataType>
                return Long.parseLong(value.toString());
            case 5: // <dataType id="5" initialValue="0.0">float</dataType>
                return Float.parseFloat(value.toString());
            case 6: // <dataType id="6" initialValue="0.0">double</dataType>
                return Double.parseDouble(value.toString());
            case 7: // <dataType id="7" initialValue="false">boolean</dataType>
                return (value.toString().toLowerCase().equals("true") || value.toString().equals("1"));
            case 8: // <dataType id="8" initialValue="0">char</dataType>
                return value.toString().charAt(0);
            case 9: // <dataType id="9" initialValue="unknown">String</dataType>
                return value.toString();
            case 10: // <dataType id="10" initialValue="null">Object</dataType>
                return value;
        }
        return null;
    }

}

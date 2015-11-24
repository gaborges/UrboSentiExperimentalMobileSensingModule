/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.communication;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Guilherme
 */
public class Message  implements Serializable  {
    
    public static final int NORMAL_PRIORITY = 1;
    public static final int PREFERENTIAL_PRIORITY = 2;  
    public static final int SUBJECT_REGISTRATION = 1;
    public static final int SUBJECT_CANCEL_REGISTRATION = 2;
    public static final int SUBJECT_UPLOAD_REPORT = 3;
    public static final int SUBJECT_SYSTEM_MESSAGE = 4;
    public static final int SUBJECT_APPLICATION_MESSAGE = 5;
    
    private Address origin;
    private Address target;
    
    private int subject;
    private String contentType;
    private int priority;
    private Boolean anonymousUpload;
    private String content;
    private Date createdTime;
    private Boolean usesUrboSentiXMLEnvelope;    
    private boolean requireResponse;
    private int contentSize;
   
    public Message() {
        this.priority = Message.NORMAL_PRIORITY;
        this.usesUrboSentiXMLEnvelope = true;
        this.createdTime = new Date();
        this.anonymousUpload = false;
        this.subject = Message.SUBJECT_APPLICATION_MESSAGE;
        this.contentType = "text/xml";
        this.requireResponse = false;
    }   
         
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = removeXMLheader(content);
        this.measureContentSize();
    }

    public Integer getSubject() {
        return subject;
    }

    public void setSubject(Integer subject) {
        this.subject = subject;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getPriority() {
        return priority;
    }

    public void setNormalPriority() {
        this.priority = Message.NORMAL_PRIORITY;
    }
    
    public void setPreferentialPriority() {
        this.priority = Message.PREFERENTIAL_PRIORITY;
    }

    public boolean isUsesUrboSentiXMLEnvelope() {
        return usesUrboSentiXMLEnvelope;
    }

    public void setUsesUrboSentiXMLEnvelope(boolean usesUrboSentiXMLEnvelope) {
        this.usesUrboSentiXMLEnvelope = usesUrboSentiXMLEnvelope;
    }        
    
    @Override
    public String toString() {
        return "Message{" +", sender=" + this.origin.getAddress() + ", target=" + this.target.getAddress() + ", subject=" + subject + ", contentType=" + contentType + ", content=" + content + '}';
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public Boolean isAnonymousUpload() {
        return anonymousUpload;
    }

    public void setAnonymousUpload(Boolean anonymousUpload) {
        this.anonymousUpload = anonymousUpload;
    }

    public Address getOrigin() {
        return origin;
    }

    public void setOrigin(Address origin) {
        this.origin = origin;
    }

    public Address getTarget() {
        return target;
    }

    public void setTarget(Address target) {
        this.target = target;
    }

    public boolean isRequireResponse() {
        return requireResponse;
    }

    public void setRequireResponse(boolean requireResponse) {
        this.requireResponse = requireResponse;
    }

    private void measureContentSize(){
        this.contentSize = content.length();
    }
    
    public int getContentSize() {
        return contentSize;
    }
    
    public static String removeXMLheader(String xml){
        // remove a representação do caracter para adicionar o caractere
        xml = xml.replace("&gt;", ">");
        xml = xml.replace("&lt;", "<");
        char[] characters = xml.toCharArray();
        String newXml = "";
        boolean flagEscreve = true;
        // Busca o header
        for (int i = 0;i < characters.length;i++) {
            // verifica se é o início do header XML, se sim pula todos caracterers até o fim do header
            if(characters[i] == '<'){
                if(characters[i+1] == '?'){
                    if(characters[i+2] == 'x' && characters[i+3] == 'm' && characters[i+4] == 'l'){
                        flagEscreve = false;
                    }
                }
            }
            // escreve na variável
            if(flagEscreve){
                newXml += characters[i];
            }
            // verifica se chegou no fim do header XML
            if(!flagEscreve && characters[i]== '?'){
                if(characters[i+1]== '>'){
                    i++;
                    flagEscreve = true;
                }
            }
        }
        return newXml;
    }
    
        public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

}

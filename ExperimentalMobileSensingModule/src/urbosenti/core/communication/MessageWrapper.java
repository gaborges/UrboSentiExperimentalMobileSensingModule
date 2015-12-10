/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.communication;

import java.io.Serializable;
import java.util.Date;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;


/**
 *
 * @author Guilherme
 */
public class MessageWrapper implements Serializable  {

    private int id; // id of in te local storage system
    private Message message;
    private int timeout;
    private String envelopedMessage;
    private Date createdTime;
    private Date sentTime;
    private boolean checked; // Se foi checada pela aplica√ß„o
    private boolean sent;
    private long serviceProcessingTime;
    // CritÈrios utilizados para avalia√ß„o ao enviar a mensagem
    private int size; // number of characters
    private long responseTime; // milliseconds
    private CommunicationInterface usedCommunicationInterface;

    public MessageWrapper(Message message) {
        this.message = message;
        this.checked = false;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked() {
        this.checked = true;
    }

    public void setUnChecked() {
        this.checked = false;
    }

    public int getSize() {
        return size;
    }

    public double getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public CommunicationInterface getUsedCommunicationInterface() {
        return usedCommunicationInterface;
    }

    public void setUsedCommunicationInterface(CommunicationInterface usedCommunicationInterface) {
        this.usedCommunicationInterface = usedCommunicationInterface;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getEnvelopedMessage() {
        return envelopedMessage;
    }

    public void setEnvelopedMessage(String envelopedMessage) {
        this.envelopedMessage = envelopedMessage;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public Date getSentTime() {
        return sentTime;
    }

    public void setSentTime(Date sentTime) {
        this.sentTime = sentTime;
    }
    /*
    void build2() throws ParserConfigurationException, TransformerConfigurationException, TransformerException {
        // se a mensagem for usar o envelope.
        if (message.isUsesUrboSentiXMLEnvelope()) {
            // gerar mensagem em XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            // Criar o documento e com verte a String em DOC
            Document doc = builder.newDocument();
            Element root = doc.createElement("message"),
                    content = doc.createElement("content"),
                    header = doc.createElement("header"),
                    origin = doc.createElement("origin"),
                    target = doc.createElement("target"),
                    layer = doc.createElement("layer"),
                    uid = doc.createElement("uid"),
                    priority = doc.createElement("priority"),
                    subject = doc.createElement("subject"),
                    contentType = doc.createElement("contentType"),
                    contentSize = doc.createElement("contentSize"),
                    anonymousUpload = doc.createElement("anonymousUpload");

            // atributo requireResponse
            root.setAttribute("requireResponse", String.valueOf(message.isRequireResponse()));

            // origin
            if (Message.SUBJECT_REGISTRATION != message.getSubject()) { // Se for para registro n„o h· UID de origem
                uid.setTextContent(message.getOrigin().getUid());
                layer.setTextContent(message.getOrigin().getLayer().toString());
                origin.appendChild(uid);
                origin.appendChild(layer);                
                header.appendChild(origin);
            }
            // target
            if (Message.SUBJECT_REGISTRATION != message.getSubject()) { // Se for para registro n„o h· UID do alvo
                layer = doc.createElement("layer");
                uid = doc.createElement("uid");
                uid.setTextContent(message.getTarget().getUid());
                layer.setTextContent(String.valueOf(message.getTarget().getLayer().toString()));
                target.appendChild(uid);
                target.appendChild(layer);            
                header.appendChild(target);
            }

            // header
            contentType.setTextContent(message.getContentType());
            subject.setTextContent(String.valueOf(message.getSubject()));
            anonymousUpload.setTextContent(message.isAnonymousUpload().toString());
            priority.setTextContent(String.valueOf(message.getPriority()));
            contentSize.setTextContent(String.valueOf(message.getContent().length()));
            
            header.appendChild(priority);
            header.appendChild(subject);
            header.appendChild(contentType);
            header.appendChild(contentSize);
            header.appendChild(anonymousUpload);
            
            // content
            content.setTextContent(message.getContent());

            root.appendChild(header);
            root.appendChild(content);
            doc.appendChild(root);

            // Converter Documento para STRING
            StringWriter stw = new StringWriter();
            Transformer serializer = TransformerFactory.newInstance().newTransformer();
            serializer.transform(new DOMSource(doc), new StreamResult(stw));

            this.envelopedMessage = stw.getBuffer().toString();
        } else {
            this.envelopedMessage = message.getContent();
        }

        this.size = (this.envelopedMessage.length());
        this.createdTime = new Date();
    }
*/
    void build() throws ParserConfigurationException, TransformerConfigurationException, TransformerException {
        // se a mensagem for usar o envelope.
        if (message.isUsesUrboSentiXMLEnvelope()) {
            // gerar mensagem em XML
            // Criar o documento e com verte a String em DOC
            //RootElement root = new RootElement("message");
            
            String mensagem = "<message requireResponse=\""+String.valueOf(message.isRequireResponse())+"\" >"+  // atributo requireResponse
			"<header>";
		         // origin
			if (Message.SUBJECT_REGISTRATION != message.getSubject()) { // Se for para registro n„o h· UID de origem
		        mensagem = mensagem + 
		        	"<origin>"+
			        	"<uid>"+message.getOrigin().getUid()+"</uid>"+
			        	"<layer>"+message.getOrigin().getLayer().toString()+"</layer>"+
		       		"</origin>";
		    }
			// target
		    if (Message.SUBJECT_REGISTRATION != message.getSubject()) { // Se for para registro n„o h· UID do alvo
		    	mensagem = mensagem + 
					"<target>" +
				    	"<uid>"+message.getTarget().getUid()+"</uid>"+
				    	"<layer>"+String.valueOf(message.getTarget().getLayer().toString())+"</layer>" +
					"</target>";
		    }
		    mensagem = mensagem +
					"<priority>"+String.valueOf(message.getPriority())+"</priority>"+
					"<subject>"+String.valueOf(message.getSubject())+"</subject>"+
					"<contentType>"+message.getContentType()+"</contentType>"+
					"<contentSize>"+String.valueOf(message.getContent().length())+"</contentSize>"+
					"<anonymousUpload>"+message.isAnonymousUpload().toString()+"</anonymousUpload>"+
				"</header>"+
				"<content>"+message.getContent()+"</content>"+
			"</message>";
            this.envelopedMessage = mensagem;
        } else {
            this.envelopedMessage = message.getContent();
        }

        this.size = (this.envelopedMessage.length());
        this.createdTime = new Date();
    }
    
    public void cleanEnvelopedMessage(){
    	this.envelopedMessage = "";
    }

    public static MessageWrapper createAndBuild(Message m) throws ParserConfigurationException, TransformerConfigurationException, TransformerException {
        MessageWrapper messageWrapper = new MessageWrapper(m);
        messageWrapper.build();
        return messageWrapper;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public long getServiceProcessingTime() {
        return serviceProcessingTime;
    }

    public void setServiceProcessingTime(long serviceProcessingTime) {
        this.serviceProcessingTime = serviceProcessingTime;
    }
    
    public String getTargetAddress(){
        return message.getTarget().getAddress();
    }

    @Override
    public String toString() {
        return "MessageWrapper{" + "id=" + id + ", timeout=" + timeout + ", createdTime=" + createdTime + ", sentTime=" + sentTime + ", checked=" + checked + '}';
    }
    
}

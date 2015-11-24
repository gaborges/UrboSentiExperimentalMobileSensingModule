/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.communication.receivers;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import urbosenti.core.communication.CommunicationManager;
import urbosenti.core.communication.PushServiceReceiver;

/**
 *
 * @author Guilherme
 */
public class SocketPushServiceReceiver extends PushServiceReceiver {

    private Integer port;
    private ServerSocket serverSocket;

    public SocketPushServiceReceiver(CommunicationManager communicationManager) {
        super(communicationManager);
        super.setId(1);
        super.setDescription("Socket Input Interface");
        this.port = 55666;
    }

    @Override
    public void stop() {
        super.stop(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        try {            
            // When have a new message
            String message="";
//                    = "<message>"
//                    + "	<header>"
//                    + "		<origin>"
//                    + "			<uid>11XYZ</uid>"
//                    + "			<layer>2</layer>"
//                    + "               </origin>"
//                    + "               <target>"
//                    + "			<uid>22XYZ</uid>"
//                    + " 			<layer>2</layer>"
//                    + "               </target>"
//                    + "               <priority>1</priority>"
//                    + "               <subject>4</subject>"
//                    + "               <contentType>text/xml</contentType>"
//                    + "               <contentSize>29</contentSize>"
//                    + "               <anonymousUpload>false</anonymousUpload>"
//                    + "	</header>\n"
//                    + "	<content>message according the subject</content>"
//                    + "</message>";
//            super.communicationManager.newPushMessage("http://exemplo:8084/TestServer/webresources/test/return", message);
            while (this.getStatus() == STATUS_LISTENING) {
                if(!serverSocket.isBound()){
                    serverSocket = new ServerSocket(port);
                    this.getInterfaceConfigurations().put("ipv4Address",InetAddress.getLocalHost().getHostAddress());
                    super.communicationManager.updateInputCommunicationInterfaceConfiguration(this,this.getInterfaceConfigurations());
                }
                Socket accept = this.serverSocket.accept();
                DataInputStream dataInputStream = new DataInputStream(accept.getInputStream());
                message = dataInputStream.readUTF();
                dataInputStream.close();
                accept.close();
                super.communicationManager.newPushMessage(accept.getInetAddress().getHostAddress(), message);
            }
        } catch (IOException ex) {
            Logger.getLogger(SocketPushServiceReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void addressDiscovery() throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.getInterfaceConfigurations().put("ipv4Address",getLocalIpAddress());
        this.getInterfaceConfigurations().put("port", String.valueOf(serverSocket.getLocalPort()));
    }
    
    public String getLocalIpAddress() throws SocketException {
        for (Enumeration<NetworkInterface> en = NetworkInterface
                .getNetworkInterfaces(); en.hasMoreElements();) {
            NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf
                    .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                InetAddress inetAddress = enumIpAddr.nextElement();
                if (!inetAddress.isLoopbackAddress()) {
                    return inetAddress.getHostAddress().toString();
                }
            }
        }
        return "";
    }

}

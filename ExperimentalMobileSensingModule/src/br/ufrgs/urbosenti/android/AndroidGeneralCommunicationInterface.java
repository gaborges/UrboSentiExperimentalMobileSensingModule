package br.ufrgs.urbosenti.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import urbosenti.core.communication.CommunicationInterface;
import urbosenti.core.communication.CommunicationManager;
import urbosenti.core.communication.MessageWrapper;
import urbosenti.core.communication.interfaces.WiredCommunicationInterface;

public class AndroidGeneralCommunicationInterface extends CommunicationInterface {

    private Context context;
    ConnectivityManager connMgr;
    NetworkInfo networkInfo;
    
	public AndroidGeneralCommunicationInterface(Context context) {
        super();
        setId(1);
        setName("Wired Interface");
        setUsesMobileData(false);
        setStatus(CommunicationInterface.STATUS_UNAVAILABLE);
        this.context = context;
        connMgr = (ConnectivityManager) 
        		context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public boolean testConnection() throws IOException, UnsupportedOperationException {
        // URL do destino escolhido
        //URL url = new URL("http://www.google.com");

        // abre a conexão
        //HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();

           // tenta buscar conteúdo da URL
        // se não tiver conexão, essa linha irá falhar
        //urlConnect.connect();
        //urlConnect.disconnect();        
         networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
           //Object objData = urlConnect.getContent();
    }

    @Override
    public boolean testConnection(URL url) throws IOException, UnsupportedOperationException {
        try {
            // abre a conexão
            HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();
            // tenta buscar conteúdo da URL
            // se não tiver conexão, essa linha irá falhar
            urlConnect.connect();
            urlConnect.disconnect();
        } catch (UnknownHostException ex) {
            Logger.getLogger(WiredCommunicationInterface.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(WiredCommunicationInterface.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    @Override
    public boolean connect(String address) throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean disconnect() throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isAvailable() throws IOException, UnsupportedOperationException {
        setStatus(CommunicationInterface.STATUS_AVAILABLE);
        return true;
    }

    @Override
    public Object sendMessageWithResponse(CommunicationManager communicationManager, MessageWrapper messageWrapper) throws SocketTimeoutException, IOException {
        String result = "";
        //URL url = new URL("http://143.54.12.47:8084/TestServer/webresources/test/return");

        HttpURLConnection conn = (HttpURLConnection) (new URL(messageWrapper.getMessage().getTarget().getAddress())).openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        //conn.setRequestProperty("Accept", "application/json");
        //conn.setRequestProperty("Content-Type", "text/plain");
        conn.setRequestProperty("Content-Type", messageWrapper.getMessage().getContentType());
        // Se possuí timeout customizado o utiliza
        if (messageWrapper.getTimeout() > 0) {
            conn.setReadTimeout(messageWrapper.getTimeout());
        }
        conn.connect();
        OutputStream os = conn.getOutputStream();
        os.write(messageWrapper.getEnvelopedMessage().getBytes());
        os.flush();
        // HttpURLConnection.HTTP_NOT_FOUND --- Posso usar para um evento de endereço não existe
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
        	throw new IOException("Failed : HTTP error code : "
                    + conn.getResponseCode());
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(
                (conn.getInputStream())));
        String output;
        while ((output = br.readLine()) != null) {
            result += output;
        }
        br.close();
        conn.disconnect();
        return result;
    }

    @Override
    public boolean sendMessage(CommunicationManager communicationManager, MessageWrapper messageWrapper) throws SocketTimeoutException, IOException {
        //String result = "";
        //URL url = new URL(messageWrapper.getTargetAddress());
        //URL url = new URL("http://143.54.12.47:8084/TestServer/webresources/test");
        HttpURLConnection conn = (HttpURLConnection) (new URL(messageWrapper.getMessage().getTarget().getAddress())).openConnection();
        // Se possuí timeout customizado o utiliza
        if (messageWrapper.getTimeout() > 0) {
            conn.setReadTimeout(messageWrapper.getTimeout());
        }
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        //conn.setRequestProperty("Accept", "application/json");
        //conn.setRequestProperty("Content-Type", "text/plain");
        conn.setRequestProperty("Content-Type", messageWrapper.getMessage().getContentType());
        conn.connect();
        OutputStream os = conn.getOutputStream();
        os.write(messageWrapper.getEnvelopedMessage().getBytes());
        os.flush();

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK && conn.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT) {
            throw new IOException("Failed : HTTP error code : "
                    + conn.getResponseCode());
        }
        conn.disconnect();
        // Adicionar métricas de avaliação **************************************8
        return true;
    }

    @Override
    public Object receiveMessage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object receiveMessage(int timeout) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

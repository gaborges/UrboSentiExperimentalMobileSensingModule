/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufrgs.urbosenti.test;

import java.io.FileReader;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import urbosenti.core.communication.Address;
import urbosenti.core.communication.Message;
import urbosenti.core.device.DeviceManager;
import urbosenti.core.events.Event;
import urbosenti.core.events.SystemEvent;

/**
 *
 * @author Guilherme
 */
public class TestCommunication {

    private DeviceManager deviceManager;
    private Boolean end;
    private final Boolean lock;

    public TestCommunication(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
        lock = true;
    }

    public void test1() {
            // Envio de mensagem sem retorno
        // testaEnvioMensagemSemRetorno(); // OK

        // Envio de mensagem com retorno
        // testaEnvioMensagemComRetorno();
        // Teste do serviço de upload - ok
        testaUploadServer();

    }

    public void test2(long interval,long experimentalTime) {
        
        end = false;
        FileReader fr = null;
        final Timer timer = new Timer("Tester");
        long endInterval = 10000L; //10s
        long stopTime = 57600000L; //16h
        
        if(interval > 0){
            endInterval = interval;
        }
        
        if(experimentalTime > 0){
            stopTime = experimentalTime;
        }
        
        //long endInterval = 2831650;
        final String data = "14353,20,Walking,324212291000,5.41,13.21,-4.630918\n"
                + "14354,20,Walking,324262248000,6.74,8.16,0.95342433\n"
                + "14355,20,Walking,324312297000,5.33,5.37,-2.7921712\n"
                + "14356,20,Walking,324362254000,3.38,8.54,-1.525479\n"
                + "14357,20,Walking,324412212000,1.73,9.11,-0.9942854\n"
                + "14358,20,Walking,324462321000,3.95,9.47,-1.7297841\n"
                + "14359,20,Walking,324512248000,8.77,9.51,-2.2609777\n"
                + "14360,20,Walking,324562297000,9.43,12.07,-1.3756552\n"
                + "14361,20,Walking,324612254000,7.63,12.45,2.152015\n"
                + "14362,20,Walking,324662303000,5.63,10.57,0.95342433\n"
                + "14363,20,Walking,324712230000,1.73,16.55,-7.8589406\n"
                + "14364,20,Walking,324762279000,4.33,15.24,-0.5720546\n"
                + "14365,20,Walking,324812205000,6.13,6.78,-2.7921712\n"
                + "14366,20,Walking,324862285000,0.08,3.99,-2.3018389\n"
                + "14367,20,Walking,324912242000,-0.46,3.64,-1.9477097\n"
                + "14368,20,Walking,324962260000,-1.46,3.26,-0.7218784\n"
                + "14369,20,Walking,325012279000,4.99,4.37,3.8273177\n"
                + "14370,20,Walking,325062297000,16.51,14.48,11.182305\n"
                + "14371,20,Walking,325112285000,8.12,7.06,5.7477865\n"
                + "14372,20,Walking,325162242000,2.98,10.61,-1.334794\n"
                + "14373,20,Walking,325212291000,0.76,14.25,0.23154591\n"
                + "14374,20,Walking,325262218000,-0.34,5.9,-7.7363577\n"
                + "14375,20,Walking,325312266000,3.49,11.92,-6.851035\n"
                + "14376,20,Walking,325362254000,8.89,12.18,1.0351465\n"
                + "14377,20,Walking,325412303000,5.01,4.4,-2.0294318\n"
                + "14378,20,Walking,325462230000,3.53,7.31,-1.4982382\n"
                + "14379,20,Walking,325512309000,2.53,8.89,-1.1441092\n"
                + "14380,20,Walking,325562266000,4.63,9.66,-1.56634\n"
                + "14381,20,Walking,325612254000,7.27,8.16,-1.7978859\n"
                + "14382,20,Walking,325662303000,9.04,10.57,-2.7513103\n"
                + "14383,20,Walking,325712230000,7.67,13.33,1.0760075\n"
                + "14384,20,Walking,325762340000,9.7,9.08,2.9556155\n"
                + "14385,20,Walking,325812694000,3.49,15.17,-7.9270425\n"
                + "14386,20,Walking,325862376000,2.87,17.01,-1.56634\n"
                + "14387,20,Walking,325912212000,6.93,9.34,-1.56634\n"
                + "14388,20,Walking,325962321000,3.34,4.4,-3.568531\n"
                + "14389,20,Walking,326012279000,-0.84,4.75,-1.4573772\n"
                + "14390,20,Walking,326062297000,-1.73,3.83,-1.6889231\n"
                + "14391,20,Walking,326112224000,1.38,4.1,1.2258313\n"
                + "14392,20,Walking,326162273000,10.27,7.82,6.4424243\n"
                + "14393,20,Walking,326212260000,17.01,16.17,6.851035\n"
                + "14394,20,Walking,326262218000,4.6,-0.72,2.4516625\n"
                + "14395,20,Walking,326262218000,4.6,-0.72,2.4516625\n"
                + "14396,20,Walking,326342662000,2.56,16.21,0.9942854\n"
                + "14397,20,Walking,326342662000,2.56,16.21,0.9942854\n"
                + "14398,20,Walking,0,0,0,0.0\n"
                + "14399,20,Walking,0,0,0,0.0\n"
                + "14400,20,Walking,326652385000,3.11,9.08,-0.9942854\n"
                + "14401,20,Walking,326702281000,5.22,8.62,-3.255263\n"
                + "14402,20,Walking,326752238000,8.81,9.53,-1.0351465\n"
                + "14403,20,Walking,326752238000,8.81,9.53,-1.0351465\n"
                + "14404,20,Walking,326752238000,8.81,9.53,-1.0351465\n"
                + "14405,20,Walking,326902354000,7.27,10.5,-0.29964766\n"
                + "14406,20,Walking,326952342000,3.02,17.58,-7.6273947\n"
                + "14407,20,Walking,327002238000,6.89,15.85,-0.42223078\n"
                + "14408,20,Walking,327052287000,5.86,6.13,-4.0588636\n"
                + "14409,20,Walking,327102214000,-1.27,4.06,-2.5333846\n"
                + "14410,20,Walking,327152232000,-2.45,2.91,-1.7570249\n"
                + "14411,20,Walking,327202281000,-1.95,2.79,-0.0\n"
                + "14412,20,Walking,327252269000,5.9,4.63,5.134871\n"
                + "14413,20,Walking,327302287000,19.23,14.63,8.689782\n"
                + "14414,20,Walking,327352275000,6.93,6.21,5.2165933\n"
                + "14415,20,Walking,327402263000,3.3,9.51,-1.0760075\n"
                + "14416,20,Walking,327452251000,5.75,13.99,1.4573772\n"
                + "14417,20,Walking,327502269000,-0.23,7.08,-6.701211\n"
                + "14418,20,Walking,327552257000,2.56,8.47,-6.238119\n"
                + "14419,20,Walking,327602244000,2.91,14.56,-2.7513103\n"
                + "14420,20,Walking,327652202000,4.29,5.56,-1.7978859\n"
                + "14421,20,Walking,327702281000,4.56,4.56,-3.9771416\n"
                + "14422,20,Walking,327752238000,4.48,9.77,-1.879608\n"
                + "14423,20,Walking,327802287000,5.05,8.62,-2.3426998\n"
                + "14424,20,Walking,327852244000,6.28,7.27,-2.3426998\n"
                + "14425,20,Walking,327902293000,7.86,11.54,-3.2961242\n"
                + "14426,20,Walking,327952251000,10.99,9.77,-0.5720546\n"
                + "14427,20,Walking,328002269000,9,10.23,3.405087\n"
                + "14428,20,Walking,328052257000,6.85,12.6,-5.7886477\n"
                + "14429,20,Walking,328102306000,3.99,15.89,-5.7886477\n"
                + "14430,20,Walking,328152202000,6.02,14.67,-1.8387469\n"
                + "14431,20,Walking,328202281000,5.33,4.14,-3.8273177\n"
                + "14432,20,Walking,328252238000,0.72,1.14,-2.7513103\n"
                + "14433,20,Walking,328302287000,-1.04,4.67,-2.7240696\n"
                + "14434,20,Walking,328352214000,0.53,5.71,-0.9125633\n"
                + "14435,20,Walking,328402263000,8.16,7.4,4.7126403\n"
                + "14436,20,Walking,328452251000,17.77,12.53,11.182305\n"
                + "14437,20,Walking,328502269000,4.33,8.73,4.3312707\n"
                + "14438,20,Walking,328552257000,9.08,11.75,-0.61291564\n"
                + "14439,20,Walking,328602336000,-1.5,7.86,-6.5922484\n"
                + "14440,20,Walking,328652110000,1.38,8.73,-7.04172\n"
                + "14441,20,Walking,328702312000,4.4,10.08,-4.903325\n"
                + "14442,20,Walking,328752238000,9.58,8.69,-2.6832085\n"
                + "14443,20,Walking,328752238000,9.58,8.69,-2.6832085\n"
                + "14444,20,Walking,328802318000,7.21,4.29,-1.1849703\n"
                + "14445,20,Walking,328862315000,2.72,8.73,-0.50395286\n"
                + "14446,20,Walking,328922313000,6.28,8.24,-2.5606253\n"
                + "14447,20,Walking,328972240000,7.63,7.67,-2.6832085\n"
                + "14448,20,Walking,329022319000,9.15,9.51,-3.9499009\n"
                + "14449,20,Walking,329072246000,9.47,12.15,-0.46309182\n"
                + "14450,20,Walking,329122325000,10.34,10.04,2.982856\n"
                + "14451,20,Walking,329172252000,6.63,12.15,-7.6273947\n"
                + "14452,20,Walking,329222301000,3.6,16.78,-3.5276701\n"
                + "14453,20,Walking,329272258000,5.48,11.07,-1.1168685\n"
                + "14454,20,Walking,329322307000,5.13,2.79,-3.1054392\n"
                + "14455,20,Walking,329372264000,-0.53,3.87,-1.9885708\n"
                + "14456,20,Walking,329422313000,-1.27,4.06,-1.6480621\n"
                + "14457,20,Walking,329472240000,1.65,4.44,0.95342433\n"
                + "14458,20,Walking,329522319000,11.37,6.85,6.2108784\n"
                + "14459,20,Walking,329572246000,18.01,14.21,7.7772183\n"
                + "14460,20,Walking,329622325000,2.98,3.53,3.1054392\n"
                + "14461,20,Walking,329672252000,6.82,14.75,-0.6946377\n"
                + "14462,20,Walking,329722301000,-3.3,8.58,-5.134871\n"
                + "14463,20,Walking,329772227000,1.5,8.35,-6.701211\n"
                + "14464,20,Walking,329822307000,7.46,12.72,-7.6546354\n"
                + "14465,20,Walking,329872233000,7.78,6.66,1.2258313\n"
                + "14466,20,Walking,329922313000,6.47,4.79,-1.9477097\n"
                + "14467,20,Walking,329972270000,5.6,7.12,-0.7627395\n"
                + "14468,20,Walking,330022319000,4.4,9.15,0.53119355\n"
                + "14469,20,Walking,330072246000,6.02,7.12,-3.1054392\n"
                + "14470,20,Walking,330122325000,9.58,7.55,-3.3642259\n"
                + "14471,20,Walking,330172252000,10.69,11.14,-0.27240697\n"
                + "14472,20,Walking,330222331000,9.72,10.42,2.5333846\n"
                + "14473,20,Walking,330272349000,7.4,10.31,-0.46309182\n"
                + "14474,20,Walking,330322307000,4.56,18.28,-6.6331096\n"
                + "14475,20,Walking,330372233000,6.63,13.33,-0.27240697\n"
                + "14476,20,Walking,330422313000,7.74,3.95,-3.255263\n"
                + "14477,20,Walking,330472270000,0.15,4.6,-2.5333846\n"
                + "14478,20,Walking,330522319000,-1.33,4.99,-2.2609777\n"
                + "14479,20,Walking,330572246000,-1.12,3.76,-0.50395286\n"
                + "14480,20,Walking,330622356000,4.94,3.34,3.568531\n"
                + "14481,20,Walking,330672252000,17.73,11.88,10.228881\n"
                + "14482,20,Walking,330722301000,10.61,6.74,5.134871\n"
                + "14483,20,Walking,330772227000,2.6,7.74,-0.19068487\n"
                + "14484,20,Walking,330822307000,4.75,15.94,0.10896278\n"
                + "14485,20,Walking,330872233000,-0.84,5.94,-7.164303\n"
                + "14486,20,Walking,330922343000,5.6,10.88,-7.8589406\n"
                + "14487,20,Walking,330972240000,8.54,10.23,-0.6537767\n"
                + "14488,20,Walking,331022288000,5.41,5.18,-0.8036005\n"
                + "14489,20,Walking,331072246000,4.56,6.63,-1.4165162\n"
                + "14490,20,Walking,331122295000,4.48,8.73,-0.53119355\n"
                + "14491,20,Walking,331172252000,5.37,8.39,-1.7570249\n"
                + "14492,20,Walking,331222301000,7.44,7.82,-1.3756552\n"
                + "14493,20,Walking,331272227000,10.69,7.67,-4.0180025\n"
                + "14494,20,Walking,331322307000,9.28,12.98,0.42223078\n"
                + "14495,20,Walking,331372264000,9.62,8.43,2.7240696\n"
                + "14496,20,Walking,331422313000,5.24,12.98,-4.5900574\n"
                + "14497,20,Walking,331472240000,4.29,16.78,-3.1463003\n"
                + "14498,20,Walking,331522319000,7.4,11.14,-1.0351465\n"
                + "14499,20,Walking,331572246000,6.09,3.76,-3.7864566\n"
                + "14500,20,Walking,331622569000,-0.65,5.33,-2.070293\n"
                + "14501,20,Walking,331682262000,-1.65,4.52,-1.9885708\n"
                + "14502,20,Walking,331732219000,1.12,3.15,1.2258313\n"
                + "14503,20,Walking,331782298000,10.8,6.21,7.5456724\n"
                + "14504,20,Walking,331832286000,17.16,15.09,6.891896\n"
                + "14505,20,Walking,331882304000,3.57,3.34,3.1463003\n"
                + "14506,20,Walking,331932292000,8.89,17.08,-1.7570249\n"
                + "14507,20,Walking,331982280000,-4.21,5.79,-6.2108784\n"
                + "14508,20,Walking,332032298000,2.83,9.66,-7.04172\n"
                + "14509,20,Walking,332082255000,7.01,11.69,-2.5606253\n"
                + "14510,20,Walking,332132304000,7.74,5.41,-0.9942854\n"
                + "14511,20,Walking,332182262000,6.47,6.05,-1.1441092\n"
                + "14512,20,Walking,332232310000,5.43,7.21,-1.3075534\n"
                + "14513,20,Walking,332282237000,3.79,7.93,-1.3075534\n"
                + "14514,20,Walking,332332317000,5.41,8.35,-2.0294318\n"
                + "14515,20,Walking,332382274000,8.69,7.08,-3.486809\n"
                + "14516,20,Walking,332432292000,10.92,11.22,-2.6423476\n"
                + "14517,20,Walking,332482249000,9.28,11.37,-0.23154591\n"
                + "14518,20,Walking,332532298000,8.66,8.47,3.2961242\n"
                + "14519,20,Walking,332582225000,4.18,14.48,-7.6954966\n"
                + "14520,20,Walking,332632274000,4.25,15.43,-2.2609777\n"
                + "14521,20,Walking,332682292000,8.01,8.54,-1.920469\n"
                + "14522,20,Walking,332732219000,2.79,3.72,-3.486809\n"
                + "14523,20,Walking,332782298000,-0.53,5.18,-2.3699405\n"
                + "14524,20,Walking,332832286000,-1.65,4.48,-0.3405087\n"
                + "14525,20,Walking,332832286000,-1.65,4.48,-0.3405087\n"
                + "14526,20,Walking,332942271000,14.33,7.16,8.349273\n"
                + "14527,20,Walking,332992259000,12.37,8.66,4.440233\n"
                + "14528,20,Walking,333042277000,3.72,11.6,0.19068487\n"
                + "14529,20,Walking,333092296000,1.33,12.49,-3.5276701\n"
                + "14530,20,Walking,333142253000,-0.53,4.9,-6.66035\n"
                + "14531,20,Walking,333192302000,5.22,11.03,-6.891896\n"
                + "14532,20,Walking,333242198000,7.82,9.81,0.313268\n"
                + "14533,20,Walking,333292338000,6.21,5.6,-1.4982382\n"
                + "14534,20,Walking,333342204000,5.56,6.97,-1.607201\n"
                + "14535,20,Walking,333392314000,4.99,7.67,-0.6537767\n"
                + "14536,20,Walking,333442210000,4.4,8.01,-2.070293\n"
                + "14537,20,Walking,333492290000,6.66,8.66,-2.8330324\n"
                + "14538,20,Walking,333542216000,10.65,8.5,-3.486809\n"
                + "14539,20,Walking,333592296000,11.37,11.65,0.9942854\n"
                + "14540,20,Walking,333642223000,9.19,8.08,2.6832085\n"
                + "14541,20,Walking,333642223000,9.19,8.08,2.6832085\n"
                + "14542,20,Walking,333712443000,4.75,15.55,-8.267551\n"
                + "14543,20,Walking,333762248000,6.63,15.17,-0.53119355\n"
                + "14544,20,Walking,333812297000,6.4,7.86,-3.0237172\n"
                + "14545,20,Walking,333862285000,1.5,3.53,-3.445948\n"
                + "14546,20,Walking,333912273000,-1.14,4.75,-1.7978859\n"
                + "14547,20,Walking,333962321000,-1.57,4.6,-0.19068487\n"
                + "14548,20,Walking,334012279000,4.21,2.96,2.982856\n"
                + "14549,20,Walking,334062297000,16.74,10.46,10.378705\n"
                + "14550,20,Walking,334112285000,10.76,7.82,4.7943625\n"
                + "14551,20,Walking,334162303000,5.67,12.41,0.6946377\n"
                + "14552,20,Walking,334212260000,-0.5,9.23,-4.2495484";
   
            // Envio de mensagem sem retorno
        // testaEnvioMensagemSemRetorno(); // OK
        // Envio de mensagem com retorno
        // testaEnvioMensagemComRetorno();
        // Teste do serviço de upload - ok
        //testaUploadServer();
        //final File simulatedSet = new File("user20.csv");
        // simulatedSet.
        //fr = new FileReader(simulatedSet);
        //final BufferedReader lerArq = new BufferedReader(fr);
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    ///String line = lerArq.readLine();
//                        if (line == null) {
//                            synchronized (lock) {
//                                end = true;
//                                lock.notifyAll();
//                            }
//                            timer.cancel();
//                        } else {
                    Message m = new Message();
                    //m.setContent(line);
                    m.setContent(data);
                    deviceManager.getCommunicationManager().addReportToSend(m);
//                        }
                    //System.out.println("Time: "+new Date().getTime());
                } catch (IOException ex) {
                    Logger.getLogger(TestCommunication.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(TestCommunication.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, 50, endInterval);
        final Timer endTimer = new Timer("Experiment end timer");
        endTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				deviceManager.stopUrboSentiServices();
				System.out.println("Fim experimento: " + (new Date()).getTime());
				String currentData = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date());
		        System.out.println("Fim experimento: " + currentData);	
		        Log.d("DEBUG", "Fim experimento: " + currentData);
		        timer.cancel();
			}
		}, new Date((System.currentTimeMillis()+experimentalTime)));
//        while (true) {
//            synchronized (lock) {
//                if (end == true) {
//                    break;
//                } else {
//                    try {
//                        lock.wait();
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(TestCommunication.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            }
//        }
    }

    public void testaEnvioMensagemSemRetorno() {
        // Envio normal de mensagem sem retorno
        Address target = new Address("http://localhost:8090/Test2Server/webresources/generic");
        target.setUid("12345");

        Address origin = new Address();
        origin.setLayer(Address.LAYER_SYSTEM);
        origin.setUid("6666");

        Message m = new Message();
        m.setTarget(target);
        m.setOrigin(origin);
        m.setSubject(Message.SUBJECT_APPLICATION_MESSAGE);
        m.setContentType("application/xml");
        m.setContent("oiiiiii");
        try {
            deviceManager.getCommunicationManager().sendMessage(m);
        } catch (java.net.ConnectException ex) {
            System.out.println("Host não acessível!");
        } catch (SocketTimeoutException ex) {
            Logger.getLogger(TestCommunication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TestCommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void testaEnvioMensagemComRetorno() {
        Address target = new Address("http://localhost:8090/Test2Server/webresources/generic");
        target.setUid("12345");

        Address origin = new Address();
        origin.setUid("6666");

        Message m = new Message();
        m.setTarget(target);
        m.setOrigin(origin);
        m.setSubject(Message.SUBJECT_APPLICATION_MESSAGE);
        m.setContentType("application/xml");
        m.setContent("oi22222");

        String result = "";
        try {
            result = deviceManager.getCommunicationManager().sendMessageWithResponse(m);
        } catch (java.net.SocketTimeoutException ex) {
            Logger.getLogger(TestCommunication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TestCommunication.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Result: " + result);
    }

    public void testaUploadServer() {
//        // Serviço do report
//        Service service = new Service();
//        service.setServiceUID("666");
//        service.setApplicationUID("333");
//        service.setAddress("http://localhost:8090/Test2Server/webresources/generic");
//        // adiciona o serviço       
//        deviceManager.getCommunicationManager().addUploadServer(service);
//        
        // Inicia o serviço de envio de mensagens
//        Thread t = new Thread(deviceManager.getCommunicationManager());
//        t.start();        

        // Adiciona a origem, e é somente necessário se for uma mensagem de 
        // sistema, caso não seja somente a mensagem é necessária
//        Address origin = new Address();
//        origin.setLayer(Address.LAYER_SYSTEM);
        // Cria a mensagem
        Message m = new Message();
        //m.setOrigin(origin);
        //m.setSubject(Message.SUBJECT_UPLOAD_REPORT);
        //m.setContentType("application/xml");
        m.setContent("oiiiiii");

        try {
            // Envia o relato
            deviceManager.getCommunicationManager().addReportToSend(m);
        } catch (SQLException ex) {
            Logger.getLogger(TestCommunication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(TestCommunication.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}

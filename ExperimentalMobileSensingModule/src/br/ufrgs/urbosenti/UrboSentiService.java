package br.ufrgs.urbosenti;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import br.ufrgs.urbosenti.android.AndroidOperationalSystemDiscovery;
import br.ufrgs.urbosenti.android.ConcreteApplicationHandler;
import br.ufrgs.urbosenti.android.AndroidGeneralCommunicationInterface;
import br.ufrgs.urbosenti.android.SQLiteAndroidDatabaseHelper;
import br.ufrgs.urbosenti.test.TestCommunication;
import br.ufrgs.urbosenti.test.TestECADiagnosisModel;
import br.ufrgs.urbosenti.test.TestManager;
import br.ufrgs.urbosenti.test.TestStaticPlanningModel;
import urbosenti.core.communication.Message;
import urbosenti.core.communication.PushServiceReceiver;
import urbosenti.core.communication.receivers.SocketPushServiceReceiver;
import urbosenti.core.device.DeviceManager;

public class UrboSentiService extends Service {

	private DeviceManager deviceManager;
	private String args[];
	private TestManager testManager;

	public UrboSentiService() {

	}

	@Override
	public IBinder onBind(Intent intent) {
		args = intent.getStringArrayExtra("args");
		deviceManager = new DeviceManager(); // Objetos Core j� est�o
		// incanciados internamente
		if (args[1].equals("1") && args.length == 5) {
			// se n�o tem n�o habilita adapta��o, sen�o faz adapta��o
			if (!args[4].trim().equals("no") && !args[4].trim().equals("n")) {
				// adapta��o
				deviceManager.enableAdaptationComponent(); // Habilita
															// componente de
															// adapta��o
				// adiciona os tradadores de evento cursomisados
				deviceManager.getAdaptationManager().setDiagnosisModel(new TestECADiagnosisModel(deviceManager));
				deviceManager.getAdaptationManager().setPlanningModel(new TestStaticPlanningModel(deviceManager));
			}
		} else {
			deviceManager.enableAdaptationComponent(); // Habilita componente de
														// adapta��o
			// adiciona os tradadores de evento cursomisados
			deviceManager.getAdaptationManager().setDiagnosisModel(new TestECADiagnosisModel(deviceManager));
			deviceManager.getAdaptationManager().setPlanningModel(new TestStaticPlanningModel(deviceManager));
		}
		try {
			testManager = null;
			if (args.length >= 5 && Integer.parseInt(args[1]) != 1) { // tem 5
																		// ou
																		// mais
																		// itens,
																		// ent�o
																		// usa o
																		// par�metro
																		// 5 do
																		// arquivo
				testManager = new TestManager(deviceManager, args[6]);

			} else {
				testManager = new TestManager(deviceManager);
			}
			deviceManager.addComponentManager(testManager);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Adicionar as interfaces de comunica��o suportadas --- Inicialmente
		// manual. Ap�s adicionar um processo autom�tico
		deviceManager.addSupportedCommunicationInterface(new AndroidGeneralCommunicationInterface(getBaseContext()));
		ConcreteApplicationHandler handler = new ConcreteApplicationHandler(deviceManager);
		deviceManager.getEventManager().subscribe(handler);
		PushServiceReceiver teste = new SocketPushServiceReceiver(deviceManager.getCommunicationManager());
		deviceManager.addSupportedInputCommunicationInterface(teste);
		deviceManager.setOSDiscovery(new AndroidOperationalSystemDiscovery(getBaseContext()));
		Log.d("LOG", "adicionou comunica��o e o Application Handle");
		try {
			deviceManager.setDeviceKnowledgeRepresentationModel(getAssets().open("deviceKnowledgeModel.xml"),
					"xmlInputStream");
			Log.d("LOG", "Adicionou o conhecimento ");
			SQLiteAndroidDatabaseHelper dbHelper = new SQLiteAndroidDatabaseHelper(deviceManager.getDataManager(),
					getBaseContext());
			Log.d("LOG", "Criou o DB Handler ");
			deviceManager.getDataManager().setUrboSentiDatabaseHelper(dbHelper);
			// SQLiteDatabase db = (SQLiteDatabase)
			// dbHelper.openDatabaseConnection();
			// Log.d("LOG", "Abriu a conex�o ");
			// dbHelper.createDatabase();
			// Log.d("LOG", "Tentou criar o DB ");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d("Error", "IO: " + e.getMessage());
		}
		deviceManager.onCreate();
		Log.d("DEBUG", "onCreateCompletado");
		return null;
	}

	/**
	 *
	 * @param args
	 *            // Experimentos // Args: (0) porta; (1) Experimento; (2 ...)
	 *            depepende dos experimentos
	 *            <ul>
	 *            <li>Args Gerais: (0) porta; (1) Experimento; (2 ...) depende
	 *            dos experimentos</li>
	 *            <li>Experimento 1 (Aplica��o): (2) tempo de parada; (3)
	 *            intervalo entre uploads; (4) com ou sem adapta��o (no)</li>
	 *            <li>Experimento 2 (Eventos internos): (2) quantityOfEvents,
	 *            (3) int quantityOfRules, (4) quantityOfConditions, (5)
	 *            nomeArquivoDeSa�da</li>
	 *            <li>Experimento 3 (Intera��es): (2) Modo de opera��o (1 ou 2);
	 *            </li>
	 *            <li>Experimento 3 (Intera��es) - modo de opera��o 1
	 *            (Escutador): nenhum par�metro;</li>
	 *            <li>Experimento 3 (Intera��es) - modo de opera��o 2 (Envia
	 *            mensagens): (3) quantityOfEvents, (4) int quantityOfRules, (5)
	 *            quantityOfConditions, (6) nomeArquivoDeSa�da; (7) arquivo de
	 *            lista de ips; (8) desligar escutadores?</li>
	 *            <li>Experimento 4 (Intera��es e eventos internos) - modo de
	 *            opera��o 2 (Envia mensagens): (3) quantityOfEvents, (4) int
	 *            quantityOfRules, (5) quantityOfConditions, (6)
	 *            nomeArquivoDeSa�da; (7) arquivo de lista de ips; (8) desligar
	 *            escutadores?</li>
	 *            </ul>
	 * @throws java.io.IOException
	 * @throws java.lang.InterruptedException
	 *
	 */
	@Override
	public void onCreate() {

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					/**
					 * *** Inicio das fun��es e aplica��o de sensoriamento ****
					 */
					// (1) Experimento
					switch (Integer.parseInt(args[1])) {
					case 1: // Experimento 1 (Aplica��o): nenhum
						// Aplica��o de sensoriamento blablabla
						// Testes
						TestCommunication tc = new TestCommunication(deviceManager);
						// experimento de aplica��o
						try {
							if (args.length < 4) {
								tc.test2(0, Long.parseLong(args[2]));
							} else {
								tc.test2(Long.parseLong(args[3]), Long.parseLong(args[2]));
							}
						} catch (java.lang.ArrayIndexOutOfBoundsException ex) {
							tc.test2(0, Long.parseLong(args[2]));
						}
						break;
					case 2: // Experimento 2 (Eventos internos):
						// (2) quantityOfEvents, (3) quantityOfRules, (4)
						// quantityOfConditions, (6) nomeArquivoDeSa�da
						testManager.startExperimentOfInternalEvents(Integer.parseInt(args[2]), // quantityOfInteractions,
								Integer.parseInt(args[3]), // quantityOfRules,
								Integer.parseInt(args[4]));
						// quantityOfConditions,
						testManager.waitEventsQueueBeFinished();
						break;
					case 3: // Experimento 3 (Intera��es): (2) Modo de opera��o
							// (1
							// ou 2);
						if (args[2].equals("1")) {
							// modo de opera��o 1 (Escutador): nenhum par�metro
							testManager.waitExperiment();
						} else if (args[2].equals("2")) { // modo de opera��o 2
															// (Envia
															// mensagens):
							// (3) quantityOfEvents, (4) int quantityOfRules,
							// (5)
							// quantityOfConditions,
							// (6) nomeArquivoDeSa�da; (7) arquivo de lista de
							// ips;
							// (8) desligar escutadores?
							// abrir arquivo e repetir eventos por agente
							// extra�do
							// por porta e ip
							ArrayList<String> ips = new ArrayList();
							ArrayList<Integer> ports = new ArrayList<Integer>();
							FileReader agentAddresses = new FileReader(args[7]); // (6)
																					// arquivo
																					// de
																					// lista
																					// de
																					// ips;
							BufferedReader br = new BufferedReader(agentAddresses);
							String s, ss[];
							while ((s = br.readLine()) != null) {
								ss = s.split(":");
								ips.add(ss[0]);
								ports.add(Integer.parseInt(ss[1]));
							}
							agentAddresses.close();
							for (int i = 0; i < ips.size(); i++) {
								testManager.startExperimentOfContinuosInteractionEvents(Integer.parseInt(args[3]), // quantityOfInteractions,
										Integer.parseInt(args[4]), // quantityOfRules,
										Integer.parseInt(args[5]), // quantityOfConditions,
										ips.get(i), ports.get(i));
							}
							testManager.waitInteractionsBeFinished();
							args[8] = args[8].trim();
							// testar se precisa desligar
							if (args[8].equals("sim") || args[8].equals("yes") || args[8].equals("s")
									|| args[8].equals("y")) {
								testManager.stopAgents(ips, ports);
								Thread.sleep(5000);
							}
						}
						break;
					case 4: // Experimento 4 (Intera��es e eventos internos)
						if (args[3].equals("1")) {
							// modo de opera��o 1 (Escutador): nenhum par�metro
							testManager.waitExperiment();
						} else if (args[3].equals("2")) { // modo de opera��o 2
															// (Envia
															// mensagens):
							// (3) quantityOfEvents, (4) int quantityOfRules,
							// (5)
							// quantityOfConditions,
							// (6) nomeArquivoDeSa�da; (7) arquivo de lista de
							// ips;
							// (8) desligar escutadores?; (9) quantidade de
							// agentes
							// abrir arquivo e repetir eventos por agente
							// extra�do
							// por porta e ip
							ArrayList<String> ips = new ArrayList();
							int quantityOfAgents = Integer.parseInt(args[8]);
							ArrayList<Integer> ports = new ArrayList<Integer>();
							FileReader agentAddresses = new FileReader(args[7]); // (6)
																					// arquivo
																					// de
																					// lista
																					// de
																					// ips;
							BufferedReader br = new BufferedReader(agentAddresses);
							String s, ss[];
							while ((s = br.readLine()) != null && ips.size() < quantityOfAgents) {
								ss = s.split(":");
								ips.add(ss[0]);
								ports.add(Integer.parseInt(ss[1]));
							}
							agentAddresses.close();
							for (int i = 0; i < ips.size(); i++) {
								testManager.startExperimentOfInteractionEvents(Integer.parseInt(args[3]), // quantityOfInteractions,
										Integer.parseInt(args[4]), // quantityOfRules,
										Integer.parseInt(args[5]), // quantityOfConditions,
										ips.get(i), ports.get(i));
							}
							args[8] = args[8].trim();
							// testar se precisa desligar
							if (args[8].equals("sim") || args[8].equals("yes") || args[8].equals("s")
									|| args[8].equals("y")) {
								testManager.stopAgents(ips, ports);
							}
							testManager.waitEventsQueueBeFinished();
						}
						break;
					}
				} catch (NumberFormatException e) {
					Log.d("ERROR", e.getLocalizedMessage());
				} catch (IOException e) {
					Log.d("ERROR", e.getLocalizedMessage());
				} catch (InterruptedException e) {
					Log.d("ERROR", e.getLocalizedMessage());
				}
			}
		});
		t.start();
		Log.d("DEBUG", "inicializa��o dos servi�os da urbosenti completado");

		try {
			Message m = new Message();
			m.setContent("oiiiiii");

			// Envia o relato
			deviceManager.getCommunicationManager().addReportToSend(m);
			m = new Message();
			m.setContent("oiiiiii");

			// Envia o relato
			deviceManager.getCommunicationManager().addReportToSend(m);
			Log.d("DEBUG", "Relato enviado");
			m = new Message();
			m.setContent("oiiiiii");

			// Envia o relato
			deviceManager.getCommunicationManager().addReportToSend(m);
			m = new Message();
			m.setContent("oiiiiii");

			// Envia o relato
			deviceManager.getCommunicationManager().addReportToSend(m);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		deviceManager.stopUrboSentiServices();
		Log.d("DEBUG", "Servi�os parados");
	}
}

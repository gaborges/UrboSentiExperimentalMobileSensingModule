/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.events.timer;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import urbosenti.core.events.EventManager;

/**
 *
 * @author Guilherme
 */
public class UrboSentiNativeEventTimer extends EventTimer{

    private Timer timer;
    private boolean finished;

    public UrboSentiNativeEventTimer(TriggerRequest request, EventManager eventManager) {
        super(request,eventManager);
        this.finished = false;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void start() {
        // instancia o temporizador deste worker
        timer = new Timer();
        // adiciona o tempo inicial de execução do gatilho
        request.setStartedTime(new Date());
        if (EventManager.METHOD_ONLY_INTERVAL == request.getMethod()) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // Adiciona o tempo final de execução do gatilho
                    request.setFinishedTime(new Date());
                    // Atribui a variável finished o valor true com o objetivo de evitar buscar desnecessárias
                    finished = true;
                    // Notifica o componente de eventos sobre a conclusão do evento
                    notifyTriggeredEvent(request);
                }
            }, request.getInterval());
        } else if (EventManager.METHOD_ONLY_DATE == request.getMethod()) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // Adiciona o tempo final de execução do gatilho
                    request.setFinishedTime(new Date());
                    // Atribui a variável finished o valor true com o objetivo de evitar buscar desnecessárias
                    finished = true;
                    // Notifica o componente de eventos sobre a conclusão do evento
                    notifyTriggeredEvent(request);
                }
            }, request.getTime());
        } else if (EventManager.METHOD_DATE_PLUS_REPEATED_INTERVALS == request.getMethod()) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // Adiciona o tempo final de execução do gatilho
                    request.setFinishedTime(new Date());
                    // Atribui a variável finished o valor true com o objetivo de evitar buscar desnecessárias
                    finished = true;
                    // Notifica o componente de eventos sobre a conclusão do evento
                    notifyTriggeredEvent(request);
                }
            },
                    request.getTime(), request.getInterval()
            );
        }
    }

    @Override
    public void cancel() {
        timer.cancel();
        this.finished = true;
    }

}

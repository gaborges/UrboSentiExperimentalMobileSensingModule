/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package urbosenti.core.device;

import java.util.HashMap;

/**
 *
 * @author Guilherme
 */
public interface OperatingSystemDiscovery {

    /**
     * Espaço disponível de Armazenamento em Bytes
     */
    public static final String AVAILABLE_STORAGE_SPACE = "storage";

    /**
     * Quantidade de núcleos disponíveis
     */
    public static final String CPU_CORE_COUNT = "cores";

    /**
     * Frequência por núcleos do CPU
     */
    public static final String CPU_CORE_FREQUENCY = "clock";
    /**
     * Modelo do CPU
     */
    public static final String CPU_MODEL = "cpuModel";
    /**
     * S.O. Nativo
     */
    public static final String NATIVE_OPERATION_SYSTEM = "nativeOS";
    /**
     * Memória RAM em Bytes
     */
    public static final String RAM_AVAILABLE = "RAM";
    /**
     * Modelo de dispositivo
     */
    public static final String DEVICE_MODEL = "deviceModel";
    /**
     * Capacidade da Bateria
     */
    public static final String BATTERY_CAPACITY = "battery";
    /**
     * Realiza o processo de descoberta dos recursos do sistema operacional
     * @return HashMap<String,Object> contendo as chaves destacados pelas constantes dessa classe.
     */
    public HashMap<String,Object> discovery();
}

package br.ufrgs.urbosenti.android;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import java.io.File;
import java.util.HashMap;

import urbosenti.core.device.OperatingSystemDiscovery;

public class AndroidOperationalSystemDiscovery implements OperatingSystemDiscovery{

	private Context context;

	public AndroidOperationalSystemDiscovery(Context context) {
		this.context = context;
	}

	@Override
    public HashMap<String, Object> discovery() {
        Runtime runtime = Runtime.getRuntime();        
        HashMap<String,Object> m = new HashMap<String, Object>();
        m.put(OperatingSystemDiscovery.AVAILABLE_STORAGE_SPACE, diskTotalSpace());
        m.put(OperatingSystemDiscovery.BATTERY_CAPACITY, getBatteryCapacity()); // somente em android
        m.put(OperatingSystemDiscovery.CPU_CORE_COUNT, runtime.availableProcessors());
        //m.put(OperatingSystemDiscovery.CPU_CORE_FREQUENCY, args); não existe como
        m.put(OperatingSystemDiscovery.CPU_MODEL, System.getProperty("os.arch"));
        m.put(OperatingSystemDiscovery.DEVICE_MODEL, (
        		Build.MODEL.startsWith(Build.MANUFACTURER) ? Build.MODEL : Build.MANUFACTURER+" "+Build.MODEL)); // Somente em android
        //m.put(OperatingSystemDiscovery.VIRTUAL_MACHINE_NAME, System.getProperty("java.vm.name"));
        m.put(OperatingSystemDiscovery.NATIVE_OPERATION_SYSTEM, System.getProperty("os.name"));
        m.put(OperatingSystemDiscovery.RAM_AVAILABLE, runtime.maxMemory());
        return m;
    }
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public long diskTotalSpace() {
        /* Get a list of all filesystem roots on this system */
        File[] roots = File.listRoots();
        long total = 0;
        /* For each filesystem root, print some info */
        for (File root : roots) {
            total += root.getTotalSpace();
        }
        return total;
    }
	
	public double getBatteryCapacity() {
	    Object mPowerProfile_ = null;

	    final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";

	    try {
	        mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
	                .getConstructor(Context.class).newInstance(context);
	    } catch (Exception e) {
	        e.printStackTrace();
	    } 

	    try {
	        double batteryCapacity = (Double) Class
	                .forName(POWER_PROFILE_CLASS)
	                .getMethod("getAveragePower", java.lang.String.class)
	                .invoke(mPowerProfile_, "battery.capacity");
	        return batteryCapacity;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		return 0; 
	}

}

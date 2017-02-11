package apmon.lisa_host;

import java.util.HashMap;
import java.util.Hashtable;

/**
 * @author ML team
 */
public class HostPropertiesMonitor {

	private final static String osName = System.getProperty("os.name");
	private static ProcReader reader = null;
	private static MacHostPropertiesMonitor macHostMonitor = null;
	
	static {
		if (System.getProperty("os.name").indexOf("Linux") == -1 && System.getProperty("os.name").indexOf("Mac") == -1) {
			System.loadLibrary("system");
		} else  if (System.getProperty("os.name").indexOf("Linux") != -1){
			reader = new ProcReader();
		} else {
			macHostMonitor = new MacHostPropertiesMonitor();
		}
	}
	
    /**
     * @return values
     */
    public HashMap<Long, String> getHashParams() {
        if (osName.indexOf("Linux") != -1) {
            return reader.getHashedValues();
        }
        
        return null;
    }

    /**
     * @return mac addresses
     */
    public native String getMacAddresses();
	
	/**
	 * @return mac addresses
	 */
	public String getMacAddressesCall() {
		
		if (osName.indexOf("Linux") != -1) {
			return reader.getMacAddress();
		}
		if (osName.indexOf("Mac") != -1)
			return macHostMonitor.getMacAddresses();
		return getMacAddresses();
	}
	
	/**
	 * update
	 */
	public native void update();
	
	/**
	 * update 
	 */
	public void updateCall() {

		if (osName.indexOf("Linux") != -1) {
			reader.update();
			return;
		} 
		if (osName.indexOf("Mac") != -1) {
			macHostMonitor.update();
			return;
		}
		update();
	}
	
	/**
	 * @return cpu usage
	 */
	public native String getCpuUsage();
	
	/**
	 * @return cpu usage
	 */
	public String getCpuUsageCall() {
		
		if (osName.indexOf("Linux") != -1) {
			return reader.getCPUUsage();
		}
		if (osName.indexOf("Mac") != -1) {
			return macHostMonitor.getCpuUsage();
		}
		return getCpuUsage();
	}

	/**
	 * @return cpu used
	 */
	public native String getCpuUSR();
	
	/**
	 * @return cpu user
	 */
	public String getCpuUSRCall() {
		
		if (osName.indexOf("Linux") != -1) {
			return reader.getCPUUsr();
		}
		if (osName.indexOf("Mac") != -1)
			return macHostMonitor.getCpuUSR();
		return getCpuUSR();
	}
	
	/**
	 * @return cpu sys
	 */
	public native String getCpuSYS();
	
	/**
	 * @return cpu sys
	 */
	public String getCpuSYSCall() {
		
		if (osName.indexOf("Linux") != -1) {
			return reader.getCPUSys();
		}
		if (osName.indexOf("Mac") != -1)
			return macHostMonitor.getCpuSYS();
		return getCpuSYS();
	}
	
	/**
	 * @return cpu nice
	 */
	public native String getCpuNICE();
	
	/**
	 * @return cpu nice
	 */
	public String getCpuNICECall() {
		
		if (osName.indexOf("Linux") != -1) {
			return reader.getCPUNice();
		}
		if (osName.indexOf("Mac") != -1)
			return macHostMonitor.getCpuNICE();
		return getCpuNICE();
	}
	
	/**
	 * @return cpu idle
	 */
	public native String getCpuIDLE();
	
	/**
	 * @return cpu idle
	 */
	public String getCpuIDLECall() {
		
		if (osName.indexOf("Linux") != -1) {
			return reader.getCPUIdle();
		}
		if (osName.indexOf("Mac") != -1)
			return macHostMonitor.getCpuIDLE();
		return getCpuIDLE();
	}
	
	/**
	 * @return pages in
	 */
	public native String getPagesIn();
	
	/**
	 * @return pages in
	 */
	public String getPagesInCall() {
		
		if (osName.indexOf("Linux") != -1) {
			return reader.getPagesIn();
		}
		if (osName.indexOf("Mac") != -1)
			return macHostMonitor.getPagesIn();
		return getPagesIn();
	}
	
	/**
	 * @return pages out
	 */
	public native String getPagesOut();
	
	/**
	 * @return pages out
	 */
	public String getPagesOutCall() {
		
		if (osName.indexOf("Linux") != -1) {
			return reader.getPagesOut();
		}
		if (osName.indexOf("Mac") != -1)
			return macHostMonitor.getPagesOut();
		return getPagesOut();
	}
	
	/**
	 * @return memory usage
	 */
	public native String getMemUsage();
	
	/**
	 * @return memory usage
	 */
	public String getMemUsageCall() {
		
		if (osName.indexOf("Linux") != -1) {
			return reader.getMemUsage();
		}
		if (osName.indexOf("Mac") != -1)
			return macHostMonitor.getMemUsage();
		return getMemUsage();
	}
	
	/**
	 * @return used memory
	 */
	public native String getMemUsed();
	
	/**
	 * @return used memory
	 */
	public String getMemUsedCall() {
		
		if (osName.indexOf("Linux") != -1) {
			return reader.getMemUsed();
		}
		if (osName.indexOf("Mac") != -1)
			return macHostMonitor.getMemUsed();
		return getMemUsed();
	}
	
	
    /**
     * @return total memory 
     */
    public String getMemTotalCall() {
        
        if (osName.indexOf("Linux") != -1) {
            return reader.getMemTotal();
        }
        return null;
    }


    /**
     * @return free swap
     */
    public String getSwapFreeCall() {
        if (osName.indexOf("Linux") != -1) {
            return reader.getSwapFree();
        }
        
        return null;
    }
    
    /**
     * @return total swap
     */
    public String getSwapTotalCall() {
        if (osName.indexOf("Linux") != -1) {
            return reader.getSwapTotal();
        }
        
        return null;
    }
    
    /**
     * @return mem free
     */
    public native String getMemFree();

    /**
     * @return mem free
     */
    public String getMemFreeCall() {
		
		if (osName.indexOf("Linux") != -1) {
			return reader.getMemFree();
		}
		if (osName.indexOf("Mac") != -1)
			return macHostMonitor.getMemFree();
		return getMemFree();
	}
	
	/**
	 * @return disk io
	 */
	public native String getDiskIO();
	
	/**
	 * @return disk io
	 */
	public String getDiskIOCall() {
		
		if (osName.indexOf("Linux") != -1) {
			return reader.getDiskIO();
		}
		if (osName.indexOf("Mac") != -1)
			return macHostMonitor.getDiskIO();
		return getDiskIO();
	}
	
	/**
	 * @return disk total
	 */
	public native String getDiskTotal();
	
	/**
	 * @return disk total
	 */
	public String getDiskTotalCall() {
		
		if (osName.indexOf("Linux") != -1) {
			return reader.getDiskTotal();
		}
		if (osName.indexOf("Mac") != -1)
			return macHostMonitor.getDiskTotal();
		return getDiskTotal();
	}
	
	/**
	 * @return disk used
	 */
	public native String getDiskUsed();
	
	/**
	 * @return disk used
	 */
	public String getDiskUsedCall() {
		
		if (osName.indexOf("Linux") != -1) {
			return reader.getDiskUsed();
		}
		if (osName.indexOf("Mac") != -1)
			return macHostMonitor.getDiskUsed();
		return getDiskUsed();
	}
	
	/**
	 * @return disk free
	 */
	public native String getDiskFree();
	
	/**
	 * @return disk free
	 */
	public String getDiskFreeCall() {
		
		if (osName.indexOf("Linux") != -1) {
			return reader.getDiskFree();
		}
		if (osName.indexOf("Mac") != -1)
			return macHostMonitor.getDiskFree();
		return getDiskFree();
	}
	
	/**
	 * @return processes
	 */
	public native String getNoProcesses();
	
	/**
	 * @return processes
	 */
	public String getNoProcessesCall() {
		
		if (osName.indexOf("Linux") != -1) {
			return reader.getNoProcesses();
		}
		if (osName.indexOf("Mac") != -1)
			return macHostMonitor.getNoProcesses();
		return getNoProcesses();
	}
	
	/**
	 * @return load1
	 */
	public native String getLoad1();
	
	/**
	 * @return load1
	 */
	public String getLoad1Call() {
		
		if (osName.indexOf("Linux") != -1) {
			return reader.getLoad1();
		}
		if (osName.indexOf("Mac") != -1)
			return macHostMonitor.getLoad1();
		return getLoad1();
	}
	
	/**
	 * @return load5
	 */
	public native String getLoad5();
	
	/**
	 * @return load5
	 */
	public String getLoad5Call() {
		
		if (osName.indexOf("Linux") != -1) {
			return reader.getLoad5();
		}
		if (osName.indexOf("Mac") != -1)
			return macHostMonitor.getLoad5();
		return getLoad5();
	}
	
	/**
	 * @return load15
	 */
	public native String getLoad15();
	
	/**
	 * @return load15
	 */
	public String getLoad15Call() {
		
		if (osName.indexOf("Linux") != -1) {
			return reader.getLoad15();
		}
		if (osName.indexOf("Mac") != -1)
			return macHostMonitor.getLoad15();
		return getLoad15();
	}
	
	/**
	 * @return if
	 */
	public native String[] getNetInterfaces();
	
	/**
	 * @return if
	 */
	public String[] getNetInterfacesCall() {
		
		if (osName.indexOf("Linux") != -1) {
			return reader.getNetInterfaces();
		}
		if (osName.indexOf("Mac") != -1)
			return macHostMonitor.getNetInterfaces();
		return getNetInterfaces();
	}

	/**
	 * @param ifName
	 * @return net in
	 */
	public native String getNetIn(String ifName);
	
	/**
	 * @param ifName
	 * @return net in
	 */
	public String getNetInCall(String ifName) {
		
		if (osName.indexOf("Linux") != -1) {
			return reader.getNetIn(ifName);
		}
		if (osName.indexOf("Mac") != -1)
			return macHostMonitor.getNetIn(ifName);
		return getNetIn(ifName);
	}
	
	/**
	 * @param ifName
	 * @return net out
	 */
	public native String getNetOut(String ifName);
	
	/**
	 * @param ifName
	 * @return net out
	 */
	public String getNetOutCall(String ifName) {
		
		if (osName.indexOf("Linux") != -1) {
			return reader.getNetOut(ifName);
		}
		if (osName.indexOf("Mac") != -1)
			return macHostMonitor.getNetOut(ifName);
		
		return getNetOut(ifName);
	}
    
    /**
     * @return processes states
     */
    public native Hashtable<String, Integer> getProcessesState();
    
    /**
     * @return processes states
     */
    public Hashtable<String, Integer> getPState() {
        
        if (osName.indexOf("Linux") != -1) {
            return reader.getProcessesState();
        }
        if (osName.indexOf("Mac") != -1)
            return macHostMonitor.getProcessesState();
        
        return getProcessesState();        
    }
    
    /**
     * @return socket states
     */
    public native Hashtable<String, Integer> getNetSockets();
    
    /**
     * @return socket states
     */
    public Hashtable<String, Integer> getSockets() {
        
        if (osName.indexOf("Linux") != -1) {
            return reader.getNetSockets();
        }
        if (osName.indexOf("Mac") != -1)
            return macHostMonitor.getNetSockets();
        
        return getNetSockets();        
    }
    
    /**
     * @return tcp details
     */
    public native Hashtable<String, Integer> getTcpDetails();
    
    /**
     * @return tcp details
     */
    public Hashtable<String, Integer> getTCPDetails() {
        
        if (osName.indexOf("Linux") != -1) {
            return reader.getTcpDetails();
        }
        if (osName.indexOf("Mac") != -1)
            return macHostMonitor.getTcpDetails();
        
        return getTcpDetails();        
    }
	
	/**
	 * 
	 */
	public void stopIt() {
		reader.stopIt();
	}
	
} // end of class HostPropertiesMonitor


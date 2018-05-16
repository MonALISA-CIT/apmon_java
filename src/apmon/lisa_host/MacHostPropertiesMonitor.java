package apmon.lisa_host;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @author Gregory Denis
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
/**
 * @author costing
 * @since Nov 13, 2010
 */
public class MacHostPropertiesMonitor {

	private String[] networkInterfaces;
	private String activeInterface;
	private String cpuUsage = "0";
	private String cpuUSR = "0";
	private String cpuSYS = "0";
	private String cpuIDLE = "0";
	private String nbProcesses = "0";
	private String load1 = "0";
	private String load5 = "0";
	private String load15 = "0";
	private String memUsed = "0";
	private String memFree = "0";
	private String memUsage = "0";
	private String netIn = "0";
	private String netOut = "0";
	private String pagesIn = "0";
	private String pagesOut = "0";
	private String macAddress = "unknown";
	private String diskIO = "0";
	private String diskIn = "0";
	private String diskOut = "0";
	private String diskFree = "0";
	private String diskUsed = "0";
	private String diskTotal = "0";
	private String command = "";
	private cmdExec execute = null;
	private String sep = null;
    private Parser parser = null;
    private Hashtable<String, Integer> netSockets = null;
    private Hashtable<String, Integer> netTcpDetails = null;
	
	/**
	 * 
	 */
	public MacHostPropertiesMonitor() {

		parser = new Parser();
        execute = new cmdExec();
		execute.setTimeout(3 * 1000);
		sep = System.getProperty("file.separator");
		// get the network interfaces up
		command = sep + "sbin" + sep	+ "ifconfig -l -u";
		String result = execute.executeCommand(command, "lo0");
		//System.out.println(command + " = "+ result);
		if (result == null || result.equals("")) {
			System.out.println(command + ": No result???");
		} else {
			int where = result.indexOf("lo0");
			networkInterfaces =
				result
					.substring(where + 3, result.length())
					.replaceAll("  ", " ")
					.trim()
					.split(
					" ");

			//			 get the currently used Mac Address
			for (int i = 0; i < networkInterfaces.length; i++) {
				String current = networkInterfaces[i];
				command = sep + "sbin" + sep + "ifconfig " + current;
				result = execute.executeCommand(command, current);
				//System.out.println(command + " = " + result);
				if (result == null || result.equals("")) {
					System.out.println(command + ": No result???");
				} else {
					if (result.indexOf("inet ") != -1) {
						int pointI = result.indexOf("ether");
						int pointJ = result.indexOf("media", pointI);
						macAddress =
							result.substring(pointI + 5, pointJ).trim();
						//System.out.println("Mac Address:" + macAddress);
						activeInterface = current;
					}
				}
			}
		}

		//		 get the disk information
		command = sep + "bin" + sep + "df -k -h "	+ sep;
		result = execute.executeCommand(command, "/dev");
		//System.out.println(command + " = "+ result);
		if (result == null || result.equals("")) {
			System.out.println(command + ": No result???");
		} else {
			parseDf(result);
		}

		update();
	}

	/**
	 * @return mac addresses
	 */
	public String getMacAddresses() {
		return macAddress;
	}

	/**
	 * 
	 */
	public void update() {

		if (execute == null) {
			execute = new cmdExec();
			execute.setTimeout(100 * 1000);
		}

		// get CPU, load, Mem, Pages, Processes from '/usr/bin/top'
		command = sep + "usr" + sep + "bin" + sep
				+ "top -d -l2 -n1 -F -R";
		String result = execute.executeCommand(command, "PID", 2);
		//System.out.println(command + " = "+ result);
		if (result == null || result.equals("")) {
			System.out.println("No result???");
		} else {
			parseTop(result);
		}
	}

	private void parseDf(String toParse) {

		//System.out.println("result of df -k -h /:" + toParse);

		int pointI = toParse.indexOf("/dev/");
		int pointJ = 0;
		int pointK = 0;

		// Get the size of the root disk
		try {
			pointJ = toParse.indexOf(" ", pointI);
			pointK = indexOfUnitLetter(toParse, pointJ);
			diskTotal = toParse.substring(pointJ, pointK).trim();
		} catch (java.lang.StringIndexOutOfBoundsException e) {
			// ignore
		}

		// Get the capacity used
		try {
			pointI = toParse.indexOf(" ", pointK);
			pointJ = indexOfUnitLetter(toParse, pointI);
			diskUsed = toParse.substring(pointI, pointJ).trim();
		} catch (java.lang.StringIndexOutOfBoundsException e) {
			// ignore
		}

		// Get the free space
		try {
			pointK = toParse.indexOf(" ", pointJ);
			pointI = indexOfUnitLetter(toParse, pointK);
			diskFree = toParse.substring(pointK, pointI).trim();
		} catch (java.lang.StringIndexOutOfBoundsException e) {
			// ignore
		}

		/*System.out.println(
			"Disk: Total:"
				+ diskTotal
				+ " Used:"
				+ diskUsed
				+ " Free:"
				+ diskFree);*/
	}

	private int indexOfUnitLetter(String inside, int from) {

		int temp = inside.indexOf('K', from);
		if (temp == -1 || (temp - from > 10)) {
			temp = inside.indexOf('M', from);
			if (temp == -1 || (temp - from > 10)) {
				temp = inside.indexOf('G', from);
				if (temp == -1 || (temp - from > 10)) {
					temp = inside.indexOf('B', from);
					if (temp == -1 || (temp - from > 10)) {
						temp = inside.indexOf('T', from);
						if (temp == -1 || (temp - from > 10)) {
							temp = inside.indexOf('b', from);
							if (temp - from > 10)
								temp = -1;
						}
					}
				}
			}
		}
		return temp;
	}

	private int lastIndexOfUnitLetter(String inside, int from) {

		int temp = inside.lastIndexOf('K', from);
		if (temp == -1 || (from - temp > 10)) {
			temp = inside.lastIndexOf('M', from);
			if (temp == -1 || (from - temp > 10)) {
				temp = inside.lastIndexOf('G', from);
				if (temp == -1 || (from - temp > 10)) {
					temp = inside.lastIndexOf('B', from);
					if (temp == -1 || (from - temp > 10)) {
						temp = inside.lastIndexOf('T', from);
						if (temp == -1 || (from - temp > 10)) {
							temp = inside.lastIndexOf('b', from);
							if (from - temp > 10)
								temp = -1;
						}
					}
				}
			}
		}
		return temp;
	}

	private double howMuchMegaBytes(char a) {

		switch (a) {
			case 'T' :
				return 1048576.0;
			case 'G' :
				return 1024.0;
			case 'M' :
				return 1.0;
			case 'K' :
				return 0.0009765625;
			case 'B' :
				return 0.0000009537;
			default :
				return 1.0;
		}
	}
	
	private void parseTop(String toParse) {

		//System.out.println("\n******\n"+toParse+"\n********\n");

		int pointA = 0;
		int pointB = 0;
		int unitPos = 0;
		double sum = 0.0;

		//		 Get number of total Processes
		try {
			pointA = toParse.indexOf("Processes:");
			//System.out.println("First Processes at " + pointA);
			pointA = toParse.indexOf("Processes:", pointA + 12) + 12;
			//System.out.println("Second Processes at " + pointA);
			pointB = toParse.indexOf(",", pointA + 1);
			nbProcesses = toParse.substring(pointA, pointB).trim();
			//System.out.println("Processes: " + nbProcesses);
		} catch (java.lang.StringIndexOutOfBoundsException e) {
			// ignore
		}

		// Get the loads...
		try {
			pointA = toParse.indexOf("Load Avg:", pointA);
			pointA += 9;
			pointB = toParse.indexOf(",", pointA);
			load1 = toParse.substring(pointA, pointB).trim();
			pointA = toParse.indexOf(",", pointB + 1);
			load5 = toParse.substring(pointB + 1, pointA).trim();
			pointB = toParse.indexOf("CPU usage:", pointA + 1);
			pointB = toParse.lastIndexOf(".", pointB);
			load15 = toParse.substring(pointA + 1, pointB).trim();
			//System.out.println("Load: [" + load1 + "][" + load5 + "][" + load15 + "]");
		} catch (java.lang.StringIndexOutOfBoundsException e) {
			// ignore
		}

		// Get CPUs...
		try {
			pointB = toParse.indexOf("CPU usage:", pointB + 1) + 4;
			pointA = toParse.indexOf("% user", pointB);
			cpuUSR = toParse.substring(pointB, pointA).trim();
			pointA = toParse.indexOf(",", pointA);
			pointB = toParse.indexOf("% sys", pointA + 1);
			cpuSYS = toParse.substring(pointA + 1, pointB).trim();
			pointA = toParse.indexOf(",", pointB);
			pointB = toParse.indexOf("% idle", pointA + 1);
			cpuIDLE = toParse.substring(pointA + 1, pointB).trim();
			sum = 100.0 - Double.parseDouble(cpuIDLE);
			cpuUsage = String.valueOf(sum);
			/*System.out.println("Cpu Usage:" + cpuUsage + " user:"
					+ cpuUSR + " sys:" + cpuSYS
					+ " idle:" + cpuIDLE);*/
		} catch (java.lang.StringIndexOutOfBoundsException e) {
			//ignore
		}

		//		 Get Mem...
		try {
			pointA = toParse.indexOf("PhysMem:", pointB);
			pointA += 8;
			pointB = toParse.indexOf("M used", pointA);
			double factor = 1;
			if (pointB == -1){
				factor = 1024;
				pointB = toParse.indexOf("G used", pointA);
			}
			//pointA = toParse.lastIndexOf(",", pointB);
			memUsed = toParse.substring(pointA + 1, pointB).trim();
			pointB = toParse.indexOf("M unused", pointB);
			pointA = toParse.lastIndexOf(",", pointB);
			memFree = toParse.substring(pointA + 1, pointB).trim();
			double memUsedD = Double.parseDouble(memUsed)*factor;
			//System.out.println("Mem Used: "+memUsedD+"M Free: "+memFree+"M");
			sum = memUsedD + Double.parseDouble(memFree);
			double percentage = Integer.parseInt(memUsed) / sum * 100;
			memUsage = String.valueOf(percentage);
		} catch (java.lang.StringIndexOutOfBoundsException e) {
			// ignore
		}


		// Pages In/Out...
		try {
			pointA = toParse.indexOf("VM:", pointB + 6);
			pointB = toParse.indexOf("swapins", pointA);
			pointA = toParse.lastIndexOf(",", pointB);
			pagesIn = toParse.substring(pointA + 1, pointB).trim();
			pointA = toParse.indexOf("swapouts", pointB);
			pointB = toParse.lastIndexOf(",", pointA);
			pagesOut = toParse.substring(pointB + 1, pointA).trim();
			//System.out.println("Swaps In: " + pagesIn + " Out: " + pagesOut);
		} catch (java.lang.StringIndexOutOfBoundsException e) {
			System.out.println("Can't find swaps in :" + toParse);
		}

//		// Get Network IO...
//		try {
//			pointA = toParse.indexOf("Networks:", pointB) + 9;
//			pointB = toParse.indexOf("packets:", pointA) + 6;
//			pointA = toParse.indexOf("in", pointB);
//			unitPos = lastIndexOfUnitLetter(toParse, pointA);
//			netIn = toParse.substring(pointB, unitPos).trim();
//			System.out.print("Net In:" + netIn);
//			double factor =
//				howMuchMegaBytes(
//					(toParse.substring(unitPos, unitPos + 1).toCharArray())[0]);
//			netIn = String.valueOf(Double.parseDouble(netIn) * factor * 4);
//			pointB = toParse.indexOf("out", pointA);
//			unitPos = lastIndexOfUnitLetter(toParse, pointB);
//			factor =
//				howMuchMegaBytes(
//					(toParse.substring(unitPos, unitPos + 1).toCharArray())[0]);
//			netOut = toParse.substring(pointA + 3, unitPos).trim();
//			System.out.println("Net Out:" + netOut);
//			netOut = String.valueOf(Double.parseDouble(netOut) * factor);
//			System.out.println("Network In:" + netIn + " OUT:" + netOut);
//		} catch (java.lang.StringIndexOutOfBoundsException e) {
//			System.out.println(e);
//		}
//
//		// Get Disks IO...
//		try {
//			pointB = toParse.indexOf("Disks:", pointA) + 6;
//			//pointA = toParse.indexOf("data =", pointB) + 6;
//			pointB = toParse.indexOf("read,", pointA);
//			unitPos = lastIndexOfUnitLetter(toParse, pointB);
//			diskIn = toParse.substring(pointA, unitPos).trim();
//			pointA = toParse.indexOf("written", pointB);
//			unitPos = lastIndexOfUnitLetter(toParse, pointA);
//			diskOut = toParse.substring(pointB + 3, unitPos).trim();
//
//			System.out.println("diskIO In:" + diskIn + " Out:" + diskOut);
//			diskIO = diskIn + diskOut;
//		} catch (java.lang.StringIndexOutOfBoundsException e) {
//			// ignore
//		}
	}
    
    /**
     * @return processes states
     */
    public Hashtable<String, Integer> getProcessesState() {
        Hashtable<String, Integer> states = null;
        String output = execute.executeCommandReality("ps -e -A -o state", "");
        if (output != null && !output.equals("")) {
            
            parser.parse(output);
            String line = parser.nextLine();
            
            states = new Hashtable<String, Integer>();
            states.put("D", Integer.valueOf(0));
            states.put("R", Integer.valueOf(0));
            states.put("S", Integer.valueOf(0));
            states.put("T", Integer.valueOf(0));
            states.put("Z", Integer.valueOf(0));
    
            while (line != null) {
                if ((line.startsWith(" ") || line.startsWith("\t"))) {
                    line = parser.nextLine();
                    continue;
                }
                Enumeration<String> e = states.keys();
                while(e.hasMoreElements()){
                    String key = e.nextElement();
                    if(line.startsWith(key)){
                        int x = (states.get(key)).intValue();
                        x++;
                        states.put(key, Integer.valueOf(x));
                    }
                }
                line = parser.nextLine();
            }
        }
        return states;
    }

	/**
	 * @return cpu usage
	 */
	public String getCpuUsage() {
		return cpuUsage;
	}

	/**
	 * @return cpu user
	 */
	public String getCpuUSR() {
		return cpuUSR;
	}

	/**
	 * @return cpu system
	 */
	public String getCpuSYS() {
		return cpuSYS;
	}

	/**
	 * @return cpu nice
	 */
	public String getCpuNICE() {
		return "0";
	}

	/**
	 * @return cpu idle
	 */
	public String getCpuIDLE() {
		return cpuIDLE;
	}

	/**
	 * @return pages in
	 */
	public String getPagesIn() {
		return pagesIn;
	}

	/**
	 * @return pages out
	 */
	public String getPagesOut() {
		return pagesOut;
	}

	/**
	 * @return memory usage
	 */
	public String getMemUsage() {
		return memUsage;
	}

	/**
	 * @return memory used
	 */
	public String getMemUsed() {
		return memUsed;
	}

	/**
	 * @return memory free
	 */
	public String getMemFree() {
		return memFree;
	}

	/**
	 * @return disk io
	 */
	public String getDiskIO() {
		return diskIO;
	}

	/**
	 * @return disk total
	 */
	public String getDiskTotal() {
		return diskTotal;
	}

	/**
	 * @return disk used
	 */
	public String getDiskUsed() {
		return diskUsed;
	}

	/**
	 * @return disk free
	 */
	public String getDiskFree() {
		return diskFree;
	}

	/**
	 * @return number of processes
	 */
	public String getNoProcesses() {
		return nbProcesses;
	}
    
    /**
     * @return sockets
     */
    public Hashtable<String, Integer> getNetSockets() {
        return netSockets;
    }
    
    /**
     * @return tcp states
     */
    public Hashtable<String, Integer> getTcpDetails() {
        return netTcpDetails;
    }

	/**
	 * @return load 1
	 */
	public String getLoad1() {
		return load1;
	}

	/**
	 * @return load 5
	 */
	public String getLoad5() {
		return load5;
	}

	/**
	 * @return load 15
	 */
	public String getLoad15() {
		return load15;
	}

	/**
	 * @return net interfaces
	 */
	public String[] getNetInterfaces() {
		return networkInterfaces;
	}

	/**
	 * @param ifName
	 * @return net in
	 */
	public String getNetIn(String ifName) {
		if (ifName.equalsIgnoreCase(activeInterface))
			return netIn;
		return "0";
	}

	/**
	 * @param ifName
	 * @return net out
	 */
	public String getNetOut(String ifName) {
		if (ifName.equalsIgnoreCase(activeInterface))
			return netOut;
		return "0";
	}
}

package apmon.lisa_host;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.StringTokenizer;

import apmon.ApMonMonitoringConstants;

// import lisa.core.util.cmdExec;

/**
 * Designated class that reads from the proc different parameter values.
 */
public class ProcReader {
	private HashMap<Long, String> hm = null;

	private cmdExec exec = null;

	private Parser parser = null;

	private String[] netInterfaces = null;

	private String hwAddress = null;

	private String cpuUsr = null;

	private long dcpuUsr = 0;

	private String cpuNice = null;

	private long dcpuNice = 0;

	private String cpuSys = null;

	private long dcpuSys = 0;

	private String cpuIdle = null;

	private long dcpuIdle = 0;

	private String cpuUsage = null;

	private String pagesIn = null;

	private long dpagesIn = 0;

	private String pagesOut = null;

	private long dpagesOut = 0;

	private String memUsage = null;

	private String memTotal = null;

	private String memUsed = null;

	private String memFree = null;

	private String swapTotal = null;

	private String swapFree = null;

	private String swapUsed = null;

	private String swapUsage = null;

	private String diskIO = null;

	private long dblkRead = 0;

	private long dblkWrite = 0;

	private String diskTotal = null;

	private String diskUsed = null;

	private String diskFree = null;

	private String diskUsage = null;

	private String processesNo = null;

	private String load1 = null;

	private String load5 = null;

	private String load15 = null;

	private Hashtable<String, String> netIn = null;

	private Hashtable<String, Long> dnetIn = null;

	private Hashtable<String, String> netOut = null;

	private Hashtable<String, Long> dnetOut = null;

	private Hashtable<String, Integer> states = null;

	private Hashtable<String, Integer> netSockets = null;

	private Hashtable<String, Integer> netTcpDetails = null;

	private long lastCall = 0;

	/**
	 * 
	 */
	public ProcReader() {
		netIn = new Hashtable<>();
		dnetIn = new Hashtable<>();
		netOut = new Hashtable<>();
		dnetOut = new Hashtable<>();
		exec = new cmdExec();
		parser = new Parser();
		states = new Hashtable<>();
		netSockets = new Hashtable<>();
		netTcpDetails = new Hashtable<>();

		update();
	}

	private void addNetInterface(String netInterfaceParam) {

		String netInterface = netInterfaceParam;

		if (netInterface == null || netInterface.equals(""))
			return;
		netInterface = netInterface.trim();
		if (netInterfaces == null) {
			netInterfaces = new String[1];
			netInterfaces[0] = netInterface;
			return;
		}
		for (int i = 0; i < netInterfaces.length; i++)
			if (netInterface.equals(netInterfaces[i]))
				return;
		String[] tmpNetInterfaces = new String[netInterfaces.length + 1];
		System.arraycopy(netInterfaces, 0, tmpNetInterfaces, 0, netInterfaces.length);
		tmpNetInterfaces[netInterfaces.length] = netInterface;
		netInterfaces = tmpNetInterfaces;
	}

	/**
	 * 
	 */
	public synchronized void update() {
		long newCall = System.currentTimeMillis();
		double diffCall = (newCall - lastCall) / 1000.0; // in seconds

		String str = null;
		String output = "";
		String line = "";

		if (hwAddress == null) {
			output = exec.executeCommandReality("ifconfig -a", "b");
			if (exec.isError())
				output = null;

			if (output != null && !output.equals("")) {
				netInterfaces = null;
				parser.parse(output);
				line = parser.nextLine();
				hwAddress = null;
				while (line != null) {
					if ((line.startsWith(" ") || line.startsWith("\t"))) {
						line = parser.nextLine();
						continue;
					}

					parser.parseAux(line);
					// get the name
					String netName = parser.nextAuxToken(" \t\n");
					if (netName != null && !netName.equals("")) {
						addNetInterface(netName);
					}
					// get the hw address
					// str = parser.nextAuxToken("HWaddr");
					str = Parser.getTextAfterToken(line, "HWaddr ");
					if (str != null) {
						hwAddress = str;
					}
					line = parser.nextLine();
				}
			}
		}

		pagesIn = pagesOut = null;
		cpuUsage = cpuUsr = cpuIdle = cpuNice = cpuSys = diskIO = null;
		parser.parseFromFile("/proc/stat");
		line = parser.nextLine();
		while (line != null) {
			if (line.startsWith("page")) {
				line = Parser.getTextAfterToken(line, "page ");
				parser.parseAux(line);
				pagesIn = parser.nextAuxToken();
				pagesOut = parser.nextAuxToken();
				long dpIn = 0, dpOut = 0;
				try {
					dpIn = Long.parseLong(pagesIn);
				}
				catch (@SuppressWarnings("unused") Exception e) {
					dpIn = -1;
				}
				try {
					dpOut = Long.parseLong(pagesOut);
				}
				catch (@SuppressWarnings("unused") Exception e) {
					dpOut = -1;
				}
				if (dpIn >= 0) {
					pagesIn = "" + ((diffWithOverflowCheck(dpIn, dpagesIn)) / diffCall);
					dpagesIn = dpIn;
				}
				if (dpOut >= 0) {
					pagesOut = "" + ((diffWithOverflowCheck(dpOut, dpagesOut)) / diffCall);
					dpagesOut = dpOut;
				}
			}
			if (line.startsWith("cpu") && cpuUsr == null) {
				line = Parser.getTextAfterToken(line, "cpu ");
				parser.parseAux(line);
				long dcUsr = 0, dcSys = 0, dcNice = 0, dcIdle = 0;
				line = parser.nextAuxToken(); // cpu usr
				try {
					dcUsr = Long.parseLong(line);

				}
				catch (@SuppressWarnings("unused") Exception e) {
					dcUsr = -1;
				}
				line = parser.nextAuxToken(); // cpu nice
				try {
					dcNice = Long.parseLong(line);
				}
				catch (@SuppressWarnings("unused") Exception e) {
					dcNice = -1;
				}
				line = parser.nextAuxToken(); // cpu sys
				try {
					dcSys = Long.parseLong(line);
				}
				catch (@SuppressWarnings("unused") Exception e) {
					dcSys = -1;
				}
				line = parser.nextAuxToken(); // cpu idle
				try {
					dcIdle = Long.parseLong(line);
				}
				catch (@SuppressWarnings("unused") Exception e) {
					dcIdle = -1;
				}

				double tmpUsr = diffWithOverflowCheck(dcUsr, dcpuUsr) / diffCall;
				double tmpSys = (diffWithOverflowCheck(dcSys, dcpuSys)) / diffCall;
				double tmpIdle = (diffWithOverflowCheck(dcIdle, dcpuIdle)) / diffCall;
				double tmpNice = (diffWithOverflowCheck(dcNice, dcpuNice)) / diffCall;
				if (tmpUsr >= 0.0 && tmpSys >= 0.0 && tmpIdle >= 0.0 && tmpNice >= 0.0) {
					dcpuUsr = dcUsr;
					dcpuSys = dcSys;
					dcpuNice = dcNice;
					dcpuIdle = dcIdle;
					double dcTotalP = tmpUsr + tmpSys + tmpNice;
					double dcTotal = dcTotalP + tmpIdle;
					cpuUsr = "" + (100.0 * tmpUsr / dcTotal);
					cpuSys = "" + (100.0 * tmpSys / dcTotal);
					cpuNice = "" + (100.0 * tmpNice / dcTotal);
					cpuIdle = "" + (100.0 * tmpIdle / dcTotal);
					cpuUsage = "" + (100.0 * dcTotalP / dcTotal);
				}
			}
			line = parser.nextLine();
		}

		output = exec.executeCommandReality(System.getProperty("user.home") + "/iostat -k", "L");
		if (exec.isError())
			output = null;
		if (output != null && !output.equals("")) {
			parser.parse(output);
			line = parser.nextLine();
			while (line != null && line.indexOf("avg-cpu") == -1)
				line = parser.nextLine();
			if (line != null && cpuUsr == null) {
				str = parser.nextToken(" \t\n");
				if (str != null)
					cpuUsr = str;
				str = parser.nextToken(" \t\n");
				if (str != null)
					cpuNice = str;
				str = parser.nextToken(" \t\n");
				if (str != null)
					cpuSys = str;
				str = parser.nextToken(" \t\n");
				str = parser.nextToken(" \t\n");
				if (str != null)
					cpuIdle = str;
				double dcUsr = 0.0, dcSys = 0.0, dcNice = 0.0, dcIdle = 0.0, d = -1.0;
				try {
					d = Double.parseDouble(cpuUsr);
				}
				catch (@SuppressWarnings("unused") Exception e) {
					d = -1.0;
				}
				if (d >= 0.0)
					dcUsr = d;
				try {
					d = Double.parseDouble(cpuSys);
				}
				catch (@SuppressWarnings("unused") Exception e) {
					d = -1.0;
				}
				if (d >= 0.0)
					dcSys = d;
				try {
					d = Double.parseDouble(cpuIdle);
				}
				catch (@SuppressWarnings("unused") Exception e) {
					d = -1.0;
				}
				if (d >= 0.0)
					dcIdle = d;
				try {
					d = Double.parseDouble(cpuNice);
				}
				catch (@SuppressWarnings("unused") Exception e) {
					d = -1.0;
				}
				if (d >= 0.0)
					dcNice = d;
				if ((dcUsr + dcSys + dcNice + dcIdle) != 0.0)
					cpuUsage = "" + (dcUsr + dcSys + dcNice);
				str = parser.nextToken(" \t\n");
				while (str != null && str.indexOf("Device:") == -1)
					str = parser.nextToken(" \t\n");
				if (str != null) {
					for (int i = 0; i < 5 && str != null; i++)
						str = parser.nextToken(" \t\n");
					long blkRead = 0, blkWrite = 0;
					while (true) {
						str = parser.nextToken(" \t\n");
						if (str == null)
							break;
						str = parser.nextToken(" \t\n"); // skip tps
						str = parser.nextToken(" \t\n"); // skip KB read /
															// sec
						str = parser.nextToken(" \t\n"); // skip KB write /
															// sec
						str = parser.nextToken(" \t\n"); // blk read / sec
						long l = 0;
						try {
							l = Long.parseLong(str);
						}
						catch (@SuppressWarnings("unused") Exception e) {
							l = -1;
						}
						if (l >= 0)
							blkRead += l;
						str = parser.nextToken(" \t\n"); // blk written / sec
						l = 0;
						try {
							l = Long.parseLong(str);
						}
						catch (@SuppressWarnings("unused") Exception e) {
							l = -1;
						}
						if (l >= 0.0)
							blkWrite += l;
					}

					double dRead = (diffWithOverflowCheck(blkRead, dblkRead)) / diffCall;
					double dWrite = (diffWithOverflowCheck(blkWrite, dblkWrite)) / diffCall;
					diskIO = "" + (dRead + dWrite);
					// ddiskIO = blkRead + blkWrite;
					dblkRead = blkRead;
					dblkWrite = blkWrite;
				}
			}
			else
				if (line != null) {
					str = parser.nextToken(" \t\n");
					while (str != null && str.indexOf("Device:") == -1)
						str = parser.nextToken(" \t\n");
					if (str != null) {
						for (int i = 0; i < 5 && str != null; i++)
							str = parser.nextToken(" \t\n");
						long blkRead = 0, blkWrite = 0;
						while (true) {
							str = parser.nextToken(" \t\n");
							if (str == null)
								break;
							str = parser.nextToken(" \t\n"); // skip tps
							str = parser.nextToken(" \t\n"); // skip KB read /
																// sec
							str = parser.nextToken(" \t\n"); // skip KB write /
																// sec
							str = parser.nextToken(" \t\n"); // blk read / sec
							long l = 0;
							try {
								l = Long.parseLong(str);
							}
							catch (@SuppressWarnings("unused") Exception e) {
								l = -1;
							}
							if (l >= 0)
								blkRead += l;
							str = parser.nextToken(" \t\n"); // blk written / sec
							l = 0;
							try {
								l = Long.parseLong(str);
							}
							catch (@SuppressWarnings("unused") Exception e) {
								l = -1;
							}
							if (l >= 0)
								blkWrite += l;
						}
						double dRead = (diffWithOverflowCheck(blkRead, dblkRead)) / diffCall;
						double dWrite = (diffWithOverflowCheck(blkWrite, dblkWrite)) / diffCall;
						diskIO = "" + (dRead + dWrite);
						// ddiskIO = blkRead + blkWrite;

						dblkRead = blkRead;
						dblkWrite = blkWrite;
					}
				}
		}

		swapFree = swapTotal = memUsage = memFree = memUsed = null;
		parser.parseFromFile("/proc/meminfo");
		line = parser.nextLine();
		double dmemTotal = 0.0, dmemFree = 0.0;
		while (line != null) {
			if (line.startsWith("MemTotal")) {
				line = Parser.getTextAfterToken(line, "MemTotal:");
				parser.parseAux(line);
				memTotal = parser.nextAuxToken();
				double d = 0.0;
				try {
					d = Double.parseDouble(memTotal);
				}
				catch (@SuppressWarnings("unused") Exception e) {
					d = -1.0;
				}
				if (d >= 0.0)
					dmemTotal = d;
			}
			else
				if (line.startsWith("MemFree")) {
					line = Parser.getTextAfterToken(line, "MemFree:");
					parser.parseAux(line);
					memFree = parser.nextAuxToken();
					double d = 0.0;
					try {
						d = Double.parseDouble(memFree);
					}
					catch (@SuppressWarnings("unused") Exception e) {
						d = -1.0;
					}
					if (d >= 0.0)
						dmemFree = d;
				}
				else
					if (line.startsWith("SwapTotal")) {
						line = Parser.getTextAfterToken(line, "SwapTotal:");
						parser.parseAux(line);
						swapTotal = parser.nextAuxToken();
					}
					else
						if (line.startsWith("SwapFree")) {
							line = Parser.getTextAfterToken(line, "SwapFree:");
							parser.parseAux(line);
							swapFree = parser.nextAuxToken();
						}

			line = parser.nextLine();
		}

		double dst = Double.valueOf(swapTotal).doubleValue();
		double dsf = Double.valueOf(swapFree).doubleValue();
		double dsu = dst - dsf;

		swapUsed = "" + dsu;
		swapUsage = "" + (1.0 - dsu / dst) * 100;

		memFree = "" + (dmemFree / 1024.0);
		memUsed = "" + ((dmemTotal - dmemFree) / 1024.0);
		memUsage = "" + (100.0 * (dmemTotal - dmemFree) / dmemTotal);

		output = exec.executeCommandReality("df -B 1024", "o");
		if (exec.isError())
			output = null;
		double size = 0.0, used = 0.0, available = 0.0, usage = 0.0;
		if (output != null && output != "") {
			parser.parse(output);
			line = parser.nextToken(" \t\n");
			int nr = 0;
			for (int i = 0; i < 6 && line != null; i++)
				line = parser.nextToken(" \t\n");
			while (true) {
				line = parser.nextToken(" \t\n");
				if (line == null)
					break;
				line = parser.nextToken(" \t\n"); // size
				double d = 0.0;
				try {
					d = Double.parseDouble(line);
				}
				catch (@SuppressWarnings("unused") Exception e) {
					d = -1.0;
				}
				if (d < 0.0)
					break;
				size += d;
				line = parser.nextToken(" \t\n"); // used
				d = 0.0;
				try {
					d = Double.parseDouble(line);
				}
				catch (@SuppressWarnings("unused") Exception e) {
					d = -1.0;
				}
				if (d < 0.0)
					break;
				used += d;
				line = parser.nextToken(" \t\n"); // available
				d = 0.0;
				try {
					d = Double.parseDouble(line);
				}
				catch (@SuppressWarnings("unused") Exception e) {
					d = -1.0;
				}
				if (d < 0.0)
					break;
				available += d;
				line = parser.nextToken(" \t\n"); // usage
				line = Parser.getTextBeforeToken(line, "%");
				d = 0.0;
				try {
					d = Double.parseDouble(line);
				}
				catch (@SuppressWarnings("unused") Exception e) {
					d = -1.0;
				}
				if (d < 0.0)
					break;
				usage += d;
				nr++;
				line = parser.nextToken(" \t\n");
				if (line == null)
					break;
			}
			diskTotal = "" + (size / (1024.0 * 1024.0)); // total size (GB)
			diskUsed = "" + (used / (1024.0 * 1024.0)); // used size (GB)
			diskFree = "" + (available / (1024.0 * 1024.0)); // free size
																// (GB)
			diskUsage = "" + (usage * 1.0 / nr); // usage (%)
		}
		else { // read from /proc/ide
			String files[] = Parser.listFiles("/proc/ide");
			if (files != null && files.length != 0)
				for (int i = 0; i < files.length; i++)
					if (files[i].startsWith("hd")) {
						parser.parseFromFile("/proc/ide/" + files[i] + "/capacity");
						line = parser.nextLine();
						double d = 0.0;
						try {
							d = Double.parseDouble(line);
						}
						catch (@SuppressWarnings("unused") Exception e) {
							d = -1.0;
						}
						if (d >= 0.0)
							size += d;
					}
			diskTotal = "" + (size / (1024.0 * 1024.0)); // disk total (GB)
			diskFree = diskTotal;
		}

		/**
		 * old version
		 * 
		 * String[] files = parser.listFiles("/proc");
		 * processesNo = null;
		 * if (files != null && files.length != 0) {
		 * int nr = 0;
		 * for (int i = 0; i < files.length; i++) {
		 * char[] chars = files[i].toCharArray();
		 * boolean isProc = true;
		 * for (int j = 0; j < chars.length; j++)
		 * if (!Character.isDigit(chars[j])) {
		 * isProc = false;
		 * break;
		 * }
		 * if (isProc) nr++;
		 * }
		 * processesNo = "" + nr;
		 * }
		 */

		/**
		 * new version
		 */
		output = exec.executeCommandReality("ps -e -A -o state", "");
		if (output != null && !output.equals("")) {
			parser.parse(output);
			line = parser.nextLine();
			processesNo = null;
			int pNo = 0;

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
				while (e.hasMoreElements()) {
					String key = e.nextElement();
					if (line.startsWith(key)) {
						int value = (states.get(key)).intValue();
						value++;
						pNo++;
						states.put(key, Integer.valueOf(value));
					}
				}
				line = parser.nextLine();
			}
			processesNo = "" + pNo;
		}

		/**
		 * run netstat
		 */
		output = exec.executeCommandReality("netstat -an", "");
		if (output != null && !output.equals("")) {
			parser.parse(output);
			line = parser.nextLine();

			netSockets.put("tcp", Integer.valueOf(0));
			netSockets.put("udp", Integer.valueOf(0));
			netSockets.put("unix", Integer.valueOf(0));
			netSockets.put("icm", Integer.valueOf(0));

			netTcpDetails.put("ESTABLISHED", Integer.valueOf(0));
			netTcpDetails.put("SYN_SENT", Integer.valueOf(0));
			netTcpDetails.put("SYN_RECV", Integer.valueOf(0));
			netTcpDetails.put("FIN_WAIT1", Integer.valueOf(0));
			netTcpDetails.put("FIN_WAIT2", Integer.valueOf(0));
			netTcpDetails.put("TIME_WAIT", Integer.valueOf(0));
			netTcpDetails.put("CLOSED", Integer.valueOf(0));
			netTcpDetails.put("CLOSE_WAIT", Integer.valueOf(0));
			netTcpDetails.put("LAST_ACK", Integer.valueOf(0));
			netTcpDetails.put("LISTEN", Integer.valueOf(0));
			netTcpDetails.put("CLOSING", Integer.valueOf(0));
			netTcpDetails.put("UNKNOWN", Integer.valueOf(0));

			try {
				String key = null;
				int value = 0;
				while (line != null) {
					if ((line.startsWith(" ") || line.startsWith("\t"))) {
						line = parser.nextLine();
						continue;
					}
					if (line.startsWith("tcp")) {
						key = "tcp";
						value = (netSockets.get(key)).intValue();
						value++;
						netSockets.put(key, Integer.valueOf(value));
						StringTokenizer st = new StringTokenizer(line, " []");
						while (st.hasMoreTokens()) {
							String element = st.nextToken();
							key = element;
							if (netTcpDetails.containsKey(element)) {
								value = (netTcpDetails.get(key)).intValue();
								value++;
								netTcpDetails.put(key, Integer.valueOf(value));
							}
						}
					}
					if (line.startsWith("udp")) {
						key = "udp";
						value = (netSockets.get(key)).intValue();
						value++;
						netSockets.put(key, Integer.valueOf(value));
					}
					if (line.startsWith("unix")) {
						key = "unix";
						value = (netSockets.get(key)).intValue();
						value++;
						netSockets.put(key, Integer.valueOf(value));
					}
					if (line.startsWith("icm")) {
						key = "icm";
						value = (netSockets.get(key)).intValue();
						value++;
						netSockets.put(key, Integer.valueOf(value));
					}
					line = parser.nextLine();
				}
			}
			catch (@SuppressWarnings("unused") Exception e) {
				// ignore
			}
		}

		parser.parseFromFile("/proc/loadavg");
		load1 = load5 = load15 = null;
		line = parser.nextToken(" \t\n"); // load1
		if (line != null) {
			double d = 0.0;
			try {
				d = Double.parseDouble(line);
			}
			catch (@SuppressWarnings("unused") Exception e) {
				d = -1.0;
			}
			if (d >= 0.0)
				load1 = "" + d;
			line = parser.nextToken(" \t\n"); // load5
			d = 0.0;
			try {
				d = Double.parseDouble(line);
			}
			catch (@SuppressWarnings("unused") Exception e) {
				d = -1.0;
			}
			if (d >= 0.0)
				load5 = "" + d;
			line = parser.nextToken(" \t\n"); // load15
			d = 0.0;
			try {
				d = Double.parseDouble(line);
			}
			catch (@SuppressWarnings("unused") Exception e) {
				d = -1.0;
			}
			if (d >= 0.0)
				load15 = "" + d;
		}

		parser.parseFromFile("/proc/net/dev");
		if (netInterfaces == null) {
			while (true) {
				line = parser.nextToken(":\n\t ");
				if (line == null)
					break;
				if (line.startsWith("eth") || line.startsWith("lo")) {
					addNetInterface(line);
					String name = line;
					line = parser.nextToken(" \t\n"); // bytes received
					long d = 0;
					long oldReceived = 0;
					if (dnetIn.containsKey(name)) {
						try {
							oldReceived = (dnetIn.get(name)).longValue();
						}
						catch (@SuppressWarnings("unused") Exception e) {
							oldReceived = -1;
						}
						try {
							d = Long.parseLong(line);
						}
						catch (@SuppressWarnings("unused") Exception e) {
							d = -1;
						}
						if (oldReceived >= 0 && d >= 0) {
							double in = (diffWithOverflowCheck(d, oldReceived)) / diffCall;
							// double in = (d - oldReceived) / diffCall;
							in = in / (1024.0 * 1024.0);
							oldReceived = d;
							netIn.put(name, "" + in);
							dnetIn.put(name, Long.valueOf(oldReceived));
						}
					}
					else {
						d = 0;
						try {
							d = Long.parseLong(line);
						}
						catch (@SuppressWarnings("unused") Exception e) {
							d = -1;
						}
						if (d >= 0) {
							netIn.put(name, "" + d);
							dnetIn.put(name, Long.valueOf(d));
						}
					}
					line = parser.nextToken(" \t\n"); // packets received
					for (int i = 0; i < 6; i++)
						line = parser.nextToken(" \t\n");
					line = parser.nextToken(" \t\n"); // bytes sent
					d = 0;
					long oldSent = 0;
					if (dnetOut.containsKey(name)) {
						try {
							oldSent = (dnetOut.get(name)).longValue();
						}
						catch (@SuppressWarnings("unused") Exception e) {
							oldSent = -1;
						}
						try {
							d = Long.parseLong(line);
						}
						catch (@SuppressWarnings("unused") Exception e) {
							d = -1;
						}
						if (oldSent >= 0 && d >= 0) {
							double out = (diffWithOverflowCheck(d, oldSent)) / diffCall;
							// double out = (d - oldSent) / diffCall;
							out = out / (1024.0 * 1024.0);
							oldSent = d;
							netOut.put(name, "" + out);
							dnetOut.put(name, Long.valueOf(oldSent));
						}
					}
					else {
						d = 0;
						try {
							d = Long.parseLong(line);
						}
						catch (@SuppressWarnings("unused") Exception e) {
							d = -1;
						}
						if (d >= 0) {
							netOut.put(name, "" + d);
							dnetOut.put(name, Long.valueOf(d));
						}
					}
					line = parser.nextToken(" \t\n"); // packets sent
				}
			}
		}
		else {
			while (true) {
				line = parser.nextToken(":\n\t ");
				if (line == null)
					break;
				boolean found = false;
				for (int i = 0; i < netInterfaces.length; i++)
					if (line.equals(netInterfaces[i])) {
						found = true;
						break;
					}
				if (found) {
					String name = line;
					line = parser.nextToken(" \t\n:"); // bytes received
					long d = 0;
					long oldReceived = 0;
					if (dnetIn.containsKey(name)) {
						try {
							oldReceived = (dnetIn.get(name)).longValue();
						}
						catch (@SuppressWarnings("unused") Exception e) {
							oldReceived = -1;
						}
						try {
							d = Long.parseLong(line);
						}
						catch (@SuppressWarnings("unused") Exception e) {
							d = -1;
						}
						if (oldReceived >= 0 && d >= 0) {
							double in = (diffWithOverflowCheck(d, oldReceived)) / diffCall;
							// double in = (d - oldReceived) / diffCall;
							in = in / (1024.0 * 1024.0);
							oldReceived = d;
							netIn.put(name, "" + in);
							dnetIn.put(name, Long.valueOf(oldReceived));
						}
					}
					else {
						d = 0;
						try {
							d = Long.parseLong(line);
						}
						catch (@SuppressWarnings("unused") Exception e) {
							d = -1;
						}
						if (d >= 0) {
							netIn.put(name, "" + d);
							dnetIn.put(name, Long.valueOf(d));
						}
					}
					line = parser.nextToken(" \t\n"); // packets received
					for (int i = 0; i < 6; i++)
						line = parser.nextToken(" \t\n");
					line = parser.nextToken(" \t\n"); // bytes sent
					d = 0;
					long oldSent = 0;
					if (dnetOut.containsKey(name)) {
						try {
							oldSent = (dnetOut.get(name)).longValue();
						}
						catch (@SuppressWarnings("unused") Exception e) {
							oldSent = -1;
						}
						try {
							d = Long.parseLong(line);
						}
						catch (@SuppressWarnings("unused") Exception e) {
							d = -1;
						}

						if (oldSent >= 0 && d >= 0) {
							double out = (diffWithOverflowCheck(d, oldSent)) / diffCall;
							// double out = (d - oldSent) / diffCall;
							out = out / (1024.0 * 1024.0);
							oldSent = d;
							netOut.put(name, "" + out);
							dnetOut.put(name, Long.valueOf(oldSent));
						}
					}
					else {
						d = 0;
						try {
							d = Long.parseLong(line);
						}
						catch (@SuppressWarnings("unused") Exception e) {
							d = -1;
						}
						if (d >= 0) {
							netOut.put(name, "" + d);
							dnetOut.put(name, Long.valueOf(d));
						}
					}
					line = parser.nextToken(" \t\n"); // packets sent
				}
			}
		}

		// add all monitored info to a hashmap for serial processing
		hm = new HashMap<>();
		hm.put(ApMonMonitoringConstants.LSYS_LOAD1, load1);
		hm.put(ApMonMonitoringConstants.LSYS_LOAD5, load5);
		hm.put(ApMonMonitoringConstants.LSYS_LOAD15, load15);

		hm.put(ApMonMonitoringConstants.LSYS_CPU_USR, cpuUsr);
		hm.put(ApMonMonitoringConstants.LSYS_CPU_NICE, cpuNice);
		hm.put(ApMonMonitoringConstants.LSYS_CPU_SYS, cpuSys);
		hm.put(ApMonMonitoringConstants.LSYS_CPU_IDLE, cpuIdle);
		hm.put(ApMonMonitoringConstants.LSYS_CPU_USAGE, cpuUsage);

		hm.put(ApMonMonitoringConstants.LSYS_MEM_FREE, memFree);
		hm.put(ApMonMonitoringConstants.LSYS_MEM_USED, memUsed);
		hm.put(ApMonMonitoringConstants.LSYS_MEM_USAGE, memUsage);

		hm.put(ApMonMonitoringConstants.LSYS_PAGES_IN, pagesIn);
		hm.put(ApMonMonitoringConstants.LSYS_PAGES_OUT, pagesOut);

		hm.put(ApMonMonitoringConstants.LSYS_SWAP_FREE, swapFree);
		hm.put(ApMonMonitoringConstants.LSYS_SWAP_USED, swapUsed);
		hm.put(ApMonMonitoringConstants.LSYS_SWAP_USAGE, swapUsage);

		hm.put(ApMonMonitoringConstants.LSYS_PROCESSES, processesNo);

		lastCall = newCall;
	}

	/**
	 * @return values
	 */
	public synchronized HashMap<Long, String> getHashedValues() {
		return hm;
	}

	/**
	 * @return MAC address
	 */
	public synchronized String getMacAddress() {

		if (hwAddress != null)
			hwAddress = hwAddress.trim();
		return hwAddress;
	}

	/**
	 * @return CPU usage
	 */
	public synchronized String getCPUUsage() {

		if (cpuUsage != null)
			cpuUsage = cpuUsage.trim();
		return cpuUsage;
	}

	/**
	 * @return CPU user
	 */
	public synchronized String getCPUUsr() {

		if (cpuUsr != null)
			cpuUsr = cpuUsr.trim();
		return cpuUsr;
	}

	/**
	 * @return CPU system
	 */
	public synchronized String getCPUSys() {

		if (cpuSys != null)
			cpuSys = cpuSys.trim();
		return cpuSys;
	}

	/**
	 * @return CPU nice
	 */
	public synchronized String getCPUNice() {

		if (cpuNice != null)
			cpuNice = cpuNice.trim();
		return cpuNice;
	}

	/**
	 * @return CPU idle
	 */
	public synchronized String getCPUIdle() {

		if (cpuIdle != null)
			cpuIdle = cpuIdle.trim();
		return cpuIdle;
	}

	/**
	 * @return Pages IN
	 */
	public synchronized String getPagesIn() {

		if (pagesIn != null)
			pagesIn = pagesIn.trim();
		return pagesIn;
	}

	/**
	 * @return Pages OUT
	 */
	public synchronized String getPagesOut() {

		if (pagesOut != null)
			pagesOut = pagesOut.trim();
		return pagesOut;
	}

	/**
	 * @return memory usage
	 */
	public synchronized String getMemUsage() {

		if (memUsage != null)
			memUsage = memUsage.trim();
		return memUsage;
	}

	/**
	 * @return Total memory
	 */
	public synchronized String getMemTotal() {

		if (memTotal != null)
			memTotal = memTotal.trim();
		return memTotal;
	}

	/**
	 * @return Used memory
	 */
	public synchronized String getMemUsed() {

		if (memUsed != null)
			memUsed = memUsed.trim();
		return memUsed;
	}

	/**
	 * @return Free memory
	 */
	public synchronized String getMemFree() {

		if (memFree != null)
			memFree = memFree.trim();
		return memFree;
	}

	/**
	 * @return Total swap
	 */
	public synchronized String getSwapTotal() {

		if (swapTotal != null)
			swapTotal = swapTotal.trim();
		return swapTotal;
	}

	/**
	 * @return Free swap
	 */
	public synchronized String getSwapFree() {

		if (swapTotal != null)
			swapTotal = swapTotal.trim();
		return swapTotal;
	}

	/**
	 * @return Disk IO
	 */
	public synchronized String getDiskIO() {

		if (diskIO != null)
			diskIO = diskIO.trim();
		return diskIO;
	}

	/**
	 * @return Disk total
	 */
	public synchronized String getDiskTotal() {

		if (diskTotal != null)
			diskTotal = diskTotal.trim();
		return diskTotal;
	}

	/**
	 * @return disk used
	 */
	public synchronized String getDiskUsed() {

		if (diskUsed != null)
			diskUsed = diskUsed.trim();
		return diskUsed;
	}

	/**
	 * @return disk free
	 */
	public synchronized String getDiskFree() {

		if (diskFree != null)
			diskFree = diskFree.trim();
		return diskFree;
	}

	/**
	 * @return disk usage
	 */
	public synchronized String getDiskUsage() {

		if (diskUsage != null)
			diskUsage = diskUsage.trim();
		return diskUsage;
	}

	/**
	 * @return number of processes
	 */
	public synchronized String getNoProcesses() {

		if (processesNo != null)
			processesNo = processesNo.trim();
		return processesNo;
	}

	/**
	 * @return load 1
	 */
	public synchronized String getLoad1() {

		if (load1 != null)
			load1 = load1.trim();
		return load1;
	}

	/**
	 * @return load 5
	 */
	public synchronized String getLoad5() {

		if (load5 != null)
			load5 = load5.trim();
		return load5;
	}

	/**
	 * @return processes in each state
	 */
	public synchronized Hashtable<String, Integer> getProcessesState() {
		return states;
	}

	/**
	 * @return sockets in each state
	 */
	public synchronized Hashtable<String, Integer> getNetSockets() {
		return netSockets;
	}

	/**
	 * @return TCP settings
	 */
	public synchronized Hashtable<String, Integer> getTcpDetails() {
		return netTcpDetails;
	}

	/**
	 * @return load 15
	 */
	public synchronized String getLoad15() {

		if (load15 != null)
			load15 = load15.trim();
		return load15;
	}

	/**
	 * @return network interfaces
	 */
	public synchronized String[] getNetInterfaces() {
		return netInterfaces;
	}

	/**
	 * @param netInterface
	 * @return the network interface with this name, if it exists
	 */
	public synchronized String getNetIn(String netInterface) {

		if (netIn.containsKey(netInterface)) {
			String str = netIn.get(netInterface);
			if (str != null)
				str = str.trim();
			return str;
		}
		return null;
	}

	/**
	 * @param netInterface
	 * @return the network interface of this name, if it exists
	 */
	public synchronized String getNetOut(String netInterface) {

		if (netOut.containsKey(netInterface)) {
			String str = netOut.get(netInterface);
			if (str != null)
				str = str.trim();
			return str;
		}
		return null;
	}

	/**
	 * stop monitoring
	 */
	public void stopIt() {
		exec.stopIt();
	}

	/**
	 * Computes the difference between the new value and the old value of a
	 * counter, considering the case when the counter reaches its maximum
	 * value and is reset. We assume the counter has 32 bits.
	 * 
	 * @param newVal
	 * @param oldVal
	 * @return diff
	 */
	public static long diffWithOverflowCheck(long newVal, long oldVal) {

		if (newVal >= oldVal) {
			return newVal - oldVal;
		}
		long vmax;
		long p32 = 1L << 32;
		long p64 = 1L << 64;
		if (oldVal < p32)
			vmax = p32; // 32 bits
		else
			vmax = p64; // 64 bits

		return newVal - oldVal + vmax;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ProcReader reader = new ProcReader();
		while (true) {
			reader.update();
			System.out.println("");
			System.out.println("CPU Sys: " + reader.getCPUSys());
			System.out.println("CPU Usr: " + reader.getCPUUsr());
			System.out.println("CPU Nice: " + reader.getCPUNice());
			System.out.println("CPU Idle: " + reader.getCPUIdle());
			System.out.println("CPU Usage: " + reader.getCPUUsage());
			System.out.println("");
			System.out.println("Pages in: " + reader.getPagesIn());
			System.out.println("Pages out: " + reader.getPagesOut());
			System.out.println("");
			System.out.println("Mem usage: " + reader.getMemUsage());
			System.out.println("Mem used: " + reader.getMemUsed());
			System.out.println("Mem free: " + reader.getMemFree());
			System.out.println("");
			System.out.println("Disk total: " + reader.getDiskTotal());
			System.out.println("Disk used: " + reader.getDiskUsed());
			System.out.println("Disk free: " + reader.getDiskFree());
			System.out.println("Disk usage: " + reader.getDiskUsage());
			System.out.println("Disk IO: " + reader.getDiskIO());
			System.out.println("");
			System.out.println("Processes: " + reader.getNoProcesses());
			System.out.println("Load1: " + reader.getLoad1());
			System.out.println("Load5: " + reader.getLoad5());
			System.out.println("Load15: " + reader.getLoad15());
			System.out.println("");
			System.out.println("MAC: " + reader.getMacAddress());
			System.out.println("Net IFS");
			String netIfs[] = reader.getNetInterfaces();
			if (netIfs != null) {
				for (int i = 0; i < netIfs.length; i++)
					System.out.print(netIfs[i] + " ");
				System.out.println("");
				System.out.println("Net in");
				for (int i = 0; i < netIfs.length; i++)
					System.out.print(reader.getNetIn(netIfs[i]) + " ");
				System.out.println("");
				System.out.println("Net out");
				for (int i = 0; i < netIfs.length; i++)
					System.out.print(reader.getNetOut(netIfs[i]) + " ");
				System.out.println("");
			}
			System.out.println("");
			try {
				Thread.sleep(1000);
			}
			catch (@SuppressWarnings("unused") InterruptedException e) {
				// ignore
			}
		}
	}

}

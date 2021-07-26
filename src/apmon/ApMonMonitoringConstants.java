/*
 * ApMon - Application Monitoring Tool
 * Version: 2.2.8
 *
 * Copyright (C) 2006 - 2010 California Institute of Technology
 *
 * Permission is hereby granted, free of charge, to use, copy and modify 
 * this software and its documentation (the "Software") for any
 * purpose, provided that existing copyright notices are retained in 
 * all copies and that this notice is included verbatim in any distributions
 * or substantial portions of the Software. 
 * This software is a part of the MonALISA framework (http://monalisa.caltech.edu).
 * Users of the Software are asked to feed back problems, benefits,
 * and/or suggestions about the software to the MonALISA Development Team
 * (MonALISA-CIT@cern.ch). Support for this software - fixing of bugs,
 * incorporation of new features - is done on a best effort basis. All bug
 * fixes and enhancements will be made available under the same terms and
 * conditions as the original software,
 
 * IN NO EVENT SHALL THE AUTHORS OR DISTRIBUTORS BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE, ITS DOCUMENTATION, OR ANY DERIVATIVES THEREOF,
 * EVEN IF THE AUTHORS HAVE BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 * THE AUTHORS AND DISTRIBUTORS SPECIFICALLY DISCLAIM ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT. THIS SOFTWARE IS
 * PROVIDED ON AN "AS IS" BASIS, AND THE AUTHORS AND DISTRIBUTORS HAVE NO
 * OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS.
 */

package apmon;

import java.util.HashMap;
import java.util.Map;


/**
 * @author ML team
 */
public final class ApMonMonitoringConstants {

    //SYS_* Specific
    /**
     * 
     */
    public static final long SYS_LOAD1            =   0x1L;
    /**
     * 
     */
    public static final Long LSYS_LOAD1           =   Long.valueOf(SYS_LOAD1);
    /**
     * 
     */
    public static final long SYS_LOAD5            =   0x2L;
    /**
     * 
     */
    public static final Long LSYS_LOAD5           =   Long.valueOf(SYS_LOAD5);
    /**
     * 
     */
    public static final long SYS_LOAD15           =   0x4L;
    /**
     * 
     */
    public static final Long LSYS_LOAD15          =   Long.valueOf(SYS_LOAD15);
    
    /**
     * 
     */
    public static final long SYS_CPU_USR          =   0x8L;
    /**
     * 
     */
    public static final Long LSYS_CPU_USR         =   Long.valueOf(SYS_CPU_USR);
    /**
     * 
     */
    public static final long SYS_CPU_SYS          =   0x10L;
    /**
     * 
     */
    public static final Long LSYS_CPU_SYS         =   Long.valueOf(SYS_CPU_SYS);
    /**
     * 
     */
    public static final long SYS_CPU_IDLE         =   0x20L;
    /**
     * 
     */
    public static final Long LSYS_CPU_IDLE        =   Long.valueOf(SYS_CPU_IDLE);
    /**
     * 
     */
    public static final long SYS_CPU_NICE         =   0x40L;
    /**
     * 
     */
    public static final Long LSYS_CPU_NICE        =   Long.valueOf(SYS_CPU_NICE);
    /**
     * 
     */
    public static final long SYS_CPU_USAGE        =   0x80L;
    /**
     * 
     */
    public static final Long LSYS_CPU_USAGE       =   Long.valueOf(SYS_CPU_USAGE);
    
    /**
     * 
     */
    public static final long SYS_MEM_FREE         =   0x100L;
    /**
     * 
     */
    public static final Long LSYS_MEM_FREE        =   Long.valueOf(SYS_MEM_FREE);
    /**
     * 
     */
    public static final long SYS_MEM_USED         =   0x200L;
    /**
     * 
     */
    public static final Long LSYS_MEM_USED        =   Long.valueOf(SYS_MEM_USED);
    /**
     * 
     */
    public static final long SYS_MEM_USAGE        =   0x400L;
    /**
     * 
     */
    public static final Long LSYS_MEM_USAGE       =   Long.valueOf(SYS_MEM_USAGE);
    
    /**
     * 
     */
    public static final long SYS_PAGES_IN         =   0x800L;
    /**
     * 
     */
    public static final Long LSYS_PAGES_IN        =   Long.valueOf(SYS_PAGES_IN);
    /**
     * 
     */
    public static final long SYS_PAGES_OUT        =   0x1000L;
    /**
     * 
     */
    public static final Long LSYS_PAGES_OUT       =   Long.valueOf(SYS_PAGES_OUT);
    
    /**
     * 
     */
    public static final long SYS_NET_IN           =   0x2000L;
    /**
     * 
     */
    public static final Long LSYS_NET_IN          =   Long.valueOf(SYS_NET_IN);
    /**
     * 
     */
    public static final long SYS_NET_OUT          =   0x4000L;
    /**
     * 
     */
    public static final Long LSYS_NET_OUT         =   Long.valueOf(SYS_NET_OUT);
    /**
     * 
     */
    public static final long SYS_NET_ERRS         =   0x8000L;
    /**
     * 
     */
    public static final Long LSYS_NET_ERRS        =   Long.valueOf(SYS_NET_ERRS);
    
    /**
     * 
     */
    public static final long SYS_SWAP_FREE        =   0x10000L;
    /**
     * 
     */
    public static final Long LSYS_SWAP_FREE       =   Long.valueOf(SYS_SWAP_FREE);
    /**
     * 
     */
    public static final long SYS_SWAP_USED        =   0x20000L;
    /**
     * 
     */
    public static final Long LSYS_SWAP_USED       =   Long.valueOf(SYS_SWAP_USED);
    /**
     * 
     */
    public static final long SYS_SWAP_USAGE       =   0x40000L;
    /**
     * 
     */
    public static final Long LSYS_SWAP_USAGE      =   Long.valueOf(SYS_SWAP_USAGE);

    /**
     * 
     */
    public static final long SYS_PROCESSES        =   0x80000L;
    /**
     * 
     */
    public static final Long LSYS_PROCESSES       =   Long.valueOf(SYS_PROCESSES);
    
    /**
     * 
     */
    public static final long SYS_NET_SOCKETS      =   0x100000L;
    /**
     * 
     */
    public static final Long LSYS_NET_SOCKETS     =   Long.valueOf(SYS_NET_SOCKETS);

    /**
     * 
     */
    public static final long SYS_NET_TCP_DETAILS  =   0x200000L;
    /**
     * 
     */
    public static final Long LSYS_NET_TCP_DETAILS =   Long.valueOf(SYS_NET_TCP_DETAILS);

    /**
     * 
     */
    public static final long SYS_UPTIME           =   0x400000L;
    /**
     * 
     */
    public static final Long LSYS_UPTIME          =   Long.valueOf(SYS_UPTIME);

    /**
     * 
     */
    static HashMap<String, Long>  HT_SYS_NAMES_TO_CONSTANTS = null;
    private static HashMap<Long, String>  HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES = null;
    
    static {
        HT_SYS_NAMES_TO_CONSTANTS = new HashMap<>();
        
        HT_SYS_NAMES_TO_CONSTANTS.put("sys_load1",  LSYS_LOAD1);
        HT_SYS_NAMES_TO_CONSTANTS.put("sys_load5",  LSYS_LOAD5);
        HT_SYS_NAMES_TO_CONSTANTS.put("sys_load15", LSYS_LOAD15);

        HT_SYS_NAMES_TO_CONSTANTS.put("sys_cpu_usr", LSYS_CPU_USR);
        HT_SYS_NAMES_TO_CONSTANTS.put("sys_cpu_sys", LSYS_CPU_SYS);
        HT_SYS_NAMES_TO_CONSTANTS.put("sys_cpu_idle", LSYS_CPU_IDLE);
        HT_SYS_NAMES_TO_CONSTANTS.put("sys_cpu_nice", LSYS_CPU_NICE);
        HT_SYS_NAMES_TO_CONSTANTS.put("sys_cpu_usage", LSYS_CPU_USAGE);
        
        HT_SYS_NAMES_TO_CONSTANTS.put("sys_mem_free", LSYS_MEM_FREE);
        HT_SYS_NAMES_TO_CONSTANTS.put("sys_mem_used", LSYS_MEM_USED);
        HT_SYS_NAMES_TO_CONSTANTS.put("sys_mem_usage", LSYS_MEM_USAGE);
       
        HT_SYS_NAMES_TO_CONSTANTS.put("sys_pages_in", LSYS_PAGES_IN);
        HT_SYS_NAMES_TO_CONSTANTS.put("sys_pages_out", LSYS_PAGES_OUT);

        HT_SYS_NAMES_TO_CONSTANTS.put("sys_net_in", LSYS_NET_IN);
        HT_SYS_NAMES_TO_CONSTANTS.put("sys_net_out", LSYS_NET_OUT);
        HT_SYS_NAMES_TO_CONSTANTS.put("sys_net_errs", LSYS_NET_ERRS);

        HT_SYS_NAMES_TO_CONSTANTS.put("sys_swap_free", LSYS_SWAP_FREE);
        HT_SYS_NAMES_TO_CONSTANTS.put("sys_swap_used", LSYS_SWAP_USED);
        HT_SYS_NAMES_TO_CONSTANTS.put("sys_swap_usage", LSYS_SWAP_USAGE);
        
        HT_SYS_NAMES_TO_CONSTANTS.put("sys_processes", LSYS_PROCESSES);
        HT_SYS_NAMES_TO_CONSTANTS.put("sys_net_sockets", LSYS_NET_SOCKETS);
        HT_SYS_NAMES_TO_CONSTANTS.put("sys_net_tcp_details", LSYS_NET_TCP_DETAILS);
        
        HT_SYS_NAMES_TO_CONSTANTS.put("sys_uptime", LSYS_UPTIME);
        

        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES = new HashMap<>();
        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_LOAD1,  "load1");
        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_LOAD5,  "load5");
        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_LOAD15, "load15");
        
        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_CPU_USR, "cpu_usr");
        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_CPU_SYS, "cpu_sys");
        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_CPU_IDLE, "cpu_idle");
        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_CPU_NICE, "cpu_nice");
        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_CPU_USAGE, "cpu_usage");
        
        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_MEM_FREE, "mem_free");
        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_MEM_USED, "mem_used");
        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_MEM_USAGE, "mem_usage");

        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_PAGES_IN, "pages_in");
        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_PAGES_OUT, "pages_out");
        
        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_NET_IN, "in");
        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_NET_OUT, "out");
        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_NET_ERRS, "errs");
        
        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_SWAP_FREE, "swap_free");
        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_SWAP_USED, "swap_used");
        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_SWAP_USAGE, "swap_usage");
        
        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_PROCESSES, "processes");
        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_NET_SOCKETS, "sockets");
        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_NET_TCP_DETAILS, "sockets_tcp");

        HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES.put(LSYS_UPTIME, "uptime");
        
    }
    
    
    //helper functions
    private static String getName(final Long param, final HashMap<String, Long> hm) {
        if(param == null) return null;
        
        for (Map.Entry<String, Long> me: hm.entrySet()){
        	if (me.getValue().equals(param))
        		return me.getKey();
        }
        
        //should not get normally here .... but who knows :)
        return null;
    }

    private static Long getIdx(final String name, final HashMap<String, Long> hm) {
        if( name == null ) return null;
        return hm.get(name);
    }
    
    private static String getMLParamName(final Long idx, final HashMap<Long, String> hm) {
        if(idx == null) return null;
        return hm.get(idx);
    }

    
    /**
     * this function can be slower ... it will be used only for debugging
     * @param param
     * @return system name
     */
    public static String getSysName(final Long param) {
        return getName(param, HT_SYS_NAMES_TO_CONSTANTS);
    }
          
    /**
     * @param name
     * @return system index
     */
    public static Long getSysIdx(final String name) {
        return getIdx(name, HT_SYS_NAMES_TO_CONSTANTS);
    }
    
    /**
     * @param idx
     * @return ml name
     */
    public static String getSysMLParamName(final Long idx) {
        return getMLParamName(idx, HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES);
    }

    /**
     * @param idx
     * @return system ml name
     */
    public static String getSysMLParamName(long idx) {
        return getMLParamName(Long.valueOf(idx), HT_SYS_CONSTANTS_TO_ML_PARAM_NAMES);
    }
    
    //GENERIC_*
    /**
     * 
     */
    public static final long GEN_HOSTNAME		=	0x1L;
    /**
     * 
     */
    public static final Long LGEN_HOSTNAME					=	Long.valueOf(GEN_HOSTNAME);
    /**
     * 
     */
    public static final long GEN_IP				=	0x2L;
    /**
     * 
     */
    public static final Long LGEN_IP						=	Long.valueOf(GEN_IP);
    /**
     * 
     */
    public static final long GEN_CPU_MHZ		=	0x4L;
    /**
     * 
     */
    public static final Long LGEN_CPU_MHZ  				    =   Long.valueOf(GEN_CPU_MHZ);
    /**
     * 
     */
    public static final long GEN_NO_CPUS		=   0x8L;
    /**
     * 
     */
    public static final Long LGEN_NO_CPUS 				    =   Long.valueOf(GEN_NO_CPUS);
    /**
     * 
     */
    public static final long GEN_TOTAL_MEM		=   0x10L;
    /**
     * 
     */
    public static final Long LGEN_TOTAL_MEM     			=   Long.valueOf(GEN_TOTAL_MEM);
    /**
     * 
     */
    public static final long GEN_TOTAL_SWAP		=   0x20L;
    /**
     * 
     */
    public static final Long LGEN_TOTAL_SWAP    			=   Long.valueOf(GEN_TOTAL_SWAP);
	/**
	 * 
	 */
	public static final long GEN_CPU_VENDOR_ID	=   0x40L;
    /**
     * 
     */
    public static final Long LGEN_CPU_VENDOR_ID				=   Long.valueOf(GEN_CPU_VENDOR_ID);
	/**
	 * 
	 */
	public static final long GEN_CPU_FAMILY		=   0x80L;
    /**
     * 
     */
    public static final Long LGEN_CPU_FAMILY    			=   Long.valueOf(GEN_CPU_FAMILY);
	/**
	 * 
	 */
	public static final long GEN_CPU_MODEL     	=   0x100L;
    /**
     * 
     */
    public static final Long LGEN_CPU_MODEL    				=   Long.valueOf(GEN_CPU_MODEL);
	/**
	 * 
	 */
	public static final long GEN_CPU_MODEL_NAME	=   0x200L;
    /**
     * 
     */
    public static final Long LGEN_CPU_MODEL_NAME    		=   Long.valueOf(GEN_CPU_MODEL_NAME);
	/**
	 * 
	 */
	public static final long GEN_BOGOMIPS		=   0x400L;
    /**
     * 
     */
    public static final Long LGEN_BOGOMIPS    				=   Long.valueOf(GEN_BOGOMIPS);
	
    /**
     * 
     */
    private static HashMap<String, Long>  HT_GEN_NAMES_TO_CONSTANTS = null;
    /**
     * 
     */
    private static HashMap<Long, String>  HT_GEN_CONSTANTS_TO_ML_PARAM_NAMES = null;

    static {
        HT_GEN_NAMES_TO_CONSTANTS = new HashMap<>();
        
        HT_GEN_NAMES_TO_CONSTANTS.put("hostname",  LGEN_HOSTNAME);
        HT_GEN_NAMES_TO_CONSTANTS.put("ip",  LGEN_IP);
        HT_GEN_NAMES_TO_CONSTANTS.put("cpu_MHz", LGEN_CPU_MHZ);
        HT_GEN_NAMES_TO_CONSTANTS.put("no_CPUs", LGEN_NO_CPUS);
        HT_GEN_NAMES_TO_CONSTANTS.put("total_mem", LGEN_TOTAL_MEM);
        HT_GEN_NAMES_TO_CONSTANTS.put("total_swap", LGEN_TOTAL_SWAP);
		HT_GEN_NAMES_TO_CONSTANTS.put("cpu_vendor_id", LGEN_CPU_VENDOR_ID);
		HT_GEN_NAMES_TO_CONSTANTS.put("cpu_family", LGEN_CPU_FAMILY);
		HT_GEN_NAMES_TO_CONSTANTS.put("cpu_model", LGEN_CPU_MODEL);
		HT_GEN_NAMES_TO_CONSTANTS.put("cpu_model_name", LGEN_CPU_MODEL_NAME);
		HT_GEN_NAMES_TO_CONSTANTS.put("bogomips", LGEN_BOGOMIPS);

        
        HT_GEN_CONSTANTS_TO_ML_PARAM_NAMES = new HashMap<>();
        HT_GEN_CONSTANTS_TO_ML_PARAM_NAMES.put(LGEN_HOSTNAME, "hostname");
        HT_GEN_CONSTANTS_TO_ML_PARAM_NAMES.put(LGEN_IP, "ip");
        HT_GEN_CONSTANTS_TO_ML_PARAM_NAMES.put(LGEN_CPU_MHZ, "cpu_MHZ");
        HT_GEN_CONSTANTS_TO_ML_PARAM_NAMES.put(LGEN_NO_CPUS, "no_CPUs");
        HT_GEN_CONSTANTS_TO_ML_PARAM_NAMES.put(LGEN_TOTAL_MEM, "total_mem");
        HT_GEN_CONSTANTS_TO_ML_PARAM_NAMES.put(LGEN_TOTAL_SWAP, "total_swap");
		HT_GEN_CONSTANTS_TO_ML_PARAM_NAMES.put(LGEN_CPU_VENDOR_ID, "cpu_vendor_id");
		HT_GEN_CONSTANTS_TO_ML_PARAM_NAMES.put(LGEN_CPU_FAMILY, "cpu_family");
		HT_GEN_CONSTANTS_TO_ML_PARAM_NAMES.put(LGEN_CPU_MODEL, "cpu_model");
		HT_GEN_CONSTANTS_TO_ML_PARAM_NAMES.put(LGEN_CPU_MODEL_NAME, "cpu_model_name");
		HT_GEN_CONSTANTS_TO_ML_PARAM_NAMES.put(LGEN_BOGOMIPS, "bogomips");
    }
    
    /**
     * @param param
     * @return generic name
     */
    public static String getGenName(final Long param) {
        return getName(param, HT_GEN_NAMES_TO_CONSTANTS);
    }
          
    /**
     * @param name
     * @return generic index
     */
    public static Long getGenIdx(final String name) {
        return getIdx(name, HT_GEN_NAMES_TO_CONSTANTS);
    }
    
    /**
     * @param idx
     * @return generic ML parameter name
     */
    public static String getGenMLParamName(final Long idx) {
        return getMLParamName(idx, HT_GEN_CONSTANTS_TO_ML_PARAM_NAMES);
    }

    /**
     * @param idx
     * @return generic ML parameter name
     */
    public static String getGenMLParamName(final long idx) {
        return getMLParamName(Long.valueOf(idx), HT_GEN_CONSTANTS_TO_ML_PARAM_NAMES);
    }

    //JOB_*

    /**
     * 
     */
    public static final long JOB_RUN_TIME       =   0x1L;
    /**
     * 
     */
    public static final Long LJOB_RUN_TIME      =   Long.valueOf(JOB_RUN_TIME);
    /**
     * 
     */
    public static final long JOB_CPU_TIME       =   0x2L;
    /**
     * 
     */
    public static final Long LJOB_CPU_TIME      =   Long.valueOf(JOB_CPU_TIME);
    /**
     * 
     */
    public static final long JOB_CPU_USAGE      =   0x4L;
    /**
     * 
     */
    public static final Long LJOB_CPU_USAGE     =   Long.valueOf(JOB_CPU_USAGE);
    
    /**
     * 
     */
    public static final long JOB_MEM_USAGE      =   0x8L;
    /**
     * 
     */
    public static final Long LJOB_MEM_USAGE     =   Long.valueOf(JOB_MEM_USAGE);
    /**
     * 
     */
    public static final long JOB_WORKDIR_SIZE   =   0x10L;
    /**
     * Work directory size, in MB
     */
    public static final Long LJOB_WORKDIR_SIZE  =   Long.valueOf(JOB_WORKDIR_SIZE);
    /**
     * 
     */
    public static final long JOB_DISK_TOTAL     =   0x20L;
    /**
     * 
     */
    public static final Long LJOB_DISK_TOTAL    =   Long.valueOf(SYS_CPU_IDLE);
    /**
     * 
     */
    public static final long JOB_DISK_USED      =   0x40L;
    /**
     * 
     */
    public static final Long LJOB_DISK_USED     =   Long.valueOf(JOB_DISK_USED);
    /**
     * 
     */
    public static final long JOB_DISK_FREE      =   0x80L;
    /**
     * 
     */
    public static final Long LJOB_DISK_FREE     =   Long.valueOf(JOB_DISK_FREE);
    
    /**
     * 
     */
    public static final long JOB_DISK_USAGE     =   0x100L;
    /**
     * 
     */
    public static final Long LJOB_DISK_USAGE    =   Long.valueOf(JOB_DISK_USAGE);
    /**
     * Virtual memory size, in KB
     */
    public static final long JOB_VIRTUALMEM     =   0x200L;
    /**
     * Virtual memory size, in KB
     */
    public static final Long LJOB_VIRTUALMEM    =   Long.valueOf(JOB_VIRTUALMEM);
    /**
     * Resident size, in KB
     */
    public static final long JOB_RSS            =   0x400L;
    /**
     * Resident size, in KB
     */
    public static final Long LJOB_RSS           =   Long.valueOf(JOB_RSS);
	/**
	 * 
	 */
	public static final long JOB_OPEN_FILES     =   0x800L;
    /**
     * 
     */
    public static final Long LJOB_OPEN_FILES    =   Long.valueOf(JOB_OPEN_FILES);
    
    /**
     * Key for the resident proportional set size of the child process(es)
     */
    public static final long JOB_PSS			= 0x1000L;
    
    /**
     * Object value of the same PSS key
     */
    public static final Long LJOB_PSS 			= Long.valueOf(JOB_PSS);
    
    /**
     * Key for the swapped out proportional set size of the child process(es)
     */
    public static final long JOB_SWAPPSS		= 0x2000L;
    
    /**
     * Object value of the same SWAP PSS key
     */
    public static final Long LJOB_SWAPPSS 		= Long.valueOf(JOB_SWAPPSS);
    

    private static HashMap<String, Long>  HT_JOB_NAMES_TO_CONSTANTS = null;
    private static HashMap<Long, String>  HT_JOB_CONSTANTS_TO_ML_PARAM_NAMES = null;
    
    static {
        HT_JOB_NAMES_TO_CONSTANTS = new HashMap<>();
        
        HT_JOB_NAMES_TO_CONSTANTS.put("job_run_time",  LJOB_RUN_TIME);
        HT_JOB_NAMES_TO_CONSTANTS.put("job_cpu_time",  LJOB_CPU_TIME);
        HT_JOB_NAMES_TO_CONSTANTS.put("job_cpu_usage", LJOB_CPU_USAGE);

        HT_JOB_NAMES_TO_CONSTANTS.put("job_mem_usage", LJOB_MEM_USAGE);
        HT_JOB_NAMES_TO_CONSTANTS.put("job_workdir_size", LJOB_WORKDIR_SIZE);
        HT_JOB_NAMES_TO_CONSTANTS.put("job_disk_total", LJOB_DISK_TOTAL);
        HT_JOB_NAMES_TO_CONSTANTS.put("job_disk_used", LJOB_DISK_USED);
        HT_JOB_NAMES_TO_CONSTANTS.put("job_disk_free", LJOB_DISK_FREE);
        
        HT_JOB_NAMES_TO_CONSTANTS.put("job_disk_usage", LJOB_DISK_USAGE);
        HT_JOB_NAMES_TO_CONSTANTS.put("job_virtualmem", LJOB_VIRTUALMEM);
        HT_JOB_NAMES_TO_CONSTANTS.put("job_rss", LJOB_RSS);
		HT_JOB_NAMES_TO_CONSTANTS.put("job_open_files", LJOB_OPEN_FILES);
        HT_JOB_NAMES_TO_CONSTANTS.put("job_pss", LJOB_PSS);
		HT_JOB_NAMES_TO_CONSTANTS.put("job_swappss", LJOB_SWAPPSS);
        
        HT_JOB_CONSTANTS_TO_ML_PARAM_NAMES = new HashMap<>();
        HT_JOB_CONSTANTS_TO_ML_PARAM_NAMES.put(LJOB_RUN_TIME, "run_time");
        HT_JOB_CONSTANTS_TO_ML_PARAM_NAMES.put(LJOB_CPU_TIME, "cpu_time");
        HT_JOB_CONSTANTS_TO_ML_PARAM_NAMES.put(LJOB_CPU_USAGE, "cpu_usage");
        
        HT_JOB_CONSTANTS_TO_ML_PARAM_NAMES.put(LJOB_MEM_USAGE, "mem_usage");
        HT_JOB_CONSTANTS_TO_ML_PARAM_NAMES.put(LJOB_WORKDIR_SIZE, "workdir_size");
        HT_JOB_CONSTANTS_TO_ML_PARAM_NAMES.put(LJOB_DISK_TOTAL, "disk_total");
        HT_JOB_CONSTANTS_TO_ML_PARAM_NAMES.put(LJOB_DISK_USED, "disk_used");
        HT_JOB_CONSTANTS_TO_ML_PARAM_NAMES.put(LJOB_DISK_FREE, "disk_free");
        HT_JOB_CONSTANTS_TO_ML_PARAM_NAMES.put(LJOB_DISK_USAGE, "disk_usage");
        
        HT_JOB_CONSTANTS_TO_ML_PARAM_NAMES.put(LJOB_VIRTUALMEM, "virtualmem");
        HT_JOB_CONSTANTS_TO_ML_PARAM_NAMES.put(LJOB_RSS, "rss");
		HT_JOB_CONSTANTS_TO_ML_PARAM_NAMES.put(LJOB_OPEN_FILES, "open_files");

        HT_JOB_CONSTANTS_TO_ML_PARAM_NAMES.put(LJOB_PSS, "pss");
		HT_JOB_CONSTANTS_TO_ML_PARAM_NAMES.put(LJOB_SWAPPSS, "swappss");
    }    

    /**
     * @param param
     * @return job name
     */
    public static String getJobName(final Long param) {
        return getName(param, HT_JOB_NAMES_TO_CONSTANTS);
    }
          
    /**
     * @param name
     * @return job index
     */
    public static Long getJobIdx(final String name) {
        return getIdx(name, HT_JOB_NAMES_TO_CONSTANTS);
    }
    
    /**
     * @param idx
     * @return job ML parameter name
     */
    public static String getJobMLParamName(final Long idx) {
        return getMLParamName(idx, HT_JOB_CONSTANTS_TO_ML_PARAM_NAMES);
    }

    /**
     * @param idx
     * @return job ML parameter name
     */
    public static String getJobMLParamName(final long idx) {
        return getMLParamName(Long.valueOf(idx), HT_JOB_CONSTANTS_TO_ML_PARAM_NAMES);
    }
}

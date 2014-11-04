/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iohiccup;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.Instrumentation;
import org.LatencyUtils.LatencyStats;

/**
 *
 * @author fijiol
 */
public class IOHiccup {

    public static volatile boolean initialized = false;
    public static volatile boolean finishByError = false;
    
    public static long startTime;
    public static LatencyStats i2oLS;
    public static LatencyStats o2iLS;
    public static boolean isAlive = true;

    public static IOHiccupConfiguration configuration;
    public static IOStatistic ioStat;

    public static void main(String[] args) throws UnsupportedEncodingException, IOException, InterruptedException {
        System.out.println("ioHiccup.jar doesn't have now functional main method. Please rerun your application as:\n\t"
                + "java -javaagent:ioHiccup.jar -Xbootclasspath/a:ioHiccup.jar -jar yourapp.jar");
        System.exit(1);
    }

    private static String printKeys(String[] keys) {
        StringBuilder sb = new StringBuilder();
        for (String s : keys) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(s);
        }
        return sb.toString();
    }
    
    public static void printHelp() {
        System.out.println("Usage:");
        System.out.println("\tjava -jar ioHiccup.jar[=<args>] -Xbootclasspath/a:ioHiccup.jar -jar yourapp.jar\n");
        System.out.println("\t\twhere <args> is an comma separated list of arguments like arg1,arg2=val2 e.t.c\n");
        System.out.println("\t\tARGUMENTS:");
        System.out.println("\t\t  " + printKeys(help) + " \t\t to print help");
        System.out.println("\t\t  " + printKeys(remoteaddr) + " \t\t to set filter of remote address");
        System.out.println("\t\t  " + printKeys(remoteport) + " \t\t to set filter of remote port");
        System.out.println("\t\t  " + printKeys(localport) + " \t\t to set filter of local port");
        System.out.println("\t\t  " + printKeys(loginterval) + " \t\t to set log sampling interval");
        System.out.println("\t\t  " + printKeys(startdelaying) + " \t\t to specify time delay to start ioHiccup");
        System.out.println("\t\t  " + printKeys(workingtime) + " \t\t to specify how long ioHiccup will work");
        
        System.out.println("\n");
        System.out.println("Please rerun application with proper CLI options.\n");
    }
    
    private static String fixupRegex(String str) {
        if (true) {
            return str;
        }       
        try {
        "".matches(str);
        } catch (Exception e) {
            System.err.println("WARN: regex '" + str + "' is not understandable");
            System.exit(1); //??
            return null;
        }
        return str;
    }
    
    public static void premain(String agentArgument, Instrumentation instrumentation) {
        
        //Check here another instances and exit if then!
        if (initialized) {
            System.err.println("\nTrying to run multiple instances of ioHiccup simultaneously.\n"
                    + "\nPlease run only one at the same time.\n\n");
            finishByError = true;
            System.exit(1);
        }
        
        configuration = new IOHiccupConfiguration();
        ioStat = new IOStatistic();

        startTime = System.currentTimeMillis();

        if (null != agentArgument) {
            for (String v : agentArgument.split(",")) {
                String[] vArr = v.split("=");
                if (vArr.length > 2) {
                    System.out.println("Wrong format ioHiccup arguments.\n");
                    printHelp();
                    System.exit(1);
                }
                if (hasKey(help, vArr[0])) {
                    printHelp();
                    System.exit(0);
                }
                if (hasKey(remoteaddr, vArr[0])) {
                    configuration.remoteaddr = fixupRegex(vArr[1]);
                }
                if (hasKey(localport, vArr[0])) {
                    configuration.localport = fixupRegex(vArr[1]);
                }
                if (hasKey(remoteport, vArr[0])) {
                    configuration.remoteport = fixupRegex(vArr[1]);
                }
                if (hasKey(loginterval, vArr[0])) {
                    configuration.logWriterInterval = Long.valueOf(vArr[1]);
                }
                if (hasKey(startdelaying, vArr[0])) {
                    configuration.startDelaying = Long.valueOf(vArr[1]);
                }
                if (hasKey(workingtime, vArr[0])) {
                    configuration.workingTime = Long.valueOf(vArr[1]);
                }
                //delay start time
                //how long to work
            }
        }

        instrumentation.addTransformer(new IOHiccupTransformer(configuration));

        //Some temporary place to print collected statistic.
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                if (finishByError) {
                    return;
                }
                System.out.println(" \\n");
                System.out.println("***************************************************************");
                System.out.println("ioHiccupStatistic: ");
                System.out.println("***************************************************************");
                System.out.println(" " + IOHiccup.ioStat.processedSocket + " sockets was processed");
            }

        });

        i2oLS = new LatencyStats();
        o2iLS = new LatencyStats();

        IOHiccupLogWriter ioHiccupLogWriter = new IOHiccupLogWriter();
        ioHiccupLogWriter.start();
        
        initialized = true;
    }
    
    private static final String[] remoteaddr = {"-raddr", "remote-addr"};
    private static final String[] loginterval = {"-si", "sample-interval"};
    private static final String[] remoteport = {"-rport", "remote-port"};
    private static final String[] localport = {"-lport", "local-port"};
    private static final String[] help = {"-h", "--help", "help", "h"};
    private static final String[] startdelaying = {"-start", "start"};
    private static final String[] workingtime = {"-fin", "finish-after"};

    private static boolean hasKey(String[] list, String key) {
        for (String s : list) {
            if (s.equals(key)) {
                return true;
            }
        }
        return false;
    }
}

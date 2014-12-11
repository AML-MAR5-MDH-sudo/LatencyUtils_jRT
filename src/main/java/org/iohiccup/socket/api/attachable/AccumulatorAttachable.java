/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iohiccup.socket.api.attachable;

import java.net.InetAddress;
import java.net.SocketImpl;
import org.iohiccup.socket.regular.Accumulator;
import org.iohiccup.impl.Configuration;
import org.iohiccup.socket.api.IOHic;
import org.iohiccup.impl.IOHiccup;


public class AccumulatorAttachable extends Accumulator {
    private static boolean match(String a, String filter) {
        //return a.matches(".*" + filter + ".*");
        if (null == filter) {
            return false;
        }
        return a.equals(filter);
    }
    
    private static boolean match(IOHiccup ioHiccup, InetAddress remoteAddress, int remotePort, int localPort) {
        
        for (Configuration.IOFilterEntry entry : ioHiccup.configuration.filterEntries) {
            if (null != entry.remoteaddr  &&
                    !match(remoteAddress.getHostAddress(), entry.remoteaddr) &&
                    !match(remoteAddress.getHostName(), entry.remoteaddr)  ) {
                return false;
            }
            if (null != entry.remoteport && 
                    !match(String.valueOf(remotePort), entry.remoteport)) {
                return false;
            }
            if (null != entry.localport && 
                    !match(String.valueOf(localPort), entry.localport)) {
                return false;
            }
        }
        
        //System.out.println("Calculate hiccups between " + remoteAddress + ":" + remotePort + " <-> " + "127.0.0.1:" + localPort); //Print on debug level?
        
        return true;
    }    
    
    public static IOHic initializeIOHic(IOHiccup ioHiccup, SocketImpl sock, InetAddress remoteAddress, int remotePort, int localPort) {
        //System.out.println("initializeIOHic " + sock + "," + remoteAddress + "," + remotePort + "," + localPort);;
        
        IOHic iohic = null;
        
        if (ioHiccup.sockHiccups.containsKey(sock)) {
            return ioHiccup.sockHiccups.get(sock);
        } else {
            iohic = new IOHic();
            ioHiccup.sockHiccups.put(sock, iohic);
        }
        
        //Decide to filter or not?
        if (!match(ioHiccup, remoteAddress, remotePort, localPort)) { 
            //sockHiccups.put(sock, null); //??!
            ioHiccup.sockHiccups.remove(sock);
            
            return null;
        }
        
        ++ioHiccup.ioStat.processedSocket;
        
        return iohic;
    }

    
    
    public static IOHiccupAttachable getIOHiccup(String uuid) {
        return (IOHiccupAttachable) IOHiccupAttachable.ioHiccupWorkers.get(uuid);
    }
    
    public static boolean hasAIOHic(String key, SocketImpl sock) {
        return null != getAIOHic(key, null);
    }


    public static void putTimestampReadAfter(IOHiccup ioHiccup, IOHic hic) {
        hic.i2oReadTime = System.nanoTime();
        hic.i2oLastRead = true;
    }
    
    public static void putTimestampWriteBefore(IOHiccup ioHiccup, IOHic hic) {
        hic.i2oWriteTime = System.nanoTime();
        if (hic.i2oLastRead && (hic.i2oLatency = hic.i2oWriteTime - hic.i2oReadTime) > 0) {
            ioHiccup.i2oLS.recordLatency(hic.i2oLatency);
        }
        hic.i2oLastRead = false;
    }
    
    public static void putTimestampWriteAfter(IOHiccup ioHiccup, IOHic hic) {
        hic.o2iReadTime = System.nanoTime();
        hic.o2iLastWrite = true;
    }
    
    public static void putTimestampReadBefore(IOHiccup ioHiccup, IOHic hic) {
        hic.o2iWriteTime = System.nanoTime();
        if (hic.o2iLastWrite && (hic.o2iLatency = hic.o2iWriteTime - hic.o2iReadTime) > 0) {
            ioHiccup.o2iLS.recordLatency(hic.o2iLatency);
        }
        hic.o2iLastWrite = false;
    }
    
    public static String dumpIOHiccups() {
        StringBuilder sb = new StringBuilder();

        return sb.toString();
    }   
    
    public static IOHiccup getAIOHiccup(String key, SocketImpl sock) {
        return getIOHiccup(key);
    }
    
    public static IOHic getAIOHic(String key, SocketImpl sock) {
        return getAIOHiccup(key, sock).sockHiccups.get(sock);
    }
}

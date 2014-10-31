/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iohiccup;

import java.net.SocketImpl;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author fijiol
 */

/*
 * 
 */
public class IOHiccupAccumulator {

    static Map<SocketImpl, IOHic> sockHiccups = new ConcurrentHashMap(new WeakHashMap<SocketImpl, IOHic>());
    
    private static IOHic getIOHic(SocketImpl sock) {
        IOHic hic = sockHiccups.get(sock);
        if (null == hic) {
            hic = new IOHic();
            sockHiccups.put(sock, hic);
        }
        return hic;
    }
    
    public static IOHic initializeIOHic(SocketImpl sock) {
        IOHic iohic = getIOHic(sock);
        //Decide to filter or not?
        return iohic;
    }
    
    public static void putTimestampReadAfter(IOHic hic) {
        hic.i2oReadTime = System.nanoTime();
        hic.i2oLastRead = true;
    }
    
    public static void putTimestampWriteBefore(IOHic hic) {
        hic.i2oWriteTime = System.nanoTime();
        if (hic.i2oLastRead && (hic.i2oLatency = hic.i2oWriteTime - hic.i2oReadTime) > 0) {
            IOHiccup.i2oLS.recordLatency(hic.i2oLatency);
        }
        hic.i2oLastRead = false;
    }
    
    public static void putTimestampWriteAfter(IOHic hic) {
        hic.o2iReadTime = System.nanoTime();
        hic.o2iLastWrite = true;
    }
    
    public static void putTimestampReadBefore(IOHic hic) {
        hic.o2iWriteTime = System.nanoTime();
        if (hic.o2iLastWrite && (hic.o2iLatency = hic.o2iWriteTime - hic.o2iReadTime) > 0) {
            IOHiccup.o2iLS.recordLatency(hic.o2iLatency);
        }
        hic.o2iLastWrite = false;
    }
    
    public static String dumpIOHiccups() {
        StringBuilder sb = new StringBuilder();

        return sb.toString();
    }
    
}

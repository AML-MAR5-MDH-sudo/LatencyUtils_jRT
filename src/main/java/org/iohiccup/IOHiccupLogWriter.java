/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iohiccup;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramLogWriter;

/**
 *
 * @author fijiol
 */
public class IOHiccupLogWriter extends Thread {

    public IOHiccupLogWriter() {
        setDaemon(true);
    }

    @Override
    public void run() {
        HistogramLogWriter i2olog = null;
        HistogramLogWriter o2ilog = null;
        try {
            i2olog = new HistogramLogWriter(new File("i2o.hlog"));
            o2ilog = new HistogramLogWriter(new File("o2i.hlog"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IOHiccup.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        try {
            i2olog.outputLegend();
            o2ilog.outputLegend();
            i2olog.outputStartTime(IOHiccup.startTime);
            o2ilog.outputStartTime(IOHiccup.startTime);
            
            while ((System.currentTimeMillis() - IOHiccup.startTime < IOHiccup.configuration.startDelaying)) {
                IOHiccup.i2oLS.forceIntervalSample();
                Histogram intervalHistogram = IOHiccup.i2oLS.getIntervalHistogram();
                IOHiccup.o2iLS.forceIntervalSample();
                Histogram intervalHistogram2 = IOHiccup.o2iLS.getIntervalHistogram();
                
                Thread.sleep(IOHiccup.configuration.logWriterInterval);
            }
            
            while ((System.currentTimeMillis() - IOHiccup.startTime < IOHiccup.configuration.workingTime) && IOHiccup.isAlive && !Thread.interrupted()) {
                
                IOHiccup.i2oLS.forceIntervalSample();
                Histogram intervalHistogram = IOHiccup.i2oLS.getIntervalHistogram();
                intervalHistogram.setStartTimeStamp(intervalHistogram.getStartTimeStamp() - IOHiccup.startTime);
                intervalHistogram.setEndTimeStamp(intervalHistogram.getEndTimeStamp() - IOHiccup.startTime);
                i2olog.outputIntervalHistogram(intervalHistogram);
                
                IOHiccup.o2iLS.forceIntervalSample();
                Histogram intervalHistogram2 = IOHiccup.o2iLS.getIntervalHistogram();
                intervalHistogram2.setStartTimeStamp(intervalHistogram2.getStartTimeStamp() - IOHiccup.startTime);
                intervalHistogram2.setEndTimeStamp(intervalHistogram2.getEndTimeStamp() - IOHiccup.startTime);
                o2ilog.outputIntervalHistogram(intervalHistogram2);
                
                
                Thread.sleep(IOHiccup.configuration.logWriterInterval);
                
            }
        } catch (InterruptedException ex) {
            //Nothing to do?
        } finally {
            //Nothing to do?
            //Need to flush logs?
            IOHiccup.i2oLS.stop();
            IOHiccup.o2iLS.stop();
        }
    }
    
}

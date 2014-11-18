/**
 * Written by Fedor Burdun of Azul Systems, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Fedor Burdun
 */
package org.iohiccup;

import java.lang.instrument.Instrumentation;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.NotFoundException;

public class IOHiccupTransformerAttachable extends IOHiccupTransformer {

    public IOHiccupTransformerAttachable(IOHiccup ioHiccup) {
        super(ioHiccup);
        accumulatorImplementationPackage = "org.iohiccup.";
        accumulatorImplementationClass = accumulatorImplementationPackage + "IOHiccupAccumulatorAttachable";
        
        this.iohiccup_field_name = accumulatorImplementationClass + ".getAIOHiccup(\"" + configuration.uuid + "\", impl)";
        this.iohic_field_name = accumulatorImplementationClass + ".getAIOHic(\"" + configuration.uuid + "\", impl)";
        
        if (configuration.printExceptions) {
            
            debugPre =  
                    "System.out.println(\"HAS IO HIC == \" + " + " (null != "+ iohic_field_name + " && null != " + iohiccup_field_name + ") + ';' +  " + iohic_field_name + " + ';' + " + iohiccup_field_name + " + ';' + "
                    + configuration.uuid + " + ';' + impl" + ");" + 
                    " try { ";
            debugPost = " } catch (Exception e) { e.printStackTrace(); System.exit(1); } ";
        } else {
            debugPre = "";
            debugPost = "";
        } 
        this.checkString = " if (null != "+ iohic_field_name + " && null != " + iohiccup_field_name + ") ";
    }

    @Override
    public void attachTo(Instrumentation instrumentation) {
        instrumentation.addTransformer(this, true);
    }    
    
    @Override
    public void doIOStreamsConstructor(String className, CtBehavior method) throws NotFoundException, CannotCompileException {
        if (method.getName().startsWith("SocketOutputStream") || method.getName().startsWith("SocketInputStream") ) {

            method.insertAfter(
                    debugPre +
                            accumulatorImplementationClass + 
                    ".initializeIOHic("+ iohiccup_field_name +", impl, impl.getInetAddress(), impl.getPort(), impl.getLocalPort());" +
                    debugPost);
        }
    }
        
    
}

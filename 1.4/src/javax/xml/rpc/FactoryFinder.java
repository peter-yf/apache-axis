/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.xml.rpc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * This code is designed to implement the pluggability
 * feature and is designed to both compile and run on JDK version 1.1 and
 * later.  The code also runs both as part of an unbundled jar file and
 * when bundled as part of the JDK.
 *
 * This class is duplicated for each subpackage so keep it in sync.
 * It is package private and therefore is not exposed as part of the JAXRPC
 * API.
 */
class FactoryFinder {
    /** Set to true for debugging. */
    private static final boolean debug = false;

    private static void debugPrintln(String msg) {
        if (debug) {
            System.err.println("JAXRPC: " + msg);
        }
    }

    /**
     * Figure out which ClassLoader to use.  For JDK 1.2 and later use
     * the context ClassLoader.
     *
     * @return the <code>ClassLoader</code>
     * @throws ConfigurationError   if this class is unable to work with the
     *              host JDK
     */
    private static ClassLoader findClassLoader()
        throws ConfigurationError
    {
        Method m = null;

        try {
            m = Thread.class.getMethod("getContextClassLoader", null);
        } catch (NoSuchMethodException e) {
            // Assume that we are running JDK 1.1, use the current ClassLoader
            debugPrintln("assuming JDK 1.1");
            return FactoryFinder.class.getClassLoader();
        }

        try {
            return (ClassLoader) m.invoke(Thread.currentThread(), null);
        } catch (IllegalAccessException e) {
            // assert(false)
            throw new ConfigurationError("Unexpected IllegalAccessException",
                                         e);
        } catch (InvocationTargetException e) {
            // assert(e.getTargetException() instanceof SecurityException)
            throw new ConfigurationError("Unexpected InvocationTargetException",
                                         e);
        }
    }

    /**
     * Create an instance of a class using the specified
     * <code>ClassLoader</code>, or if that fails from the
     * <code>ClassLoader</code> that loaded this class.
     *
     * @param className     the name of the class to instantiate
     * @param classLoader   a <code>ClassLoader</code> to load the class from
     *
     * @return a new <code>Object</code> that is an instance of the class of
     *              the given name from the given class loader
     * @throws ConfigurationError   if the class could not be found or
     *              instantiated
     */
    private static Object newInstance(String className,
                                      ClassLoader classLoader)
        throws ConfigurationError
    {
        try {
            if (classLoader != null) {
                try {
                    return classLoader.loadClass(className).newInstance ();
                } catch (ClassNotFoundException x) {
                      // try again
                }
            }
            return Class.forName(className).newInstance();
        } catch (ClassNotFoundException x) {
            throw new ConfigurationError(
                "Provider " + className + " not found", x);
        } catch (Exception x) {
            throw new ConfigurationError(
                "Provider " + className + " could not be instantiated: " + x,
                x);
        }
    }

    /**
     * Finds the implementation Class object in the specified order.  Main
     * entry point.
     * @return Class object of factory, never null
     *
     * @param factoryId             Name of the factory to find, same as
     *                              a property name
     * @param fallbackClassName     Implementation class name, if nothing else
     *                              is found.  Use null to mean no fallback.
     *
     * @exception FactoryFinder.ConfigurationError
     *
     * Package private so this code can be shared.
     */
    static Object find(String factoryId, String fallbackClassName)
        throws ConfigurationError
    {
        debugPrintln("debug is on");

        ClassLoader classLoader = findClassLoader();

        // Use the system property first
        try {
            String systemProp =
                System.getProperty( factoryId );
            if( systemProp!=null) {
                debugPrintln("found system property " + systemProp);
                return newInstance(systemProp, classLoader);
            }
        } catch (SecurityException se) {
        }

        // try to read from $java.home/lib/xml.properties
        try {
            String javah=System.getProperty( "java.home" );
            String configFile = javah + File.separator +
                "lib" + File.separator + "jaxrpc.properties";
            File f=new File( configFile );
            if( f.exists()) {
                Properties props=new Properties();
                props.load( new FileInputStream(f));
                String factoryClassName = props.getProperty(factoryId);
                debugPrintln("found java.home property " + factoryClassName);
                return newInstance(factoryClassName, classLoader);
            }
        } catch(Exception ex ) {
            if( debug ) ex.printStackTrace();
        }

        String serviceId = "META-INF/services/" + factoryId;
        // try to find services in CLASSPATH
        try {
            InputStream is=null;
            if (classLoader == null) {
                is=ClassLoader.getSystemResourceAsStream( serviceId );
            } else {
                is=classLoader.getResourceAsStream( serviceId );
            }

            if( is!=null ) {
                debugPrintln("found " + serviceId);

                // Read the service provider name in UTF-8 as specified in
                // the jar spec.  Unfortunately this fails in Microsoft
                // VJ++, which does not implement the UTF-8
                // encoding. Theoretically, we should simply let it fail in
                // that case, since the JVM is obviously broken if it
                // doesn't support such a basic standard.  But since there
                // are still some users attempting to use VJ++ for
                // development, we have dropped in a fallback which makes a
                // second attempt using the platform's default encoding. In
                // VJ++ this is apparently ASCII, which is a subset of
                // UTF-8... and since the strings we'll be reading here are
                // also primarily limited to the 7-bit ASCII range (at
                // least, in English versions), this should work well
                // enough to keep us on the air until we're ready to
                // officially decommit from VJ++. [Edited comment from
                // jkesselm]
                BufferedReader rd;
                try {
                    rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                } catch (java.io.UnsupportedEncodingException e) {
                    rd = new BufferedReader(new InputStreamReader(is));
                }

                String factoryClassName = rd.readLine();
                rd.close();

                if (factoryClassName != null &&
                    ! "".equals(factoryClassName)) {
                    debugPrintln("loaded from services: " + factoryClassName);
                    return newInstance(factoryClassName, classLoader);
                }
            }
        } catch( Exception ex ) {
            if( debug ) ex.printStackTrace();
        }

        if (fallbackClassName == null) {
            throw new ConfigurationError(
                "Provider for " + factoryId + " cannot be found", null);
        }

        debugPrintln("loaded from fallback value: " + fallbackClassName);
        return newInstance(fallbackClassName, classLoader);
    }

    static class ConfigurationError extends Error {
        // fixme: should this be refactored to use the jdk1.4 exception
        // wrapping?

        private Exception exception;

        /**
         * Construct a new instance with the specified detail string and
         * exception.
         *
         * @param msg   the Message for this error
         * @param x     an Exception that caused this failure, or null
         */
        ConfigurationError(String msg, Exception x) {
            super(msg);
            this.exception = x;
        }

        Exception getException() {
            return exception;
        }
    }
}

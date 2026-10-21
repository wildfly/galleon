/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;

/**
*
* @author Alexey Loubyansky
*/
public class PropertyUtils {

   private PropertyUtils() {
   }

   public static boolean isWindows() {
       return getSystemProperty("os.name").toLowerCase(Locale.ENGLISH).indexOf("windows") >= 0;
   }

   public static String getSystemProperty(final String name) {
       assert name != null : "name is null";
       final SecurityManager sm = System.getSecurityManager();
       if(sm != null) {
           return AccessController.doPrivileged(new PrivilegedAction<String>(){
               public String run() {
                   return System.getProperty(name);
               }});
       } else {
           return System.getProperty(name);
       }
   }
}

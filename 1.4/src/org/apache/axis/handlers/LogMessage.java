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

package org.apache.axis.handlers;

import org.apache.axis.MessageContext;
import org.apache.axis.components.logger.LogFactory;
import org.apache.commons.logging.Log;


/** This handler simply prints a custom message to the debug log.
 *
 * @author Glen Daniels (gdaniels@apache.org)
 */
public class LogMessage extends BasicHandler
{
    protected static Log log =
        LogFactory.getLog(LogMessage.class.getName());

    public void invoke(MessageContext context)
    {
        String msg = (String)getOption("message");
        if (msg != null)
            log.info(msg);
    }
}

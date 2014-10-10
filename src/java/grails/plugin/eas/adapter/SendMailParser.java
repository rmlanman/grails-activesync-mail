/* Copyright (C) 2010 The Android Open Source Project.
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

/*
 * Modifications to original: modified package name
 * 
 */
package grails.plugin.eas.adapter;

import java.io.IOException;
import java.io.InputStream;

public class SendMailParser extends Parser{    
    private int mStatus;

    public SendMailParser(InputStream in) throws IOException {
        super(in);
    }

    public int getStatus() {
        return mStatus;
    }

    /**
     * The only useful info in the SendMail response is the status; we capture and save it
     */
    @Override
    public boolean parse() throws IOException {    	
        if (nextTag(START_DOCUMENT) != Tags.COMPOSE_SEND_MAIL) {
            throw new IOException();
        }
        while (nextTag(START_DOCUMENT) != END_DOCUMENT) {        	     
        	if (tag == Tags.COMPOSE_STATUS) {
                mStatus = getValueInt();
            } else {
                skipTag();
            }            
        }
        return true;
    }
}

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
 * Modifications to original: modified package name and to remove any response parsing code that is not 
 * needed for this application
 * 
 */

package grails.plugin.eas.adapter;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

/**
* Parse the result of the Provision command
*/
public class ProvisionParser extends Parser {
	
	private static final Logger log = Logger.getLogger(ProvisionParser.class); 
	
   private String mSecuritySyncKey = null;
   private int mStatus;
   private boolean mIsSupportable = true;

   public ProvisionParser(final InputStream in) throws IOException {
       super(in);
   }
   
   public String getSecuritySyncKey() {
       return mSecuritySyncKey;
   }

   public void setSecuritySyncKey(String securitySyncKey) {
       mSecuritySyncKey = securitySyncKey;
   }
   
   public int getStatus() {
	   return mStatus;
   }

   public void setStatus(int status) {
	   this.mStatus = status;
   }

private void parsePolicy() throws IOException {       
       while (nextTag(Tags.PROVISION_POLICY) != END) {
           switch (tag) {
               case Tags.PROVISION_POLICY_KEY:
                   mSecuritySyncKey = getValue();
                   break;
               case Tags.PROVISION_STATUS:
                   log.debug("Policy status: " + getValue());
                   break;
               default:
                   skipTag();
           }
       }
   }

   private void parsePolicies() throws IOException {
       while (nextTag(Tags.PROVISION_POLICIES) != END) {
           if (tag == Tags.PROVISION_POLICY) {
               parsePolicy();
           } else {
               skipTag();
           }
       }
   }

   @Override
   public boolean parse() throws IOException {
       boolean res = false;
       if (nextTag(START_DOCUMENT) != Tags.PROVISION_PROVISION) {
           throw new IOException();
       }
       while (nextTag(START_DOCUMENT) != END_DOCUMENT) {    	   
           switch (tag) {
               case Tags.PROVISION_POLICIES:
                   parsePolicies();
                   res = true;
                   break;
               case Tags.PROVISION_STATUS:
            	   setStatus(getValueInt());
            	   break;
               default:
                   skipTag();
           }
       }
       return res;
   }
}


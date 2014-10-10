/*
 * Copyright (C) 2011 The Android Open Source Project
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
 * Modifications to original: extracted to top-level class, changed package name, and greatly reduced the number of statuses 
 * 
 */
package grails.plugin.eas;

public class CommandStatus {
	
    public static final int NEEDS_PROVISIONING_WIPE = 140;
    public static final int NEEDS_PROVISIONING = 142;
    public static final int NEEDS_PROVISIONING_REFRESH = 143;
    public static final int NEEDS_PROVISIONING_INVALID = 144;
    
    // String version of error status codes (for logging only)
    private static final int STATUS_TEXT_START = 101;
    private static final int STATUS_TEXT_END = 150;
    private static final String[] STATUS_TEXT = {
        "InvalidContent", "InvalidWBXML", "InvalidXML", "InvalidDateTime", "InvalidIDCombo",
        "InvalidIDs", "InvalidMIME", "DeviceIdError", "DeviceTypeError", "ServerError",
        "ServerErrorRetry", "ADAccessDenied", "Quota", "ServerOffline", "SendQuota",
        "RecipientUnresolved", "ReplyNotAllowed", "SentPreviously", "NoRecipient", "SendFailed",
        "ReplyFailed", "AttsTooLarge", "NoMailbox", "CantBeAnonymous", "UserNotFound",
        "UserDisabled", "NewMailbox", "LegacyMailbox", "DeviceBlocked", "AccessDenied",
        "AcctDisabled", "SyncStateNF", "SyncStateLocked", "SyncStateCorrupt", "SyncStateExists",
        "SyncStateInvalid", "BadCommand", "BadVersion", "NotFullyProvisionable", "RemoteWipe",
        "LegacyDevice", "NotProvisioned", "PolicyRefresh", "BadPolicyKey", "ExternallyManaged",
        "NoRecurrence", "UnexpectedClass", "RemoteHasNoSSL", "InvalidRequest", "ItemNotFound"
    };

    
    public static boolean isNeedsProvisioning(int status) {
        return (status == CommandStatus.NEEDS_PROVISIONING ||
                status == CommandStatus.NEEDS_PROVISIONING_REFRESH ||
                status == CommandStatus.NEEDS_PROVISIONING_INVALID ||
                status == CommandStatus.NEEDS_PROVISIONING_WIPE);
    }
    
    public static String toString(int status) {
        StringBuilder sb = new StringBuilder();
        sb.append(status);
        sb.append(" (");
        if (status < STATUS_TEXT_START || status > STATUS_TEXT_END) {
            sb.append("unknown");
        } else {
            int offset = status - STATUS_TEXT_START;
            if (offset <= STATUS_TEXT.length) {
                sb.append(STATUS_TEXT[offset]);
            }
        }
        sb.append(")");
        return sb.toString();
    }

}

/*
 * Copyright (C) 2008-2009 Marc Blank
 * Licensed to The Android Open Source Project.
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
 * Modifications to original: extracted to top-level class, changed package name, and removed and Android-specific dependencies
 * 
 */
package grails.plugin.eas;

import grails.plugin.eas.adapter.Serializer;
import grails.plugin.eas.adapter.Tags;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.InputStreamEntity;

public class SendMailEntity extends InputStreamEntity {    
    private final InputStream inputStream;
    private final long mimeLength;        

    private static final int[] MODE_TAGS =  new int[] {Tags.COMPOSE_SEND_MAIL,
        Tags.COMPOSE_SMART_REPLY, Tags.COMPOSE_SMART_FORWARD};

    public SendMailEntity(InputStream instream, long length) {
        super(instream, length);        
        inputStream = instream;
        mimeLength = length;        
    }

    /**
     * We always return -1 because we don't know the actual length of the POST data (this
     * causes HttpClient to send the data in "chunked" mode)
     */
    @Override
    public long getContentLength() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            // Calculate the overhead for the WBXML data
            writeTo(baos, false);
            // Return the actual size that will be sent
            return baos.size() + mimeLength;
        } catch (IOException e) {
            // Just return -1 (unknown)
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                // Ignore
            }
        }
        return -1;
    }

    @Override
    public void writeTo(OutputStream outstream) throws IOException {
        writeTo(outstream, true);
    }

    /**
     * Write the message to the output stream
     * @param outstream the output stream to write
     * @param withData whether or not the actual data is to be written; true when sending
     *   mail; false when calculating size only
     * @throws IOException
     */
    public void writeTo(OutputStream outstream, boolean withData) throws IOException {
        // Not sure if this is possible; the check is taken from the superclass
        if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }

        // We'll serialize directly into the output stream
        Serializer s = new Serializer(outstream);
        // Send the appropriate initial tag
        s.start(Tags.COMPOSE_SEND_MAIL);
        // The Message-Id for this message (note that we cannot use the messageId stored in
        // the message, as EAS 14 limits the length to 40 chars and we use 70+)
        s.data(Tags.COMPOSE_CLIENT_ID, "SendMail-" + System.nanoTime());
        // We always save sent mail
        s.tag(Tags.COMPOSE_SAVE_IN_SENT_ITEMS);

        // Start the MIME tag; this is followed by "opaque" data (byte array)
        s.start(Tags.COMPOSE_MIME);
        // Send opaque data from the file stream
        if (withData) {
            s.opaque(inputStream, (int)mimeLength);
        } else {
            s.opaqueWithoutData((int)mimeLength);
        }
        // And we're done
        s.end().end().done();
    }
}

package grails.plugin.eas;

import grails.plugin.eas.adapter.ProvisionParser;
import grails.plugin.eas.adapter.SendMailParser;
import grails.plugin.eas.adapter.Serializer;
import grails.plugin.eas.adapter.Tags;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;

public class EasMailClient {

	private static final String EAS_PROTOCOL_VERSION = "14.1";
	private static final String WBXML_CONTENT_TYPE = "application/vnd.ms-sync.wbxml";
	private static final int CONNECTION_TIMEOUT = 30000;
	private static final int SO_TIMEOUT = 30000;
	private static final String SEND_EMAIL_CMD = "SendMail";	
	private static final int MAX_PROVISION_RETRIES = 2;	
	
	private static final String POLICY_TYPE = "MS-EAS-Provisioning-WBXML";
	
	private static final Logger log = Logger.getLogger(EasMailClient.class);

	private HttpClient httpClient;
	private String baseUriString;
	private String authString;
	private String accountKey;
	private String username;

	public EasMailClient(final String easServerUrl, final String domain, final String username,
			final String password) {
		this.baseUriString = easServerUrl + "/Microsoft-Server-ActiveSync";
		this.username = username;
		this.authString = buildAuthString(domain, username, password);

		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, SO_TIMEOUT);
		HttpConnectionParams.setSocketBufferSize(params, 8192);
		httpClient = new DefaultHttpClient(getClientConnectionManager(), params);
	}

	public void sendEmail(final MimeMessage message) throws IOException, MessagingException {
		sendEmail(message, 0);
	}

	private void sendEmail(final MimeMessage message, int retry)
			throws IOException, MessagingException {
		
		if (retry > MAX_PROVISION_RETRIES) {
			throw new IOException("Provisioning has failed at least "
					+ MAX_PROVISION_RETRIES + " times. Giving up");
		}		
		
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		message.writeTo(baos);
		
		final byte[] mimeBody = baos.toByteArray();
		
		final HttpResponse resp = sendHttpPostRequest(SEND_EMAIL_CMD, 
				new SendMailEntity(new ByteArrayInputStream(mimeBody), mimeBody.length));
		
		final Header contentLengthHeader = resp.getFirstHeader("Content-Type");
		final int contentLength = contentLengthHeader == null ? 0 : Integer.parseInt(contentLengthHeader.getValue());
		final int code = resp.getStatusLine().getStatusCode(); 

		if (code == HttpStatus.SC_OK) {
			if (contentLength > 0) {
				SendMailParser parser = new SendMailParser(resp.getEntity()
						.getContent());
				if (parser.parse()) {				
					if (CommandStatus.isNeedsProvisioning(parser.getStatus())) {
						tryProvision();
						sendEmail(message, retry + 1);
					}			
				} else {
					log.error("Unexpected response from SendMail request");
				}
			} else {
				log.debug(String.format("Successfully sent email"));
			}
		}
	}

	private String makeUriString(String cmd) {
		StringBuilder sb = new StringBuilder(baseUriString)
			.append("?Cmd=")
			.append(cmd)
			.append("&User=")
			.append(username)
			.append("&DeviceId=141Device")
			.append("&DeviceType=SmartPhone");
		return sb.toString();
	}

	private void setHeaders(HttpRequestBase method) {
		method.setHeader("Authorization", authString);
		method.setHeader("MS-ASProtocolVersion", EAS_PROTOCOL_VERSION);
		method.setHeader("Content-Type", WBXML_CONTENT_TYPE);

		String key = "0";
		if (accountKey != null) {			
			key = accountKey;			
		}
		method.setHeader("X-MS-PolicyKey", key);
	}

	private ClientConnectionManager getClientConnectionManager() {
		PoolingClientConnectionManager clientConnectionManager = new PoolingClientConnectionManager();
		clientConnectionManager.setMaxTotal(25);
		clientConnectionManager.setDefaultMaxPerRoute(10);
		return clientConnectionManager;
	}

	private String buildAuthString(final String domain, final String username, final String password) {
		final StringBuilder plainAuthString = new StringBuilder(domain)
			.append("\\")
			.append(username)
			.append(":")
			.append(password);
					
		final StringBuilder encodedAuthString = new StringBuilder("Basic ")
				.append(Base64.encodeBase64String(plainAuthString.toString().getBytes()));

		return encodedAuthString.toString();
	}
	
	private void tryProvision() throws IOException {
        Serializer s = new Serializer();        
        s.start(Tags.PROVISION_PROVISION);
        addDeviceInformation(s);
        s.start(Tags.PROVISION_POLICIES);
        s.start(Tags.PROVISION_POLICY);
        s.data(Tags.PROVISION_POLICY_TYPE, POLICY_TYPE);
        s.end().end().end().done(); // PROVISION_POLICY, PROVISION_POLICIES, PROVISION_PROVISION
        
        final byte[] wbxmlBytes = s.toByteArray();               
        final HttpResponse resp = sendHttpPostRequest("Provision", 
        		new InputStreamEntity(new ByteArrayInputStream(wbxmlBytes), wbxmlBytes.length));
        
        try {
            int code = resp.getStatusLine().getStatusCode();
            if (code == HttpStatus.SC_OK) {
                InputStream is = resp.getEntity().getContent();
                ProvisionParser pp = new ProvisionParser(is);
                if (pp.parse()) {                	
                    acknowledgeProvision(pp.getSecuritySyncKey());
                } else {
                	log.warn("Provision status: " + CommandStatus.toString(pp.getStatus()));
                }
            }
        } finally {
            resp.getEntity().getContent().close();
        }
    }
	
	private void addDeviceInformation (Serializer s) throws IOException {
		s.start(Tags.SETTINGS_DEVICE_INFORMATION);
		s.start(Tags.SETTINGS_SET);
		s.data(Tags.SETTINGS_MODEL,"Grails Plugin");
		s.end().end();
	}
	
	private void acknowledgeProvision(String securitySyncKey) throws IOException {
		log.debug("Acknowledging provision key...");
		accountKey = securitySyncKey;
        
        Serializer s = new Serializer();        
        s.start(Tags.PROVISION_PROVISION);
        s.start(Tags.PROVISION_POLICIES);
        s.start(Tags.PROVISION_POLICY);
        s.data(Tags.PROVISION_POLICY_TYPE, POLICY_TYPE);
        s.data(Tags.PROVISION_POLICY_KEY, securitySyncKey);
        s.data(Tags.PROVISION_STATUS, "1");
        s.end().end().end().done();
        
        final byte[] wbxmlBytes = s.toByteArray();               
        final HttpResponse resp = sendHttpPostRequest("Provision", 
        		new InputStreamEntity(new ByteArrayInputStream(wbxmlBytes), wbxmlBytes.length));
        
        try {
            int code = resp.getStatusLine().getStatusCode();
            if (code == HttpStatus.SC_OK) {
                InputStream is = resp.getEntity().getContent();
                ProvisionParser pp = new ProvisionParser(is);
                if (pp.parse()) {                	
                    accountKey = pp.getSecuritySyncKey();
                } else {
                	log.warn("Provision Acknowledge status: " + CommandStatus.toString(pp.getStatus()));
                }
            }
        } finally {
            resp.getEntity().getContent().close();
        }
	}
	
	private HttpResponse sendHttpPostRequest(final String command, final HttpEntity entity) throws IOException {
		final HttpPost httpPost = new HttpPost(makeUriString(command));
		httpPost.setEntity(entity);
		setHeaders(httpPost);
		return httpClient.execute(httpPost);
	}
}

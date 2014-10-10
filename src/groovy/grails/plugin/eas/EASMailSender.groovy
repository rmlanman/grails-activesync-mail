package grails.plugin.eas

import javax.mail.internet.MimeMessage;

import org.springframework.mail.MailException
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

class EASMailSender extends JavaMailSenderImpl {
	
	def String easServer
	def String domain
	def String username
	def String password
	
	private EasMailClient mailClient;
	
	@Override
	protected void doSend(MimeMessage[] mimeMessages, Object[] originalMessages) throws MailException {
		if (!mailClient) {
			initClient()
		}
		
		mimeMessages.each { m ->
			mailClient.sendEmail(m)
		}
	}
	
	private void initClient () {
		mailClient = new EasMailClient(easServer, domain, username, password)
	}
}

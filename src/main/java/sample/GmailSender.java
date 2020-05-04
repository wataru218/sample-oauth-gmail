package sample;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.Profile;

@Component
public class GmailSender extends JavaMailSenderImpl {

	@Override
	protected void doSend(MimeMessage[] mimeMessages, Object[] originalMessages) throws MailException {
		try {
			for (MimeMessage mimeMessage : mimeMessages) {
				Message message = createMessageWithEmail(mimeMessage);
				send(message);
			}
		} catch (IOException | MessagingException e) {
			throw new MailSendException("Error", e);
		}
	}

	protected Message createMessageWithEmail(MimeMessage emailContent) throws IOException, MessagingException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		emailContent.writeTo(buffer);
		byte[] bytes = buffer.toByteArray();
		String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
		Message message = new Message();
		message.setRaw(encodedEmail);
		return message;
	}

	protected void send(Message content) throws IOException, MessagingException {
		Gmail client = GoogleApiUtils.getGmailClient(Application.NAME);
		Profile profile = client.users().getProfile("me").execute();
		String userId = profile.getEmailAddress();
		client.users().messages().send(userId, content).execute();
	}
}

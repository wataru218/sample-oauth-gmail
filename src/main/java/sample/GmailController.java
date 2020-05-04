package sample;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.api.client.auth.oauth2.Credential;

@Controller
public class GmailController {

	private final GmailSender gmailSender;

	@Value("${callbackUrl:http://localhost:8080/callback}")
	private String callbackUrl;

	@Value("${toAddress}")
	private String toAddress;

	public GmailController(GmailSender gmailSender) {
		this.gmailSender = gmailSender;
	}

	@GetMapping("/sendmail")
	public String sendmail(Model model) {
		System.out.println("/sendmail");
		try {
			// Google OAutn
			Credential credential = GoogleApiUtils.loadCredential();
			if (credential == null) {
				return "redirect:" + GoogleApiUtils.newAuthorizationUrl(callbackUrl);
			}
			// Send Gmail
			MimeMessage email = createEmail(toAddress, "TEST", "Hello, world!");
			gmailSender.send(email);

			model.addAttribute("message", "sent!");
			return "gmail";
		} catch (Exception e) {
			e.printStackTrace();
		}
		model.addAttribute("message", "error!");
		return "gmail";
	}

	@GetMapping("/callback")
	public String callback(@RequestParam String code) throws Exception {
		System.out.println("/callback");
		Credential credential = GoogleApiUtils.createAndStoreCredential(code, callbackUrl);
		if (credential == null) {
			return "error";
		}
		return "redirect:/sendmail";
	}

	private MimeMessage createEmail(
			String to,
			String subject,
			String bodyText)
			throws MessagingException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		MimeMessage email = new MimeMessage(session);
		email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
		email.setSubject(subject);
		email.setText(bodyText);
		return email;
	}
}

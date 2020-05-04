package sample;

import static java.nio.charset.StandardCharsets.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

public class GoogleApiUtils {

	private static final String COMMON_USER_ID = "user";
	private static final String CLIENT_ID_JSON = "/client_id.json";
	private static final File CREDENTIALS_DIRECTORY = new File(System.getProperty("user.home"), ".credentials");
	private static final List<String> OAUTH_SCOPES = Collections.singletonList(GmailScopes.GMAIL_COMPOSE);
	private static GoogleAuthorizationCodeFlow FLOW;

	static {
		try {
			FLOW = new GoogleAuthorizationCodeFlow
					.Builder(Utils.getDefaultTransport(), Utils.getDefaultJsonFactory(), loadClientSecrets(), OAUTH_SCOPES)
					.setDataStoreFactory(new FileDataStoreFactory(CREDENTIALS_DIRECTORY))
					.setAccessType("offline")
					.setApprovalPrompt("force")
					.build();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Gmail getGmailClient(String applicationName) throws IOException {
		return new Gmail.Builder(Utils.getDefaultTransport(), Utils.getDefaultJsonFactory(), loadCredential())
				.setApplicationName(applicationName)
				.build();
	}

	public static Credential loadCredential() throws IOException {
		Credential credential = FLOW.loadCredential(COMMON_USER_ID);
		if (credential == null) {
			return null;
		}
		if (credential.getRefreshToken() != null
				|| credential.getExpiresInSeconds() == null
				|| credential.getExpiresInSeconds() > 60) {
			return credential;
		}
		System.out.println("RefreshToken:\t" + credential.getRefreshToken());
		System.out.println("ExpiresInSeconds:\t" + credential.getExpiresInSeconds());
		return null;
	}

	public static String newAuthorizationUrl(String callbackUrl) throws IOException {
		AuthorizationCodeRequestUrl authorizationUrl = FLOW.newAuthorizationUrl().setRedirectUri(callbackUrl);
		String url = authorizationUrl.build();
		return url;
	}

	public static Credential createAndStoreCredential(String code, String callbackUrl) throws IOException {
		TokenResponse tokenResponse = FLOW.newTokenRequest(code).setRedirectUri(callbackUrl).execute();
		System.out.println("Response AccessToken:\t" + tokenResponse.getAccessToken());
		System.out.println("Response ExpiresInSeconds:\t" + tokenResponse.getExpiresInSeconds());
		System.out.println("Response RefreshToken:\t" + tokenResponse.getRefreshToken());
		Credential credential = FLOW.createAndStoreCredential(tokenResponse, COMMON_USER_ID);
		return credential;
	}

	private static GoogleClientSecrets loadClientSecrets() throws IOException {
		String resourceName = CLIENT_ID_JSON;
		InputStream in = GoogleApiUtils.class.getResourceAsStream(resourceName);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + resourceName);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets
				.load(Utils.getDefaultJsonFactory(), new InputStreamReader(in, UTF_8));
		return clientSecrets;
	}
}

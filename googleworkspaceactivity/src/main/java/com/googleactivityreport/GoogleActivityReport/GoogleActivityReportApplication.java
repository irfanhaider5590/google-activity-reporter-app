package com.googleactivityreport.GoogleActivityReport;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.reports.Reports;
import com.google.api.services.reports.ReportsScopes;
import com.google.api.services.reports.model.Activities;
import com.google.api.services.reports.model.Activity;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
public class GoogleActivityReportApplication {
	/** Application name. */
	private static final String APPLICATION_NAME = "googleApi";
	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	/** Directory to store authorization tokens for this application. */
	private static final String TOKENS_DIRECTORY_PATH = "tokens";

	/**
	 * Global instance of the scopes required by this quickstart.
	 * If modifying these scopes, delete your previously saved tokens/ folder.
	 */
	private static final List<String> SCOPES = Collections.singletonList(ReportsScopes.ADMIN_REPORTS_AUDIT_READONLY);
	private static final String CREDENTIALS_FILE_PATH = "/ih.json";

	/**
	 * Creates an authorized Credential object.
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException If the credentials.json file cannot be found.
	 */
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.
		InputStream in = GoogleActivityReportApplication.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
				.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
				.setAccessType("offline")
				.build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	public static void main(String... args) throws IOException, GeneralSecurityException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Reports service = new Reports.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME)
				.build();

		// Print the last 10 login events.
		String userKey = "all"; //user based report generation: when it is all , it generated all users report
		//String applicationName = "login";
		String applicationName = "groups"; //can specify different applicationName for report generation like login,admin etc
		try {
			Activities result = service.activities().list(userKey, applicationName)
					.setMaxResults(10)
					.execute();
			List<Activity> activities = result.getItems();
			if (activities == null || activities.size() == 0) {
				System.out.println("No Reports found.");
			} else {
				System.out.println("Admin:");
				for (Activity activity : activities) {
					System.out.printf("%s: %s (%s)\n",
							activity.getId().getTime(),
							activity.getActor().getEmail(),
							activity.getEvents().get(0).getName());
				}
			}
		}catch (GoogleJsonResponseException ge){
			ge.printStackTrace();
		}
	}

}

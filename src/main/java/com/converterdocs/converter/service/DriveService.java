package com.converterdocs.converter.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DriveService {

    private static final String APPLICATION_NAME = "Google Drive API Java";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FOLDER = "static/credentials"; // Directory to store user credentials.
    private static final String CLIENT_ID = "460728362097-petn6avmu12r6p3bo7lvoedb6et8bv6i.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "1GtgICUB35ud7h5lShXIR-X1";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved credentials/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    //    private static final String CLIENT_SECRET_DIR = "client_secret.json";
    private static final String CLIENT_SECRET_DIR = "Scip 84848-78bb6a229fa6.json";

    public InputStream getStreamResponse(String fileId) throws GeneralSecurityException, IOException {
        final InputStream streamFromGoogle = getFileFromGoogle(fileId);
        return convertToJpg(streamFromGoogle);
    }

    private InputStream convertToJpg(InputStream streamFromGoogle) throws IOException {
        System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
        PDDocument document = PDDocument.load(streamFromGoogle);
        PDFRenderer pdfRenderer = new PDFRenderer(document);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        for (int page = 0; page < document.getNumberOfPages(); ++page) {
            BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
            zipOutputStream.putNextEntry(new ZipEntry(page + ".jpeg"));
            ImageIOUtil.writeImage(bim, "JPEG", zipOutputStream);
            zipOutputStream.closeEntry();
        }
        document.close();
        zipOutputStream.close();
        byteArrayOutputStream.close();
        return convertOutToIn(byteArrayOutputStream);
    }

    private InputStream convertOutToIn(ByteArrayOutputStream byteArrayOutputStream) {
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    private InputStream getFileFromGoogle(String fileId) throws GeneralSecurityException, IOException {
        final Drive drive = getDrive();
        return drive.files().export(fileId, "application/pdf").executeMediaAsInputStream();
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param httpTransport The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If there is no client_secret.
     */
    private Credential getCredentials(final NetHttpTransport httpTransport) throws IOException {
//        // Load client secrets.
//
//        InputStream in = new ClassPathResource(CLIENT_SECRET_DIR).getInputStream();
//        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
//
////         Build flow and trigger user authorization request.
//        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
//                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
//                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(CREDENTIALS_FOLDER)))
//                .setAccessType("offline")
//                .build();
//
//
//        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        InputStream in = new ClassPathResource(CLIENT_SECRET_DIR).getInputStream();
        return GoogleCredential.fromStream(in).createScoped(SCOPES);

    }

    private Drive getDrive() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        final Credential credentials = getCredentials(httpTransport);
        return new Drive.Builder(httpTransport, JSON_FACTORY, credentials)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}


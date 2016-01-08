import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Preconditions;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.Collections;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public final class StorageSample {

  private static final String STORAGE_SCOPE =
      "https://www.googleapis.com/auth/devstorage.read_write";

  public static String listBucket(final String bucketName)
      throws IOException, GeneralSecurityException {
    URL.setURLStreamHandlerFactory(new OkUrlFactory(new OkHttpClient()));

    GoogleCredential credential = GoogleCredential.getApplicationDefault()
        .createScoped(Collections.singleton(STORAGE_SCOPE));

    String uri = "https://storage.googleapis.com/"
        + URLEncoder.encode(bucketName, "UTF-8");

    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    HttpRequestFactory requestFactory = httpTransport.createRequestFactory(
        credential);
    GenericUrl url = new GenericUrl(uri);

    HttpRequest request = requestFactory.buildGetRequest(url);
    HttpResponse response = request.execute();
    String content = response.parseAsString();

    return content;
  }

  private static void prettyPrintXml(
      final String bucketName, final String content) {
    // Instantiate transformer input.
    Source xmlInput = new StreamSource(new StringReader(content));
    StreamResult xmlOutput = new StreamResult(new StringWriter());

    // Configure transformer.
    try {
      Transformer transformer = TransformerFactory.newInstance()
          .newTransformer(); // An identity transformer
      transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "testing.dtd");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(
          "{http://xml.apache.org/xslt}indent-amount", "2");
      transformer.transform(xmlInput, xmlOutput);

      // Pretty print the output XML.
      System.out.println("\nBucket listing for " + bucketName + ":\n");
      System.out.println(xmlOutput.getWriter().toString());
    } catch (TransformerException e) {
      e.printStackTrace();
    }
  }

  public static void main(final String[] args) {
    try {
      // Check for valid setup.
      Preconditions.checkArgument(
          args.length == 1,
          "Please pass in the Google Cloud Storage bucket name to display");
      String bucketName = args[0];

      String content = listBucket(bucketName);

      prettyPrintXml(bucketName, content);
      System.exit(0);

    } catch (IOException e) {
      System.err.println(e.getMessage());
    } catch (Throwable t) {
      t.printStackTrace();
    }
    System.exit(1);
  }
}

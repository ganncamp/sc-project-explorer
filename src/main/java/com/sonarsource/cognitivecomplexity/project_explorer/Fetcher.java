package com.sonarsource.cognitivecomplexity.project_explorer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


public class Fetcher {

  private static final String BASE_URL = "https://sonarcloud.io/api/";


  public static JSONArray fetchPaginatedResults(String urlEncodedSearch, String field) {

    final long maxResults = 10_000;

    String search = urlEncodedSearch;
    if (urlEncodedSearch.indexOf('?') < 0) {
      search = urlEncodedSearch + '?';
    }
    search = search + "&ps=";

    JSONArray results = new JSONArray();
    long retrieved = 0;
    long expected = 1;
    int pageNumber = 1;
    long pageSize = 500;
    JSONObject sr;

    while (retrieved < expected && (sr = fetchResultPage(pageNumber++, search + pageSize)) != null ) {

      results.addAll((JSONArray) sr.get(field));

      if (sr.containsKey("paging")) {
        sr = ((JSONObject) sr.get("paging"));
      }
      expected = (long) sr.get("total");
      expected = expected > maxResults? maxResults : expected;

      retrieved = results.size();

      if (maxResults - retrieved < 500) {
        pageSize = maxResults - retrieved;
      }

      if (expected < maxResults && retrieved +1 == expected) {
        // there seems to be a bug in components/search_projects: 1 result missing from page 2.
        break;
      }
    }
    return results;
  }

  public static JSONObject fetchResultPage(int pageNumber, String urlEncodedSearch) {

    return Fetcher.getJsonFromUrl(BASE_URL
            + urlEncodedSearch
            + "&p="
            + pageNumber);
  }

//  public static int getLength(String s) {
//    return s.length();
//  }


  private static JSONObject getJsonFromUrl(String url) {

    Client client = getClient();

    WebTarget webResource = client.target(url);

    Response response = webResource.request().accept(MediaType.APPLICATION_JSON).get(Response.class);

    checkStatus(url, client, response);

    String responseStr = response.readEntity(String.class);
    response.close();
    client.close();

    JSONParser parser = new JSONParser();
    try {
      return (JSONObject)parser.parse(responseStr);
    } catch (ParseException e) {
      throw new ExplorerException(e);
    }
  }

  protected static void checkStatus(String url, Client client, Response response) {

    int status = response.getStatus();
    if (status < 200 || status > 299) {
      response.close();
      client.close();
      throw new ExplorerException("Failed : HTTP error code: "
              + response.getStatus() + " for " + url);
    }
  }

  protected static Client getClient() {
    try {
      System.setProperty("jsse.enableSNIExtension", "false");
      SSLContext sslcontext = SSLContext.getInstance( "TLS" );
      sslcontext.init(null, new TrustManager[]{new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
          // This is useless for Nemo usage
        }
        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
          // No verification for the time being there, however we should check the server certificate
        }
        @Override
        public X509Certificate[] getAcceptedIssuers() {
          return new X509Certificate[0];
        }

      }}, new java.security.SecureRandom());

      return ClientBuilder.newBuilder().sslContext(sslcontext)
              .hostnameVerifier((s1, s2) -> s1.equalsIgnoreCase(s2.getPeerHost()))
              .build();

    } catch (KeyManagementException | NoSuchAlgorithmException e) {
      throw new ExplorerException(e);
    }
  }


}
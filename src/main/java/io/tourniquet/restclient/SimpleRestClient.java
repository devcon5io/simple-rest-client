package io.tourniquet.restclient;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A simple client to send requests to a REST API using plain Java SE.
 */
public class SimpleRestClient {

   public static RequestBuilder requestTo(String url) {

      return new RequestBuilder(url);
   }

   public static RequestBuilder requestTo(URL url) {

      return new RequestBuilder(url);
   }

   public static class RequestBuilder {

      private URL url;
      private Map<String, String> headers = new HashMap<>();

      public RequestBuilder(String url) {

         try {
            this.url = new URL(url);
         } catch (MalformedURLException e) {
            throw new RuntimeException(e);
         }
      }

      public RequestBuilder(URL url) {

         this.url = url;
      }

      public RequestBuilder basicAuth(String username, String password) {

         return auth(Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
      }

      public RequestBuilder auth(String authToken) {

         headers.put("Authorization", authToken);
         return this;
      }

      public RequestBuilder acceptJson() {

         return accept("application/json");
      }

      public RequestBuilder accept(String contentType) {

         headers.put("Accept", contentType);
         return this;
      }

      public RequestBuilder sendJson() {

         return contentType("application/json");
      }

      public RequestBuilder contentType(String contentType) {

         headers.put("Content-Type", contentType);
         return this;
      }

      public RequestBuilder addHeader(String name, String value) {

         headers.put(name, value);
         return this;
      }

      public Response get() {

         return buildRequest("GET");
      }

      public Response head() {

         return buildRequest("HEAD");
      }

      public Response delete() {

         return buildRequest("DELETE");
      }

      public Response post(Consumer<OutputStream> dataProvider) {

         return buildRequest("POST", dataProvider);
      }

      public Response put(Consumer<OutputStream> dataProvider) {

         return buildRequest("PUT", dataProvider);
      }

      private Response buildRequest(String method) {

         return buildRequest(method, os -> {
         });
      }

      private Response buildRequest(String method, Consumer<OutputStream> dataProvider) {

         try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method);
            headers.entrySet().forEach(e -> con.setRequestProperty(e.getKey(), e.getValue()));

            if (dataProvider != null && ("POST".equals(method) || "PUT".endsWith(method))) {
               con.setDoOutput(true);
               try (OutputStream os = con.getOutputStream()) {
                  dataProvider.accept(os);
               }
            }

            return new Response(con);
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }
   }

   public static class Response {

      final HttpURLConnection connection;

      public Response(final HttpURLConnection connection) {

         this.connection = connection;
      }

      public int getStatusCode() {

         try {
            return connection.getResponseCode();
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }

      public String getMessage() {

         try {
            return connection.getResponseMessage();
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }

      public InputStream asInputStream() {

         try {
            return connection.getInputStream();
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }

      public String asString() {

         try (InputStream in = connection.getInputStream();
              BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }

      public byte[] asBytes() {

         try (InputStream in = connection.getInputStream();
              ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead = 0;
            while ((bytesRead = in.read(buffer)) != -1) {
               out.write(buffer, 0, bytesRead);
            }
            return out.toByteArray();
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }

   }
}

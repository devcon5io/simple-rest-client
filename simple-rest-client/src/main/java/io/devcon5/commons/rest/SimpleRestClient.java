package io.devcon5.commons.rest;

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
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A simple client to send requests to a REST API using plain Java SE.
 */
public class SimpleRestClient {

   /**
    * Initiates a new request to the specified URL
    * @param url
    *  a string representing an URL. If the url String is not valid, an exception is thrown
    * @return
    *  a new RequestBuilder to specify request parameters
    */
   public static RequestBuilder requestTo(String url) {
      try {
         return new RequestBuilder(new URL(url));
      } catch (MalformedURLException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Initiates a new request to the specified URL
    * @param url
    *  the requested URL
    * @return
    *  a new RequestBuilder to specify request parameters
    */
   public static RequestBuilder requestTo(URL url) {

      return new RequestBuilder(url);
   }

   /**
    * Builder for fluently defining a request
    */
   public static class RequestBuilder {

      private URL url;
      private Map<String, String> headers = new HashMap<>();

      RequestBuilder(URL url) {
         this.url = url;
      }

      /**
       * Specifies basic authentication information to be added to the request
       * @param username
       *  the plain text username
       * @param password
       *  the plaintext password
       * @return
       *  this builder
       */
      public RequestBuilder basicAuth(String username, String password) {

         return auth(Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
      }

      /**
       * Specifies a custom auth token to set as Authorization information on the request
       * @param authToken
       *  the authToken used as 'Authorization' header
       * @return
       *  this builder
       */
      public RequestBuilder auth(String authToken) {

         headers.put("Authorization", authToken);
         return this;
      }

      /**
       * Sets the accept content type to application/json
       * @return
       *  this builder
       */
      public RequestBuilder acceptJson() {

         return accept("application/json");
      }

      /**
       * Sets a custom content type as Accept header
       * @param contentType
       *  the accepted content type(s)
       * @return
       *  this builder
       */
      public RequestBuilder accept(String contentType) {

         headers.put("Accept", contentType);
         return this;
      }

      /**
       * Sets the sent content's type to application/json
       * @return
       * this builder
       */
      public RequestBuilder sendJson() {

         return contentType("application/json");
      }

      /**
       * Sets a custom content type as Content-Type header
       * @param contentType
       *  the content type to be set
       * @return
       *  this builder
       */
      public RequestBuilder contentType(String contentType) {

         headers.put("Content-Type", contentType);
         return this;
      }

      /**
       * Adds a custom header to the request
       * @param name
       *  the name of the header field
       * @param value
       *  the value of the header field
       * @return
       *  this builder
       */
      public RequestBuilder addHeader(String name, String value) {

         headers.put(name, value);
         return this;
      }

      /**
       * Finalizes and sends a GET request
       * @return
       *  the response handle
       */
      public Response get() {

         return buildRequest("GET");
      }

      /**
       * Finalizes and sends a HEAD request
       * @return
       *  the response handle
       */
      public Response head() {

         return buildRequest("HEAD");
      }

      /**
       * Finalizes and sends a DELETE request
       * @return
       *  the response handle
       */
      public Response delete() {

         return buildRequest("DELETE");
      }

      /**
       * Finalizes and sends a POST request
       * @param dataProvider
       *  a data provider that writes to the output stream of the outgoing request
       * @return
       *  the response handle
       */
      public Response post(Consumer<OutputStream> dataProvider) {

         return buildRequest("POST", dataProvider);
      }
      /**
       * Finalizes and sends a PUT request
       * @param dataProvider
       *  a data provider that writes to the output stream of the outgoing request
       * @return
       *  the response handle
       */
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
            headers.forEach(con::setRequestProperty);

            if (dataProvider != null && ("POST".equals(method) || "PUT".equals(method))) {
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

   /**
    * A Response handle around a HttpURLConnection
    */
   public static class Response {

      final HttpURLConnection connection;

      Response(final HttpURLConnection connection) {

         this.connection = connection;
      }

      /**
       * The http response code
       * @return
       */
      public int getStatusCode() {

         try {
            return connection.getResponseCode();
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }

      /**
       * The HTTP repsonse message
       * @return
       */
      public String getMessage() {

         try {
            return connection.getResponseMessage();
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }

      /**
       * Checks if the response code returned by the server is one of the valid response code provided. If the actual code is not valid, an {@link IllegalStateException} is
       * thrown.
       *
       * @param validResponseCodes
       *         list of valid response codes
       */
      public boolean hasValidResponseCode(final int... validResponseCodes) throws IOException {

         final int actualCode = connection.getResponseCode();
         for (int validCode : validResponseCodes) {
            if (validCode == actualCode) {
               return true;
            }
         }
         return false;
      }

      /**
       * Reads the response as InputStream if the response represents is valid (by response code)
       * @param validResponseCodes
       *  the response codes that denote the response as valid
       * @return
       */
      public InputStream asInputStream(final int... validResponseCodes) {
         validateResponseCode(validResponseCodes);
         try {
            return connection.getInputStream();
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }

      /**
       * Reads the response as String if the response represents is valid (by response code)
       * @param validResponseCodes
       *  the response codes that denote the response as valid
       * @return
       */
      public String asString(final int... validResponseCodes) {
         validateResponseCode(validResponseCodes);

         try (InputStream in = connection.getInputStream();
              BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }
      /**
       * Reads the response as byte array if the response represents is valid (by response code)
       * @param validResponseCodes
       *  the response codes that denote the response as valid
       * @return
       */
      public byte[] asBytes(final int... validResponseCodes) {
         validateResponseCode(validResponseCodes);
         try (InputStream in = connection.getInputStream();
              ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final byte[] buffer = new byte[8192];
            int bytesRead = 0;
            while ((bytesRead = in.read(buffer)) != -1) {
               out.write(buffer, 0, bytesRead);
            }
            return out.toByteArray();
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }

      /**
       * Tries to parse the returned content into the specified target type. In order to parse the data, an {@link EntityReader} must be provided via {@link ServiceLoader}
       * that supports the received content-type and data.
       * @param targetType
       *  the target type to which the received data should be marshalled
       * @param <T>
       * @return
       *  an optional instance of the target type. If no entity reader was found, the resulting optional is empty. The content could not be parsed by a provided Entity reader,
       *  an exception is thrown, depending on the used {@link EntityReader}
       */
      public <T> Optional<T> as(Class<T> targetType,final int... validResponseCodes){
         validateResponseCode(validResponseCodes);
         final String contentType = getContentType();
         for(EntityReader er : ServiceLoader.load(EntityReader.class)){
            if(er.supports(targetType, contentType)){
               return Optional.of(er.read(targetType,contentType,asInputStream()));
            }
         }
         return Optional.empty();
      }

      private void validateResponseCode(final int... validResponseCodes) {
         try {
            if (!hasValidResponseCode(validResponseCodes.length == 0 ? new int[]{200, 201, 204} : validResponseCodes)) {
               throw new IllegalStateException("Server returned " + connection.getResponseCode() + " " + connection.getResponseMessage());
            }
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }

      private String getContentType() {
         String contentType = connection.getHeaderField("Content-Type");
         if (contentType == null) {
            contentType = "*/*";
         }
         return contentType;
      }
   }
}

package io.devcon5.commons.rest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class SimpleRestClientTest {

   @Rule
   public WireMockRule wireMockRule = new WireMockRule(Ports.findAvailablePort());
   private String baseAddress;

   @Before
   public void setUp() throws Exception {
      this.baseAddress = "http://localhost:" + wireMockRule.getOptions().portNumber();
   }

   @Test(expected = RuntimeException.class)
   public void requestTo_invalidUrl() {
      SimpleRestClient.requestTo("not_a_url");
   }

   @Test
   public void requestTo_acceptXml_get_asString_returnsString() {
      stubFor(get(urlEqualTo("/my/resource")).withHeader("Accept", equalTo("text/xml"))
                                             .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/xml").withBody("<response>Some content</response>")));

      String result = SimpleRestClient.requestTo(baseAddress + "/my/resource").accept("text/xml").get().asString(200);

      assertEquals("<response>Some content</response>", result);
   }

   @Test
   public void requestTo_acceptXml_get_asInputStream_returnsInputStream() throws IOException {
      stubFor(get(urlEqualTo("/my/resource")).withHeader("Accept", equalTo("text/xml"))
                                             .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/xml").withBody("<response>Some content</response>")));

      try (InputStream result = SimpleRestClient.requestTo(baseAddress + "/my/resource").accept("text/xml").get().asInputStream(200)) {

         StringBuilder builder = new StringBuilder(1024);
         byte[] data = new byte[1024];
         int len;
         while ((len = result.read(data)) != -1) {
            builder.append(new String(data, 0, len, Charset.defaultCharset()));
         }

         assertEquals("<response>Some content</response>", builder.toString());
      }

   }

   @Test
   public void requestTo_acceptXml_get_asBytes_returnsBytes() throws IOException {
      stubFor(get(urlEqualTo("/my/resource")).withHeader("Accept", equalTo("text/xml"))
                                             .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/xml").withBody("<response>Some content</response>")));

      byte[] result = SimpleRestClient.requestTo(baseAddress + "/my/resource").accept("text/xml").get().asBytes(200);

      assertArrayEquals("<response>Some content</response>".getBytes(), result);

   }

   @Test
   public void requestTo_head_returnsOK() throws IOException {
      stubFor(head(urlEqualTo("/my/resource")).willReturn(aResponse().withStatus(200)));

      int status = SimpleRestClient.requestTo(baseAddress + "/my/resource").head().getStatusCode();

      assertEquals(200, status);
   }

   @Test
   public void requestTo_delete_returnsOK() throws IOException {
      stubFor(delete(urlEqualTo("/my/resource")).willReturn(aResponse().withStatus(204)));

      int status = SimpleRestClient.requestTo(baseAddress + "/my/resource").delete().getStatusCode();

      assertEquals(204, status);
   }

   @Test
   public void requestTo_post_returnsOK() throws IOException {
      stubFor(post(urlEqualTo("/my/resource")).willReturn(aResponse().withStatus(200).withBody("created")));

      String result = SimpleRestClient.requestTo(baseAddress + "/my/resource").contentType("text/plain").post(os -> os.write("test".getBytes())).asString();

      assertEquals("created", result);
      verify(postRequestedFor(urlEqualTo("/my/resource")).withRequestBody(equalTo("test")).withHeader("Content-Type", equalTo("text/plain")));
   }

   @Test
   public void requestTo_put_returnsOK() throws IOException {
      stubFor(put(urlEqualTo("/my/resource")).willReturn(aResponse().withStatus(200).withBody("updated")));

      String result = SimpleRestClient.requestTo(baseAddress + "/my/resource").contentType("text/plain").put(os -> os.write("test".getBytes())).asString();

      assertEquals("updated", result);
      verify(putRequestedFor(urlEqualTo("/my/resource")).withRequestBody(equalTo("test")).withHeader("Content-Type", equalTo("text/plain")));
   }
}

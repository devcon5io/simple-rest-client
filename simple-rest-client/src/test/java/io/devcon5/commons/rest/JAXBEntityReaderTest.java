package io.devcon5.commons.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;

import org.junit.Test;

public class JAXBEntityReaderTest {

   /**
    * The class under test
    */
   private JAXBEntityReader subject = new JAXBEntityReader();

   @Test
   public void supports_applicationXML() {
      assertTrue(subject.supports(Object.class, "application/xml"));
   }

   @Test
   public void supports_textXML() {
      assertTrue(subject.supports(Object.class, "text/xml"));
   }

   @Test
   public void supports_applicationXMLPlus() {
      assertTrue(subject.supports(Object.class, "application/xml+other"));
   }
   @Test
   public void supports_textXMLPlus() {
      assertTrue(subject.supports(Object.class, "text/xml+Other"));
   }

   @Test
   public void doesnot_supports_applicationJson() {
      assertFalse(subject.supports(Object.class, "application/json"));
   }

   @Test
   public void read_entity_fromBytes() {

      CustomEntity e = subject.read(CustomEntity.class, "application/xml", "<CustomEntity><body>text</body><".getBytes());

      assertEquals("text", e.getBody());
   }

   @Test
   public void read_entity_fromStream() {


      CustomEntity e = subject.read(CustomEntity.class, "application/xml", new ByteArrayInputStream("<CustomEntity><body>text</body></CustomEntity>".getBytes()));

      assertEquals("text", e.getBody());

   }

   public static class CustomEntity{
      private String body;

      public String getBody() {
         return body;
      }

      public void setBody(final String body) {
         this.body = body;
      }
   }
}

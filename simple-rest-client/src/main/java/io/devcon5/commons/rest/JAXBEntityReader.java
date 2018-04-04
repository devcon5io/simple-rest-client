package io.devcon5.commons.rest;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

public class JAXBEntityReader implements EntityReader {

   @Override
   public boolean supports(final Class<?> targetType, final String contentType) {
      return contentType.matches(".+\\/xml(\\+[a-zA-Z0-9]+)?");
   }

   @Override
   public <T> T read(final Class<T> targetType, final String contentType, final InputStream data) {

      try {
         final JAXBContext context = JAXBContext.newInstance(targetType);
         final Unmarshaller um = context.createUnmarshaller();
         final JAXBElement element = um.unmarshal(new StreamSource(data), targetType);
         return (T) element.getValue();
      } catch (JAXBException e) {
         throw new RuntimeException(e);
      }
   }
}

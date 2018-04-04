package io.devcon5.commons.rest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public interface EntityReader {

   /**
    * Checks if the implementor is capable of producing instances of the target type using data of the specified contentType.
    * @param targetType
    *  the requested target type
    * @param contentType
    *  the content type as media-type string (i.e. application/json)
    * @return
    *  true if the reader can produce entities of the target type with data of the content type
    */
   boolean supports(Class<?> targetType, String contentType);

   /**
    * Reads the input stream which is of the specified content type and produces an instance of the target type
    * @param targetType
    *  the type to produce
    * @param contentType
    *  the content type / mimetype of the data input stream
    * @param data
    *  the data to read
    * @param <T>
    * @return
    *  an instance of the target type. The implementor must not return null. If the content can not be parsed, an Exception has to be thrown.
    */
   <T> T read(Class<T> targetType, String contentType, InputStream data);

   default <T> T read(Class<T> targetType, String contentType, byte[] data){
      return read(targetType, contentType, new ByteArrayInputStream(data));
   }
}

# simple-rest-client
A simple fluent API to write calls to a REST API service without relying on additional libraries.
It doesn't need or use Jax-RS client as it's using Java's built-in HttpUrlConnection.
 
It's intended purpose is to write simple one-line requests for testing, i.e. for preparing a service via Rest API for a test or
reading the state after a test.

## Examples:


### Making a GET request
```java
String resultJson = SimpleRestClient.requestTo("http://my.domain.com/rest/api/resource")
                                    .acceptJson() //accept header
                                    .get()
                                    .asString();
```

### Making a POST request

The following snipped uses javax.json JsonWriter to serialize a JSON object onto the OutputStream. 

```java
 String result = SimpleRestClient.requestTo("http://my.domain.com/rest/api/resource")
                                      .acceptJson() //accept header
                                      .sendJson() //conent type header
                                      .post(os -> {
                                         try (JsonWriter w = Json.createWriter(os)) {
                                            w.writeObject(jsonObject);
                                         }
                                      })
                                      .asString();
```

### Sending or Accepting other content types

```java
 SimpleRestClient.requestTo("http://my.domain.com/rest/api/resource")
                 .accept("text/html")
```

```java
 SimpleRestClient.requestTo("http://my.domain.com/rest/api/resource")
                 .contentType("text/html")
```

### Custom Headers

```java
 SimpleRestClient.requestTo("http://my.domain.com/rest/api/resource")
                 .addHeader("ContentLength", "20")
```


### Authentication

Authentication in general (if you have your auth token)
```java
String resultJson = SimpleRestClient.requestTo("http://my.domain.com/rest/api/resource")
                                    .auth("Basic QWxhZGRpbjpPcGVuU2VzYW1l")
                                    .acceptJson()
                                    .get()
                                    .asString();
```

Basic Authentication 
```java
String resultJson = SimpleRestClient.requestTo("http://my.domain.com/rest/api/resource")
                                    .basicAuth("username","password")
                                    .acceptJson()
                                    .get()
                                    .asString();
```

### JsonObjects or JsonArrays 
(using `javax.json`)

Add the following dependency to your pom
```xml
<dependency>
    <groupId>javax.json</groupId>
    <artifactId>javax.json-api</artifactId>
    <version>1.0</version>
</dependency>
```

Now read the inputstream from the response

```java
JsonObject json = Json.createReader(SimpleRestClient.requestTo("http://my.domain.com/rest/api/resource")
                                                  .acceptJson()
                                                  .get()
                                                  .asInputStream())
                                                  .readObject();
```


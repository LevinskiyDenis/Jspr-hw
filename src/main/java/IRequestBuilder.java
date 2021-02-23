import java.util.*;

public interface IRequestBuilder {

    Request build();

    String getRequestLineMethod();

    String getRequestLinePath();

    String getRequestLineProtocol();

    List<String> getHeaders();

    String getBody();

    HashMap<String, List<String>> getQueryStringParams();

    IRequestBuilder setRequestLineMethod(String requestLineMethod);

    IRequestBuilder setRequestLinePath(String requestLinePath);

    IRequestBuilder setRequestLineProtocol(String requestLineProtocol);

    IRequestBuilder setHeaders(List<String> headers);

    IRequestBuilder setBody(String body);

    IRequestBuilder setQueryStringParams(HashMap<String, List<String>> queryStringParams);

}
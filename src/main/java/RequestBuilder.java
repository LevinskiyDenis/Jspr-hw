import java.util.*;

public class RequestBuilder implements IRequestBuilder {

    String requestLineMethod;
    String requestLinePath;
    String requestLineProtocol;
    List<String> headers;
    String body;
    HashMap<String, List<String>> queryStringParams;

    @Override
    public IRequestBuilder setRequestLineMethod(String requestLineMethod) {
        this.requestLineMethod = requestLineMethod;
        return this;
    }

    @Override
    public IRequestBuilder setRequestLinePath(String requestLinePath) {
        this.requestLinePath = requestLinePath;
        return this;
    }

    @Override
    public IRequestBuilder setRequestLineProtocol(String requestLineProtocol) {
        this.requestLineProtocol = requestLineProtocol;
        return this;
    }

    @Override
    public IRequestBuilder setHeaders(List<String> headers) {
        this.headers = headers;
        return this;
    }

    @Override
    public IRequestBuilder setBody(String body) {
        this.body = body;
        return this;
    }

    @Override
    public IRequestBuilder setQueryStringParams(HashMap<String, List<String>> queryStringParams) {
        this.queryStringParams = queryStringParams;
        return this;
    }

    @Override
    public Request build() {
        if (requestLineMethod == null || requestLinePath == null || requestLineProtocol == null || headers == null) {
            System.out.println("Не установлены обязательны параметры для создания объекта Request");
        }
        return new Request(this);
    }

    @Override
    public String getRequestLineMethod() {
        return requestLineMethod;
    }

    @Override
    public String getRequestLinePath() {
        return requestLinePath;
    }

    @Override
    public String getRequestLineProtocol() {
        return requestLineProtocol;
    }

    @Override
    public List<String> getHeaders() {
        return headers;
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public HashMap<String, List<String>> getQueryStringParams() {
        return queryStringParams;
    }
}
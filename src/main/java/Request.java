import java.util.*;

public class Request {

    final String requestLineMethod;
    final String requestLinePath;
    final String requestLineProtocol;
    final List<String> headers;
    final String body;
    final HashMap<String, List<String>> queryStringParams;

    public Request(IRequestBuilder requestBuilder) {
        this.requestLineMethod = requestBuilder.getRequestLineMethod();
        this.requestLinePath = requestBuilder.getRequestLinePath();
        this.requestLineProtocol = requestBuilder.getRequestLineProtocol();
        this.headers = requestBuilder.getHeaders();
        this.body = requestBuilder.getBody();
        this.queryStringParams = requestBuilder.getQueryStringParams();
    }

    public String getRequestLinePath() {
        return requestLinePath;
    }

    public String getRequestLineMethod() {
        return requestLineMethod;
    }

    public String getRequestLineProtocol() {
        return requestLineProtocol;
    }

    public HashMap<String, List<String>> getQueryStringParams() {
        return queryStringParams;
    }

    public String getQueryStringParam(String key) {
        StringBuilder sb = new StringBuilder();
        if (queryStringParams.containsKey(key)) {
            List<String> list = queryStringParams.get(key);
            for (String s : list) {
                sb.append(s);
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Request{" +
                "requestLineMethod='" + requestLineMethod + '\'' +
                ", requestLinePath='" + requestLinePath + '\'' +
                ", requestLineProtocol='" + requestLineProtocol + '\'' +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                ", queryStringParams=" + queryStringParams +
                '}';
    }
}

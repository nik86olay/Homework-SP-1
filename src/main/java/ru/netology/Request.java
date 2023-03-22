package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Request {

    private final String method;
    private final String path;
    private final List<NameValuePair> query;
    private final List<String> headers;
    private final String body;

    private String value;

    public Request(String method, String path, String query, List<String> headers, String body) {
        this.method = method;
        try {
            this.query = (query == null) ? null : URLEncodedUtils.parse(new URI(query), String.valueOf(StandardCharsets.UTF_8));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    public String getQueryParam(String name) {
        if (query == null) value = "Query отсутствует";
        else {
            for (NameValuePair x : query) {
                if (x.getName().equals(name)) {
                    return value = x.getValue();
                }
            }
        }
        return value;
    }

    public List<String> getQueryParams() {
        if (query == null) return Collections.singletonList("Query отсутствует");
        return query.stream().map(NameValuePair::getValue).collect(Collectors.toList());
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "Значения Request" + '\n' +
                "Метод запроса: " + method + '\n' +
                "Путь: " + path + '\n' +
                "Параметры: " + getQueryParams() + '\n' +
                "Заголовки: " + headers.toString() + '\n' +
                "Значение query: " + getQueryParam("name") + '\n' +
                "Тело запроса: " + body;
    }
}

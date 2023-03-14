package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

    public class Request {

        private final String method;
        private final String path;
        private final List <NameValuePair> query;
        private final List<String> headers;
        private final String body;

        public Request(String method, String path, List<String> headers, String body) {
            this.method = method;
            this.query = getQueryParams(path);
            if(path.contains("?")) path = path.substring(path.indexOf('/'), path.indexOf('?'));
            this.path = path;
            this.headers = headers;
            this.body = body;
        }

        private List<NameValuePair> getQueryParams(String pathAndQuery) {
            try {
                return URLEncodedUtils.parse(new URI(pathAndQuery), String.valueOf(StandardCharsets.UTF_8));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
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
            return  "Значения Request" + '\n' +
                    "Метод запроса: '" + method + '\n' +
                    "Путь: " + path + '\n' +
                    "Параметры: " + query.toString() + '\n' +
                    "Заголовки: " + headers.toString() + '\n' +
                    "Тело запроса: " + body;
        }
    }

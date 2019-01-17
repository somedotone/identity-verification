package some.one.contract.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpRequest {

    public enum Type {
        GET, POST, HEAD, PUT, DELETE, CONNECT, OPTIONS, TRACE, PATCH;
    }

    public interface ResponseBufferReader {
        String parseResponse(BufferedReader reader) throws IOException;
    }


    private ResponseBufferReader responseBufferReader = null;



    public HttpRequest() {}

    public HttpRequest(ResponseBufferReader responseBufferReader) {
        this.responseBufferReader = responseBufferReader;
    }


    public String request(Type type, String url) throws IOException {
        return request(type, new URL(url));
    }

    public String request(Type type, URL url) throws IOException {
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setDoOutput(true);
        request.setRequestMethod(type.toString());
        request.connect();

        if(request.getResponseCode() != 200) throw new IOException();


        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(request.getInputStream()));

        if(responseBufferReader != null) {
            String response = responseBufferReader.parseResponse(bufferedReader);
            bufferedReader.close();
            request.disconnect();
            return response;
        } else {
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = bufferedReader.readLine()) != null) response.append(inputLine + "\n");
            bufferedReader.close();
            request.disconnect();
            return response.toString();
        }
    }
}




package some.one.contract.resources;

import some.one.contract.Constants;
import some.one.contract.exception.ContractException;

import java.io.BufferedReader;
import java.io.IOException;


//public class HttpHandler implements Resource {
public class HttpHandler {


    public interface UrlChecker {
        boolean isValidUrl(String url);
    }

    public interface StringResponseTokenExtractor {
        String extractToken(String response);
    }

    public interface BufferResponseTokenExtractor {
        String extractToken(BufferedReader response) throws IOException;
    }


    private UrlChecker urlChecker = null;
    private BufferResponseTokenExtractor bufferResponseTokenExtractor = null;
    private StringResponseTokenExtractor stringResponseTokenExtractor = null;



    public void checkUrl(UrlChecker urlChecker) {
        this.urlChecker = urlChecker;
    }


    public void extractTokenFromBuffer(BufferResponseTokenExtractor bufferResponseTokenExtractor) {
        this.bufferResponseTokenExtractor = bufferResponseTokenExtractor;
    }


    public void extractToken(StringResponseTokenExtractor stringResponseTokenExtractor) {
        this.stringResponseTokenExtractor = stringResponseTokenExtractor;
    }


//    @Override
    public String getToken(String url) throws ContractException {
        try {

            if(urlChecker != null && !urlChecker.isValidUrl(url)) {
                throw new ContractException(Constants.ERROR_CODE_INVALID_URL);
            }

            HttpRequest httpRequest;
            String token;

            if(bufferResponseTokenExtractor != null) {
                httpRequest = new HttpRequest((reader) -> bufferResponseTokenExtractor.extractToken(reader));
                token = httpRequest.request(HttpRequest.Type.GET, url);
            } else {
                httpRequest = new HttpRequest();
                String response = httpRequest.request(HttpRequest.Type.GET, url);
                if(stringResponseTokenExtractor != null) token = stringResponseTokenExtractor.extractToken(response);
                else token = response;
            }

            if(token == null || token.equals("")) throw new ContractException(Constants.ERROR_CODE_TOKEN_CLAIM_FAILED);
            return token;

        } catch (IOException e) {
            throw new ContractException(Constants.ERROR_CODE_TOKEN_CLAIM_FAILED);
        }
    }
}

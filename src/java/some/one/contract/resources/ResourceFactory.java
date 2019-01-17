package some.one.contract.resources;

import some.one.contract.Constants;
import some.one.contract.exception.ContractException;

public class ResourceFactory {
    private ResourceFactory() {}


    private static String[] resourceTypes = {
            "http",
            "https",
            "twitter",
            "facebook",
            "github"
    };


//    public static Resource getResource(String resourceType) throws ContractException {
    public static HttpHandler getResource(String resourceType) throws ContractException {
        HttpHandler httpHandler;
        switch(resourceType) {

            case "http":
                httpHandler = new HttpHandler();
                httpHandler.checkUrl(url -> url.startsWith("http://"));
                httpHandler.extractToken((response -> extractTokenFromHttpResponse(response)));
                return httpHandler;


            case "https":
                httpHandler = new HttpHandler();
                httpHandler.checkUrl(url -> url.startsWith("https://"));
                httpHandler.extractToken((response -> extractTokenFromHttpResponse(response)));
                return httpHandler;


            case "twitter":
                httpHandler = new HttpHandler();
                httpHandler.checkUrl(url -> url.startsWith("https://twitter.com/") || url.startsWith("https://www.twitter.com/"));
                httpHandler.extractToken(response -> {
                    String token = extractTokenFromHttpResponse(response);
                    return token.replace("&#10;", "").trim();
                });
                return httpHandler;


            case "facebook":
                httpHandler = new HttpHandler();
                httpHandler.checkUrl(url -> url.startsWith("https://facebook.com/") || url.startsWith("https://www.facebook.com/"));
                httpHandler.extractTokenFromBuffer(reader -> {
                    String inputLine;
                    while ((inputLine = reader.readLine()) != null) {
                        if(inputLine.startsWith("<div class=\"hidden_elem\"><code id=") && inputLine.contains(Constants.TOKEN_TAG)) {
                            String token = extractTokenFromHttpResponse(inputLine);
                            return token.replace("<br />", "").trim();
                        }
                    }
                    return null;
                });
                return httpHandler;


            case "github":
                httpHandler = new HttpHandler();
                httpHandler.checkUrl(url -> url.startsWith("https://github.com/") || url.startsWith("https://www.github.com/") ||
                        url.startsWith("https://raw.githubusercontent.com/"));
                httpHandler.extractToken((response -> extractTokenFromHttpResponse(response)));
                return httpHandler;


            default:
                throw new ContractException(Constants.ERROR_CODE_INVALID_RESOURCE);
        }
    }

    private static String extractTokenFromHttpResponse(String response) {
        int tokenStartTagIndex = response.indexOf(Constants.TOKEN_TAG);
        if(tokenStartTagIndex == -1) return null;

        int tokenEndTagIndex = response.indexOf(Constants.TOKEN_TAG, tokenStartTagIndex + 1);
        if(tokenEndTagIndex == -1) return null;

        String token = response.substring(tokenStartTagIndex + Constants.TOKEN_TAG.length(), tokenEndTagIndex);
        return token.trim();
    }


    public static String[] getResourceTypes() {
        return resourceTypes;
    }
}

package some.one.contract.messageprocessor;

import nxt.account.Account;
import nxt.account.Token;
import nxt.addons.JA;
import nxt.addons.JO;
import nxt.addons.TransactionContext;
import nxt.crypto.Crypto;
import nxt.http.callers.GetAccountPropertiesCall;
import nxt.util.Convert;
import some.one.contract.Constants;
import some.one.contract.IdentityVerification;
import some.one.contract.TransactionCreator;
import some.one.contract.exception.ContractException;
import some.one.contract.resources.HttpHandler;
import some.one.contract.resources.ResourceFactory;

import java.util.Date;

//public class VerificationRequestProcessor extends MessageProcessor {
public class VerificationRequestProcessor {

    private final int VERSION_INDEX = 1;
    private final int SENDER_ACCOUNT_INDEX = 2;
    private final int RESOURCE_INDEX = 3;
    private final int TIMESTAMP_INDEX = 4;


    private JO messageObject;
    private TransactionContext context;
    private int expirationMinutes;

    private String url;
    private String challengeText;
    private String[] splittedChallengeText;



    public VerificationRequestProcessor(JO messageObject, TransactionContext context) {
        this.messageObject = messageObject;
        this.context = context;
        this.expirationMinutes = Math.abs(context.getParams(IdentityVerification.Params.class).expirationMinutes());
    }


//    @Override
    void validate() throws ContractException {
        if(messageObject.size() != 3 + 1) throw new ContractException(Constants.ERROR_CODE_INVALID_PARAMS); // + 1 because of type parameter

        url = messageObject.getString(Constants.URL_KEY);
        if(url == null || url.equals("")) throw new ContractException(Constants.ERROR_CODE_MISSING_PARAM, Constants.URL_KEY);

        challengeText = messageObject.getString(Constants.CHALLENGE_TEXT_KEY);
        if(challengeText == null || challengeText.equals("")) throw new ContractException(Constants.ERROR_CODE_MISSING_PARAM, Constants.CHALLENGE_TEXT_KEY);

        String signature = messageObject.getString(Constants.SIGNATURE_KEY);
        if(signature == null || signature.equals("")) throw new ContractException(Constants.ERROR_CODE_MISSING_PARAM, Constants.SIGNATURE_KEY);

        splittedChallengeText = challengeText.split(":");

        checkSignature(signature, challengeText, context.getConfig().getSecretPhrase());
        checkDate(Long.parseLong(splittedChallengeText[TIMESTAMP_INDEX]), expirationMinutes);
        checkVersion(splittedChallengeText[VERSION_INDEX], Constants.VERSION);
        checkAccount(Convert.rsAccount(context.getSenderId()), splittedChallengeText[SENDER_ACCOUNT_INDEX]);
    }

    private void checkSignature(String signature, String data, String secretPhrase) throws ContractException {
        boolean isValid = Crypto.verify(Convert.parseHexString(signature), Convert.toBytes(data, true), Crypto.getPublicKey(secretPhrase));
        if(!isValid) throw new ContractException(Constants.ERROR_CODE_INVALID_SIGNATURE);
    }

    private void checkDate(long challengeTextTimestamp, int expirationMinutes) throws ContractException {
        long timestamp = (new Date().getTime()) / 1000; // convert to seconds
        long timeWindow = 10; // to prevent errors caused by asynchronous times on different devices

        if(challengeTextTimestamp + expirationMinutes * 60 < timestamp || challengeTextTimestamp - timeWindow > timestamp) {
            throw new ContractException(Constants.ERROR_CODE_TIME_EXPIRED);
        }
    }

    private void checkVersion(String challengeTextVersion, String version) throws ContractException {
        if(!challengeTextVersion.equals(version)) throw new ContractException(Constants.ERROR_CODE_INVALID_VERSION);
    }

    private void checkAccount(String senderAccount, String challengeAccount) throws ContractException {
        if (!senderAccount.equals(challengeAccount)) throw new ContractException(Constants.ERROR_CODE_WRONG_ACCOUNT);
    }


//    @Override
    void perform() throws ContractException {
//        Resource resource = ResourceFactory.getResource((splittedChallengeText[RESOURCE_INDEX]));
        HttpHandler resource = ResourceFactory.getResource(splittedChallengeText[RESOURCE_INDEX]);
        checkToken(resource.getToken(url), challengeText, context.getTransaction().getSenderId());
    }

    private void checkToken(String token, String data, long senderId) throws ContractException {
        if (token.length() != 160 || !token.matches("[0-9,a-z]*")) {
            throw new ContractException(Constants.ERROR_CODE_INVALID_TOKEN_FORMAT);
        }

        Token userToken = Token.parseToken(token, data);
        long userId = Account.getId(userToken.getPublicKey());

        if(!userToken.isValid() || (senderId != userId)) {
            throw new ContractException(Constants.ERROR_CODE_INVALID_TOKEN);
        }
    }


//    @Override
    JO response() {
        return TransactionCreator.createAccountProperty(context, createPropertyName(), challengeText);
    }

    private String createPropertyName() {
        String numberPrefix = Constants.PROPERTY_NAME + "[";
        String numberSuffix = "]";

        String  contractAccountRS = context.getAccountRs();
        String senderAccountRS = context.getTransaction().getSenderRs();
        JO propertiesObject = GetAccountPropertiesCall.create().recipient(senderAccountRS).setter(contractAccountRS).call();


        int index = -1;
        JA properties = propertiesObject.getArray("properties");
        for(JO propertyObject : properties.objects()) {
            String property = propertyObject.getString("property");

            if(property.startsWith(numberPrefix) && property.endsWith(numberSuffix)) {
                String indexString = property.substring(numberPrefix.length(), property.length() - numberSuffix.length());

                try { index = Integer.parseInt(indexString); }
                catch(Exception e) {}
            }
        }

        return numberPrefix + (index + 1) + numberSuffix;
    }


    public JO process() throws ContractException{
        validate();
        perform();
        return response();
    }
}

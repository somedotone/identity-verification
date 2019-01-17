package some.one.contract.messageprocessor;

import nxt.addons.JO;
import nxt.addons.TransactionContext;
import nxt.crypto.Crypto;
import nxt.util.Convert;
import some.one.contract.Constants;
import some.one.contract.IdentityVerification;
import some.one.contract.TransactionCreator;
import some.one.contract.exception.ContractException;

import java.util.Date;

//public class ChallengeTextRequestProcessor extends MessageProcessor {
public class ChallengeTextRequestProcessor {

    private JO messageObject;
    private TransactionContext context;
    private String contractDomain;

    private String challengeText;
    private String signature;
    private String accountRS;
    private String resource;



    public ChallengeTextRequestProcessor(JO messageObject, TransactionContext context) {
        this.messageObject = messageObject;
        this.context = context;
        this.contractDomain = getContractDomain(context);
    }

    private String getContractDomain(TransactionContext context) {
        String rawContractDomain = context.getParams(IdentityVerification.Params.class).contractDomain();
        return rawContractDomain.replace(":", "");
    }


//    @Override
    void validate() throws ContractException {
        if(messageObject.size() != 3) throw new ContractException(Constants.ERROR_CODE_INVALID_PARAMS);

        accountRS = messageObject.getString(Constants.ACCOUNTRS_KEY);
        if(accountRS == null || accountRS.equals("")) throw new ContractException(Constants.ERROR_CODE_MISSING_PARAM, Constants.ACCOUNTRS_KEY);

        resource = messageObject.getString(Constants.RESOURCES_KEY);
        if(resource == null || resource.equals("")) throw new ContractException(Constants.ERROR_CODE_MISSING_PARAM, Constants.RESOURCES_KEY);
    }


//    @Override
    void perform() {
        challengeText = contractDomain;
        challengeText += ":";
        challengeText += Constants.VERSION;
        challengeText += ":";
        challengeText += accountRS;
        challengeText += ":";
        challengeText += resource;
        challengeText += ":";
        challengeText += (new Date().getTime()) / 1000; // convert to seconds

        signature = createSignature(challengeText);
    }

    private String createSignature(String challengeText) {
        byte[] signatureBytes = Crypto.sign(Convert.toBytes(challengeText,true), context.getConfig().getSecretPhrase());
        return Convert.toHexString(signatureBytes);
    }


//    @Override
    JO response() {
        JO challengeTextResponse =  new JO();
        challengeTextResponse.put(Constants.MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_CHALLENGE_TEXT_RESPONSE);
        challengeTextResponse.put(Constants.CHALLENGE_TEXT_KEY, this.challengeText);
        challengeTextResponse.put(Constants.SIGNATURE_KEY, signature);

        return TransactionCreator.createPayment(context, context.getAmountNQT(), challengeTextResponse.toJSONString());
    }


    public JO process() throws ContractException{
        validate();
        perform();
        return response();
    }
}
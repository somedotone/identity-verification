package some.one.contract.messageprocessor;

import nxt.addons.JO;
import nxt.addons.TransactionContext;
import org.json.simple.JSONArray;
import some.one.contract.Constants;
import some.one.contract.IdentityVerification;
import some.one.contract.TransactionCreator;
import some.one.contract.exception.ContractException;
import some.one.contract.resources.ResourceFactory;

import java.util.Arrays;

//public class InfoRequestProcessor extends MessageProcessor {
public class InfoRequestProcessor {

    private JO messageObject;
    private TransactionContext context;

    private int price;
    private int chain;
    private int expirationMinutes;
    private String contractDomain;


    public InfoRequestProcessor(JO messageObject, TransactionContext context) {
        this.messageObject = messageObject;
        this.context = context;
    }


//    @Override
    void validate() throws ContractException {
        if(messageObject.size() != 1) throw new ContractException(Constants.ERROR_CODE_INVALID_PARAMS);
    }


//    @Override
    void perform() {
        IdentityVerification.Params params = context.getParams(IdentityVerification.Params.class);
        this.price = params.price();
        this.chain = params.chain();
        this.expirationMinutes = params.expirationMinutes();
        this.contractDomain = params.contractDomain();
    }


//    @Override
    JO response() {
        JO response = new JO();
        response.put(Constants.MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_INFO_RESPONSE);
        response.put(Constants.VERSION_KEY, Constants.VERSION);
        response.put(Constants.PRICE_KEY, price);
        response.put(Constants.CHAIN_KEY, chain);
        response.put(Constants.EXPIRATION_MINUTES_KEY, expirationMinutes);
        response.put(Constants.CONTRACT_DOMAIN_KEY, contractDomain);

        JSONArray resources = new JSONArray();
        resources.addAll(Arrays.asList(ResourceFactory.getResourceTypes()));
        response.put(Constants.RESOURCES_KEY, resources);

        return TransactionCreator.createPayment(context, context.getAmountNQT(), response.toJSONString(), false);
    }


    public JO process() throws ContractException{
        validate();
        perform();
        return response();
    }
}

package some.one.contract.exception;

import nxt.addons.JO;
import nxt.addons.TransactionContext;
import some.one.contract.TransactionCreator;

//public class TransactionExceptionProcessor implements ExceptionProcessor {
public class TransactionExceptionProcessor {

    TransactionContext context;
    ContractException exception;
    long amount;


    public TransactionExceptionProcessor(TransactionContext context, ContractException exception, long amount) {
        this.context = context;
        this.exception = exception;
        this.amount = amount;
    }

    
//    @Override
    public JO process() {
        String errorResponse = createErrorResponse(exception.getErrorCode(), exception.getMessage());
        return TransactionCreator.createPayment(context, amount, errorResponse);
    }

    private String createErrorResponse(int code, String description) {
        JO response = new JO();
        response.put("errorCode", code);
        response.put("errorDescription", description);
        return response.toJSONString();
    }
}


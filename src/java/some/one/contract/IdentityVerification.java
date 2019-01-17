package some.one.contract;

import nxt.addons.*;
import some.one.contract.exception.ContractException;
import some.one.contract.exception.TransactionExceptionProcessor;
import some.one.contract.messageprocessor.ChallengeTextRequestProcessor;
import some.one.contract.messageprocessor.InfoRequestProcessor;
import some.one.contract.messageprocessor.VerificationRequestProcessor;

import static nxt.blockchain.ChildChain.IGNIS;
import static nxt.blockchain.TransactionTypeEnum.CHILD_PAYMENT;
import static nxt.blockchain.TransactionTypeEnum.SEND_MESSAGE;



@ContractInfo(version = Constants.VERSION, description = Constants.DESCRIPTION)
public class IdentityVerification extends AbstractContract {


    @ContractParametersProvider
    public interface Params {

        @ContractSetupParameter
        default int chain() {
            return IGNIS.getId();
        }

        @ContractSetupParameter
        default int price() {
            return 50;
        }

        @ContractSetupParameter
        default int expirationMinutes() {
            return 60;
        }

        @ContractSetupParameter
        default String contractDomain() {
            return "IdentityVerification";
        }
    }


    @Override
    @ValidateContractRunnerIsRecipient
    @ValidateTransactionType(accept = {CHILD_PAYMENT, SEND_MESSAGE})
    public JO processTransaction(TransactionContext context) {
        long overpaidAmount = 0;


        try {

            checkChain(context);


            JO messageObject = context.getRuntimeParams();
            String messageType = MessageTypeValidator.checkMessageType(messageObject);
            if(messageType.equals(Constants.MESSAGE_TYPE_INFO_REQUEST)) {

//                MessageProcessor messageProcessor = new InfoRequestProcessor(messageObject, context);
                InfoRequestProcessor messageProcessor = new InfoRequestProcessor(messageObject, context);
                return messageProcessor.process();

            } else if(messageType.equals(Constants.MESSAGE_TYPE_CHALLENGE_TEXT_REQUEST)) {

//                MessageProcessor messageProcessor = new ChallengeTextRequestProcessor(messageObject, context);
                ChallengeTextRequestProcessor messageProcessor = new ChallengeTextRequestProcessor(messageObject, context);
                return messageProcessor.process();

            } else if(messageType.equals(Constants.MESSAGE_TYPE_VERIFICATION_REQUEST)) {

                long price = Math.abs(context.getParams(Params.class).price());
                overpaidAmount = checkPaidAmount(context, price);

//                MessageProcessor messageProcessor = new VerificationRequestProcessor(messageObject, context);
                VerificationRequestProcessor messageProcessor = new VerificationRequestProcessor(messageObject, context);
                JO response = messageProcessor.process();

                if(overpaidAmount > 0) throw new ContractException(Constants.ERROR_CODE_OVERPAID);
                return response;

            } else {
                throw new ContractException(Constants.ERROR_CODE_MESSAGE_NOT_SUPPORTED, messageType);
            }

        } catch (ContractException exception) {
            long amount = context.getTransaction().getAmount();
            if(exception.getErrorCode() == Constants.ERROR_CODE_OVERPAID) amount = overpaidAmount;

//            ExceptionProcessor exceptionProcessor = new TransactionExceptionProcessor(context, exception, amount);
            TransactionExceptionProcessor exceptionProcessor = new TransactionExceptionProcessor(context, exception, amount);
            return exceptionProcessor.process();
        }
    }

    private void checkChain(TransactionContext context) throws ContractException {
        int thisChainId = context.getChainOfTransaction().getId();
        int paramChainId = Math.abs(context.getParams(Params.class).chain());
        if(paramChainId != thisChainId) throw new ContractException(Constants.ERROR_CODE_WRONG_CHAIN);
    }

    private long checkPaidAmount(TransactionContext context, long price) throws ContractException {
        long paidAmount = context.getTransaction().getAmount();
        long priceInNqt = price * context.getChainOfTransaction().getOneCoin();

        if(paidAmount < priceInNqt) throw new ContractException(Constants.ERROR_CODE_UNDERPAID);
        return (paidAmount - priceInNqt);
    }
}
package some.one.contract;

import nxt.addons.JO;
import some.one.contract.exception.ContractException;

public class MessageTypeValidator {

    private MessageTypeValidator() {}


    public static String checkMessageType(JO messageObject) throws ContractException {
        if(messageObject == null) throw new ContractException(Constants.ERROR_CODE_NO_PARAMS);

        String type = messageObject.getString(Constants.MESSAGE_TYPE_KEY);
        if(type == null) throw new ContractException(Constants.ERROR_CODE_NO_MESSAGE_TYPE);
        if(isInvalidMessageType(type)) throw new ContractException(Constants.ERROR_CODE_NO_MESSAGE_TYPE);

        return type;
    }

    private static boolean isInvalidMessageType(String type) {
        if(!type.equals(Constants.MESSAGE_TYPE_CHALLENGE_TEXT_REQUEST) && !type.equals(Constants.MESSAGE_TYPE_VERIFICATION_REQUEST) &&
                !type.equals(Constants.MESSAGE_TYPE_INFO_REQUEST)) {
            return true;
        }
        return false;
    }
}

package some.one.contract.exception;

import some.one.contract.Constants;

public class ContractException extends Exception {

    private int errorCode;
    private String parameter = "";


    public ContractException(int errorCode) {
        this.errorCode = errorCode;
    }

    public ContractException(int errorCode, String parameter) {
        this.errorCode = errorCode;
        this.parameter = parameter;
    }

    public int getErrorCode() {
        return errorCode;
    }


    @Override
    public String getMessage(){
        switch (errorCode) {
            case Constants.ERROR_CODE_NO_PARAMS:
                return Constants.ERROR_TEXT_NO_PARAMS;

            case Constants.ERROR_CODE_MALFORMED_MESSAGE:
                return Constants.ERROR_TEXT_MALFORMED_MESSAGE;

            case Constants.ERROR_CODE_NO_MESSAGE_TYPE:
                return Constants.ERROR_TEXT_NO_MESSAGE_TYPE;

            case Constants.ERROR_CODE_MISSING_PARAM:
                return Constants.ERROR_TEXT_MISSING_PARAM + parameter;

            case Constants.ERROR_CODE_INVALID_PARAMS:
                return Constants.ERROR_TEXT_INVALID_PARAMS;

            case Constants.ERROR_CODE_MESSAGE_NOT_SUPPORTED:
                return Constants.ERROR_TEXT_MESSAGE_NOT_SUPPORTED + parameter;

            case Constants.ERROR_CODE_TOKEN_CLAIM_FAILED:
                return Constants.ERROR_TEXT_TOKEN_CLAIM_FAILED;

            case Constants.ERROR_CODE_INVALID_SIGNATURE:
                return Constants.ERROR_TEXT_INVALID_SIGNATURE;

            case Constants.ERROR_CODE_INVALID_TOKEN:
                return Constants.ERROR_TEXT_INVALID_TOKEN;

            case Constants.ERROR_CODE_WRONG_CHAIN:
                return Constants.ERROR_TEXT_WRONG_CHAIN;

            case Constants.ERROR_CODE_UNDERPAID:
                return Constants.ERROR_TEXT_UNDERPAID;

            case Constants.ERROR_CODE_OVERPAID:
                return Constants.ERROR_TEXT_OVERPAID;

            case Constants.ERROR_CODE_INVALID_RESOURCE:
                return Constants.ERROR_TEXT_INVALID_RESOURCE;

            case Constants.ERROR_CODE_TIME_EXPIRED:
                return Constants.ERROR_TEXT_TIME_EXPIRED;

            case Constants.ERROR_CODE_INVALID_VERSION:
                return Constants.ERROR_TEXT_INVALID_VERSION;

            case Constants.ERROR_CODE_WRONG_ACCOUNT:
                return Constants.ERROR_TEXT_WRONG_ACCOUNT;

            case Constants.ERROR_CODE_INVALID_URL:
                return Constants.ERROR_TEXT_INVALID_URL + parameter;

            case Constants.ERROR_CODE_INVALID_TOKEN_FORMAT:
                return Constants.ERROR_TEXT_INVALID_TOKEN_FORMAT;

            default:
                return Constants.ERROR_TEXT_GENERIC;

        }
    }
}

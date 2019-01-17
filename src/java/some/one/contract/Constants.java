package some.one.contract;

public class Constants {

    private Constants() {}


    /**********************************************************************************
     * CONTRACT CONSTANTS
     *********************************************************************************/

    public static final String VERSION = "v0.9.0";
    public static final String DESCRIPTION = "This contract is an implementation of Jelurida's Identity Verification hackathon challenge (https://www.jelurida.com/ardor-hackathon-2018)";


    /**********************************************************************************
     * MESSAGE CONSTANTS
     *********************************************************************************/

    public static final String PARAMETER_OBJECT_NAME = "setupParams";

    public static final String MESSAGE_TYPE_KEY = "type";

    public static final String MESSAGE_TYPE_CHALLENGE_TEXT_REQUEST = "challengeTextRequest";
    public static final String MESSAGE_TYPE_CHALLENGE_TEXT_RESPONSE = "challengeTextResponse";
    public static final String MESSAGE_TYPE_VERIFICATION_REQUEST = "accountVerificationRequest";
    public static final String MESSAGE_TYPE_INFO_REQUEST = "infoRequest";
    public static final String MESSAGE_TYPE_INFO_RESPONSE = "infoResponse";

    public static final String CHALLENGE_TEXT_KEY = "challengeText";
    public static final String SIGNATURE_KEY = "signature";
    public static final String URL_KEY = "url";
    public static final String RESOURCES_KEY = "resources";
    public static final String VERSION_KEY = "version";
    public static final String PRICE_KEY = "price";
    public static final String CHAIN_KEY = "chain";
    public static final String EXPIRATION_MINUTES_KEY = "expirationMinutes";
    public static final String CONTRACT_DOMAIN_KEY = "contractDomain";
    public static final String ACCOUNTRS_KEY = "accountRS";


    /**********************************************************************************
     * PROPERTY CONSTANTS
     *********************************************************************************/

    public static final String PROPERTY_NAME = "idVerConChallengeText";


    /**********************************************************************************
     * TOKEN CLAIM CONSTANTS
     *********************************************************************************/

    public static final String TOKEN_TAG = "** Ardor Identity Verification Token **";

    
    /**********************************************************************************
     * ERROR CONSTANTS
     *********************************************************************************/

    public static final int ERROR_OFFSET_CODE = 10000;

    public static final int ERROR_CODE_GENERIC = ERROR_OFFSET_CODE + 42;
    public static final String ERROR_TEXT_GENERIC = "an unknown error occurred";

    public static final int ERROR_CODE_NO_PARAMS = ERROR_OFFSET_CODE + 1;
    public static final String ERROR_TEXT_NO_PARAMS = PARAMETER_OBJECT_NAME + " message object not found";

    public static final int ERROR_CODE_MALFORMED_MESSAGE = ERROR_OFFSET_CODE + 2;
    public static final String ERROR_TEXT_MALFORMED_MESSAGE = PARAMETER_OBJECT_NAME + " message object malformed";

    public static final int ERROR_CODE_NO_MESSAGE_TYPE = ERROR_OFFSET_CODE + 3;
    public static final String ERROR_TEXT_NO_MESSAGE_TYPE = "message type not supported";

    public static final int ERROR_CODE_MISSING_PARAM = ERROR_OFFSET_CODE + 4;
    public static final String ERROR_TEXT_MISSING_PARAM = "following parameter not found: ";

    public static final int ERROR_CODE_INVALID_PARAMS = ERROR_OFFSET_CODE + 5;
    public static final String ERROR_TEXT_INVALID_PARAMS = "message has invalid number of parameter";

    public static final int ERROR_CODE_MESSAGE_NOT_SUPPORTED = ERROR_OFFSET_CODE + 6;
    public static final String ERROR_TEXT_MESSAGE_NOT_SUPPORTED = "following message not support via this channel: ";

    public static final int ERROR_CODE_TOKEN_CLAIM_FAILED = ERROR_OFFSET_CODE + 7;
    public static final String ERROR_TEXT_TOKEN_CLAIM_FAILED = "token could not be claimed";

    public static final int ERROR_CODE_INVALID_SIGNATURE = ERROR_OFFSET_CODE + 8;
    public static final String ERROR_TEXT_INVALID_SIGNATURE = "invalid signature";

    public static final int ERROR_CODE_INVALID_TOKEN = ERROR_OFFSET_CODE + 9;
    public static final String ERROR_TEXT_INVALID_TOKEN = "invalid token";

    public static final int ERROR_CODE_WRONG_CHAIN = ERROR_OFFSET_CODE + 10;
    public static final String ERROR_TEXT_WRONG_CHAIN = "contract does not provide this chain";

    public static final int ERROR_CODE_UNDERPAID = ERROR_OFFSET_CODE + 11;
    public static final String ERROR_TEXT_UNDERPAID = "contract was underpaid";

    public static final int ERROR_CODE_OVERPAID = ERROR_OFFSET_CODE + 12;
    public static final String ERROR_TEXT_OVERPAID = "contract overpaid. Contract executed correctly and overpaid amount is returned";

    public static final int ERROR_CODE_INVALID_RESOURCE = ERROR_OFFSET_CODE + 13;
    public static final String ERROR_TEXT_INVALID_RESOURCE = "invalid resource";

    public static final int ERROR_CODE_TIME_EXPIRED = ERROR_OFFSET_CODE + 14;
    public static final String ERROR_TEXT_TIME_EXPIRED  = "verification time expired";

    public static final int ERROR_CODE_INVALID_VERSION = 15;
    public static final String ERROR_TEXT_INVALID_VERSION = "invalid version";

    public static final int ERROR_CODE_WRONG_ACCOUNT = ERROR_OFFSET_CODE + 16;
    public static final String ERROR_TEXT_WRONG_ACCOUNT  = "sender account does not match with challenge text account";

    public static final int ERROR_CODE_INVALID_TOKEN_FORMAT = ERROR_OFFSET_CODE + 17;
    public static final String ERROR_TEXT_INVALID_TOKEN_FORMAT = "invalid token format";

    public static final int ERROR_CODE_INVALID_URL = ERROR_OFFSET_CODE + 18;
    public static final String ERROR_TEXT_INVALID_URL = "url not allowed for specified resource type";
}

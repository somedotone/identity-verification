package some.one.contract;

import nxt.Tester;
import nxt.account.AccountPropertyAttachment;
import nxt.account.AccountPropertyTransactionType;
import nxt.account.PaymentTransactionType;
import nxt.account.Token;
import nxt.addons.JO;
import nxt.blockchain.*;
import nxt.crypto.Crypto;
import nxt.http.callers.SetAccountPropertyCall;
import nxt.messaging.PrunableEncryptedMessageAppendix;
import nxt.messaging.PrunablePlainMessageAppendix;
import nxt.util.Convert;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import some.one.contract.resources.ResourceFactory;

import java.util.Arrays;
import java.util.Date;

import static nxt.blockchain.ChildChain.AEUR;
import static nxt.blockchain.ChildChain.IGNIS;

public class IdentityVerificationTest extends AbstractContractTest {

    private final String TESTSERVER_URL = "http://127.0.0.1:8042/";

    private final int CONTRACT_PRICE = 5;
    private final int CONTRACT_CHAIN = IGNIS.getId();
    private final int CONTRACT_EXPIRATION_MINUTES = 60;
    private final String CONTRACT_CONTRACT_DOMAIN = "IdentityVerification";


    @Test
    public void transactionErrorTest() {
        String contractName = ContractTestHelper.deployContract(IdentityVerification.class, createSetupParams());

        checkTransactionEmptyContractParamsError(contractName);
    }

    private JO createSetupParams() {
        return createSetupParams(CONTRACT_PRICE, CONTRACT_CHAIN, CONTRACT_EXPIRATION_MINUTES, CONTRACT_CONTRACT_DOMAIN);
    }

    private JO createSetupParams(int price, int chain, int expirationMinutes, String contractDomain) {
        JO setupParams = new JO();

        setupParams.put("price", price);
        setupParams.put("chain", chain);
        setupParams.put("expirationMinutes", expirationMinutes);
        setupParams.put("contractDomain", contractDomain);
        return setupParams;
    }

    private void checkTransactionEmptyContractParamsError(String contractName) {
        JO messageJson = new JO();
        messageJson.put("contract", contractName);


        String fullHash = ContractTestHelper.bobPaysContract(messageJson.toJSONString(), IGNIS);
        generateBlock();


        ChildTransaction transaction = findLastChildTransaction(PaymentTransactionType.ORDINARY);
        Assert.assertNotNull(transaction);


        performBasicTransactionChecks(transaction, fullHash);


        JO response = extractMessageObject(transaction);
        Assert.assertNotNull(response);
        Assert.assertEquals(Constants.ERROR_TEXT_NO_MESSAGE_TYPE, response.getString("errorDescription"));
        Assert.assertEquals(Constants.ERROR_CODE_NO_MESSAGE_TYPE, response.getInt("errorCode"));
    }

    private ChildTransaction findLastChildTransaction(TransactionType transactionType) {
        Block lastBlock = getLastBlock();
        for (FxtTransaction transaction : lastBlock.getFxtTransactions()) {
            for (ChildTransaction childTransaction : transaction.getSortedChildTransactions()) {
                if (ChildTransactionType.findTransactionType(childTransaction.getType().getType(), childTransaction.getType().getSubtype()) == transactionType) {
                    return childTransaction;
                }
            }
        }
        return null;
    }

    private void performBasicTransactionChecks(ChildTransaction transaction, String referencedTransactionHash) {
        performBasicTransactionChecks(IGNIS, transaction, referencedTransactionHash, 100);
    }

    private void performBasicTransactionChecks(ChildChain chain, ChildTransaction transaction, String referencedTransactionHash) {
        performBasicTransactionChecks(chain, transaction, referencedTransactionHash, 100);
    }

    private void performBasicTransactionChecks(ChildTransaction transaction, String referencedTransactionHash, int amount) {
        performBasicTransactionChecks(IGNIS, transaction, referencedTransactionHash, amount);
    }

    private void performBasicTransactionChecks(ChildChain chain, ChildTransaction transaction, String referencedTransactionHash, int amount) {
        Assert.assertEquals(chain.getId(), transaction.getChain().getId());
        Assert.assertEquals(ALICE.getId(), transaction.getSenderId());
        Assert.assertEquals(BOB.getId(), transaction.getRecipientId());
        Assert.assertEquals(referencedTransactionHash, Convert.toHexString(transaction.getReferencedTransactionId().getFullHash()));
        Assert.assertEquals(amount * chain.ONE_COIN - transaction.getFee(), transaction.getAmount());
    }

    private JO extractMessageObject(ChildTransaction transaction) {
        return extractMessageObject(transaction, true);
    }

    private JO extractMessageObject(ChildTransaction transaction, boolean isEncrypted) {
        byte[] data;
        if(isEncrypted) {
            PrunableEncryptedMessageAppendix appendix = (PrunableEncryptedMessageAppendix) transaction.getAppendages().stream().filter(a -> a instanceof PrunableEncryptedMessageAppendix).findFirst().orElse(null);
            byte[] compressedData = appendix.getEncryptedData().decrypt(BOB.getSecretPhrase(), ALICE.getPublicKey());
            data = Convert.uncompress(compressedData);
        } else {
            PrunablePlainMessageAppendix appendix = (PrunablePlainMessageAppendix) transaction.getAppendages().stream().filter(a -> a instanceof PrunablePlainMessageAppendix).findFirst().orElse(null);
            data = appendix.getMessage();
        }

        return JO.parse(Convert.toString(data, true));
    }


    @Test
    public void challengeTextRequestTest() {
        String contractName = ContractTestHelper.deployContract(IdentityVerification.class, createSetupParams());

        checkChallengeTextInvalidNumOfParamError(contractName);
        checkChallengeTextParamNotFoundError(contractName);
        checkChallengeTextResponseStructure(contractName);
        checkChallengeTextResponseValues(contractName);
        checkChallengeTextUnderpaid(contractName);
    }

    private void checkChallengeTextInvalidNumOfParamError(String contractName) {
        JO messageJson = new JO();
        messageJson.put("contract", contractName);

        JO params = new JO();
        params.put(Constants.MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_VERIFICATION_REQUEST);
        messageJson.put("params", params);


        String fullHash = ContractTestHelper.bobPaysContract(messageJson.toJSONString(), IGNIS);
        generateBlock();


        ChildTransaction transaction = findLastChildTransaction(PaymentTransactionType.ORDINARY);
        Assert.assertNotNull(transaction);


        performBasicTransactionChecks(transaction, fullHash);


        JO response = extractMessageObject(transaction);
        Assert.assertNotNull(response);
        Assert.assertEquals(Constants.ERROR_TEXT_INVALID_PARAMS, response.getString("errorDescription"));
        Assert.assertEquals(Constants.ERROR_CODE_INVALID_PARAMS, response.getInt("errorCode"));
    }

    private void checkChallengeTextParamNotFoundError(String contractName) {
        JO messageJson = new JO();
        messageJson.put("contract", contractName);

        JO params = new JO();
        params.put(Constants.MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_CHALLENGE_TEXT_REQUEST);
        params.put(Constants.ACCOUNTRS_KEY, "dummy");
        params.put("dummy", "dummyParam");
        messageJson.put("params", params);


        String fullHash = ContractTestHelper.bobPaysContract(messageJson.toJSONString(), IGNIS);
        generateBlock();


        ChildTransaction transaction = findLastChildTransaction(PaymentTransactionType.ORDINARY);
        Assert.assertNotNull(transaction);


        performBasicTransactionChecks(transaction, fullHash);


        JO response = extractMessageObject(transaction);
        Assert.assertNotNull(response);
        Assert.assertEquals(Constants.ERROR_TEXT_MISSING_PARAM + Constants.RESOURCES_KEY, response.getString("errorDescription"));
        Assert.assertEquals(Constants.ERROR_CODE_MISSING_PARAM, response.getInt("errorCode"));
    }

    private void checkChallengeTextResponseStructure(String contractName) {
        JO response = getChallengeTextResponse(contractName);

        Assert.assertEquals(response.getString(Constants.MESSAGE_TYPE_KEY), Constants.MESSAGE_TYPE_CHALLENGE_TEXT_RESPONSE);
        Assert.assertNotNull(response.getString(Constants.CHALLENGE_TEXT_KEY));
        Assert.assertNotNull(response.getString(Constants.SIGNATURE_KEY));
        Assert.assertEquals(3, response.size());
    }

    private JO getChallengeTextResponse(String contractName) {
        return getChallengeTextResponse(contractName, BOB);
    }

    private JO getChallengeTextResponse(String contractName, Tester tester) {
        return getChallengeTextResponse("http", contractName, tester);
    }

    private JO getChallengeTextResponse(String urlType, String contractName) {
        return getChallengeTextResponse(urlType, contractName, BOB);
    }

    private JO getChallengeTextResponse(String urlType, String contractName, Tester tester) {
        JO messageJson = new JO();
        messageJson.put("contract", contractName);

        JO params = new JO();
        params.put(Constants.MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_CHALLENGE_TEXT_REQUEST);
        params.put(Constants.ACCOUNTRS_KEY, tester.getRsAccount());
        params.put(Constants.RESOURCES_KEY, urlType);
        messageJson.put("params", params);


        String fullHash = ContractTestHelper.bobPaysContract(messageJson.toJSONString(), IGNIS);
        generateBlock();


        ChildTransaction transaction = findLastChildTransaction(PaymentTransactionType.ORDINARY);
        Assert.assertNotNull(transaction);


        performBasicTransactionChecks(transaction, fullHash);


        return extractMessageObject(transaction);
    }

    private void checkChallengeTextResponseValues(String contractName) {
        JO transactionResponse = getChallengeTextResponse(contractName);

        String challengeText = transactionResponse.getString(Constants.CHALLENGE_TEXT_KEY);
        String signature = transactionResponse.getString(Constants.SIGNATURE_KEY);


        boolean isSignature = Crypto.verify(Convert.parseHexString(signature), Convert.toBytes(challengeText, true), Crypto.getPublicKey(aliceSecretPhrase));
        Assert.assertTrue(isSignature);

        String challengeTextStub = challengeText;
        Assert.assertTrue(checkDate(challengeTextStub));

        challengeTextStub = challengeTextStub.substring(0, challengeTextStub.lastIndexOf(":"));
        String challengeTextResource = challengeTextStub.substring(challengeTextStub.lastIndexOf(":") + 1);
        Assert.assertTrue(challengeTextResource.equals("http"));

        challengeTextStub = challengeTextStub.substring(0, challengeTextStub.lastIndexOf(":"));
        String challengeTextAccountRs = challengeTextStub.substring(challengeTextStub.lastIndexOf(":") + 1);
        Assert.assertTrue(challengeTextAccountRs.equals(BOB.getRsAccount()));

        challengeTextStub = challengeTextStub.substring(0, challengeTextStub.lastIndexOf(":"));
        String challengeTextVersion = challengeTextStub.substring(challengeTextStub.lastIndexOf(":") + 1);
        Assert.assertTrue(challengeTextVersion.equals(Constants.VERSION));

        String challengeTextName = challengeTextStub.substring(0, challengeTextStub.lastIndexOf(":"));
        Assert.assertTrue(challengeTextName.equals(CONTRACT_CONTRACT_DOMAIN));
    }

    private boolean checkDate(String challengeTextStub) {
        long challengeTextTimestamp = Long.parseLong(challengeTextStub.substring(challengeTextStub.lastIndexOf(":") + 1));
        long timestamp = (new Date().getTime()) / 1000; // convert to seconds
        long timeWindow = 10;

        if (challengeTextTimestamp + timeWindow < timestamp || challengeTextTimestamp - timeWindow > timestamp) return false;
        return true;
    }

    private void checkChallengeTextUnderpaid(String contractName) {
        JO messageJson = new JO();
        messageJson.put("contract", contractName);

        JO params = new JO();
        params.put(Constants.MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_CHALLENGE_TEXT_REQUEST);
        params.put(Constants.ACCOUNTRS_KEY, BOB.getRsAccount());
        params.put(Constants.RESOURCES_KEY, "http");
        messageJson.put("params", params);


        ContractTestHelper.bobPaysContract(messageJson.toJSONString(), IGNIS, 0);
        generateBlock();


        ChildTransaction transaction = findLastChildTransaction(PaymentTransactionType.ORDINARY);
        Assert.assertNull(transaction);
    }


    @Test
    public void verificationErrorTest() {
        String contractName = ContractTestHelper.deployContract(IdentityVerification.class, createSetupParams());

        checkVerificationInvalidNumOfParamError(contractName);
        checkVerificationParamNotFoundError(contractName);
        checkVerificationTokenClaimError(contractName);
        checkVerificationTokenTagError(contractName);
        checkVerificationInvalidSignatureError(contractName);
        checkVerificationInvalidTokenError(contractName);
        checkVerificationInvalidTokenFormatError1(contractName);
        checkVerificationInvalidTokenFormatError2(contractName);
        checkVerificationInvalidUrlError(contractName);
        checkVerificationUnderpaidError(contractName);
        checkVerificationWrongAccountError(contractName);
        checkVerificationChainError(contractName);
    }

    private void checkVerificationInvalidNumOfParamError(String contractName) {
        JO messageJson = new JO();
        messageJson.put("contract", contractName);

        JO params = new JO();
        params.put(Constants.MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_VERIFICATION_REQUEST);
        params.put(Constants.CHALLENGE_TEXT_KEY, "dummyText");
        params.put(Constants.SIGNATURE_KEY, "dummySignature");
        messageJson.put("params", params);


        String fullHash = ContractTestHelper.bobPaysContract(messageJson.toJSONString(), IGNIS, false, 100);
        generateBlock();


        ChildTransaction transaction = findLastChildTransaction(PaymentTransactionType.ORDINARY);
        Assert.assertNotNull(transaction);


        performBasicTransactionChecks(transaction, fullHash);


        JO response = extractMessageObject(transaction);
        Assert.assertNotNull(response);
        Assert.assertEquals(Constants.ERROR_TEXT_INVALID_PARAMS, response.getString("errorDescription"));
        Assert.assertEquals(Constants.ERROR_CODE_INVALID_PARAMS, response.getInt("errorCode"));
    }

    private void checkVerificationParamNotFoundError(String contractName) {
        JO messageJson = new JO();
        messageJson.put("contract", contractName);

        JO params = new JO();
        params.put(Constants.MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_VERIFICATION_REQUEST);
        params.put(Constants.CHALLENGE_TEXT_KEY, "dummyText");
        params.put(Constants.SIGNATURE_KEY, "dummySignature");
        params.put("dummy", "dummyParam");
        messageJson.put("params", params);


        String fullHash = ContractTestHelper.bobPaysContract(messageJson.toJSONString(), IGNIS);
        generateBlock();


        ChildTransaction transaction = findLastChildTransaction(PaymentTransactionType.ORDINARY);
        Assert.assertNotNull(transaction);


        performBasicTransactionChecks(transaction, fullHash);


        JO response = extractMessageObject(transaction);
        Assert.assertNotNull(response);
        Assert.assertEquals(Constants.ERROR_TEXT_MISSING_PARAM + Constants.URL_KEY, response.getString("errorDescription"));
        Assert.assertEquals(Constants.ERROR_CODE_MISSING_PARAM, response.getInt("errorCode"));
    }

    private void checkVerificationTokenClaimError(String contractName) {
        JO transactionResponse = getChallengeTextResponse(contractName);

        String challengeText = transactionResponse.getString(Constants.CHALLENGE_TEXT_KEY);
        String signature = transactionResponse.getString(Constants.SIGNATURE_KEY);


        JO messageJson = new JO();
        messageJson.put("contract", contractName);

        JO params = new JO();
        params.put(Constants.MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_VERIFICATION_REQUEST);
        params.put(Constants.URL_KEY, TESTSERVER_URL);
        params.put(Constants.CHALLENGE_TEXT_KEY, challengeText);
        params.put(Constants.SIGNATURE_KEY, signature);
        messageJson.put("params", params);


        String fullHash = ContractTestHelper.bobPaysContract(messageJson.toJSONString(), IGNIS);
        generateBlock();


        ChildTransaction transaction = findLastChildTransaction(PaymentTransactionType.ORDINARY);
        Assert.assertNotNull(transaction);


        performBasicTransactionChecks(transaction, fullHash);


        JO response = extractMessageObject(transaction);
        Assert.assertNotNull(response);
        Assert.assertEquals(Constants.ERROR_TEXT_TOKEN_CLAIM_FAILED, response.getString("errorDescription"));
        Assert.assertEquals(Constants.ERROR_CODE_TOKEN_CLAIM_FAILED, response.getInt("errorCode"));
    }

    private void checkVerificationTokenTagError(String contractName) {
        JO transactionResponse = getChallengeTextResponse(contractName);

        String challengeText = transactionResponse.getString(Constants.CHALLENGE_TEXT_KEY);
        String signature = transactionResponse.getString(Constants.SIGNATURE_KEY);
        String token = Token.generateToken(BOB.getSecretPhrase(), challengeText);


        TestServer server = new TestServer();
        server.setOneShot(true);
        server.startServer(token);


        JO messageJson = new JO();
        messageJson.put("contract", contractName);

        JO params = new JO();
        params.put(Constants.MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_VERIFICATION_REQUEST);
        params.put(Constants.URL_KEY, TESTSERVER_URL);
        params.put(Constants.CHALLENGE_TEXT_KEY, challengeText);
        params.put(Constants.SIGNATURE_KEY, signature);
        messageJson.put("params", params);


        String fullHash = ContractTestHelper.bobPaysContract(messageJson.toJSONString(), IGNIS);
        generateBlock();


        ChildTransaction transaction = findLastChildTransaction(PaymentTransactionType.ORDINARY);
        Assert.assertNotNull(transaction);


        performBasicTransactionChecks(transaction, fullHash);


        JO response = extractMessageObject(transaction);
        Assert.assertNotNull(response);
        Assert.assertEquals(Constants.ERROR_TEXT_TOKEN_CLAIM_FAILED, response.getString("errorDescription"));
        Assert.assertEquals(Constants.ERROR_CODE_TOKEN_CLAIM_FAILED, response.getInt("errorCode"));
    }

    private void checkVerificationInvalidTokenError(String contractName) {
        JO transactionResponse = getChallengeTextResponse(contractName);

        String challengeText = transactionResponse.getString(Constants.CHALLENGE_TEXT_KEY);
        String signature = transactionResponse.getString(Constants.SIGNATURE_KEY);
        String token = Token.generateToken(BOB.getSecretPhrase(), challengeText);
        token = token.substring(0, token.length() - 8);
        token += "deadbeef";


        TestServer server = new TestServer();
        server.setOneShot(true);
        server.startServer(createTokenText(token));


        JO messageJson = new JO();
        messageJson.put("contract", contractName);

        JO params = new JO();
        params.put(Constants.MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_VERIFICATION_REQUEST);
        params.put(Constants.URL_KEY, TESTSERVER_URL);
        params.put(Constants.CHALLENGE_TEXT_KEY, challengeText);
        params.put(Constants.SIGNATURE_KEY, signature);
        messageJson.put("params", params);


        String fullHash = ContractTestHelper.bobPaysContract(messageJson.toJSONString(), IGNIS);
        generateBlock();


        ChildTransaction transaction = findLastChildTransaction(PaymentTransactionType.ORDINARY);
        Assert.assertNotNull(transaction);


        performBasicTransactionChecks(transaction, fullHash);


        JO response = extractMessageObject(transaction);
        Assert.assertNotNull(response);
        Assert.assertEquals(Constants.ERROR_TEXT_INVALID_TOKEN, response.getString("errorDescription"));
        Assert.assertEquals(Constants.ERROR_CODE_INVALID_TOKEN, response.getInt("errorCode"));
    }

    private String createTokenText(String token) {
        String tokenString = Constants.TOKEN_TAG + "\n";
        tokenString += token + "\n";
        tokenString += Constants.TOKEN_TAG;
        return tokenString;
    }

    private void checkVerificationInvalidTokenFormatError1(String contractName) {
        JO transactionResponse = getChallengeTextResponse(contractName);

        String challengeText = transactionResponse.getString(Constants.CHALLENGE_TEXT_KEY);
        String signature = transactionResponse.getString(Constants.SIGNATURE_KEY);
        String token = Token.generateToken(BOB.getSecretPhrase(), challengeText);
        token = token.substring(0, token.length() - 8);
        token += ".,@12345";


        TestServer server = new TestServer();
        server.setOneShot(true);
        server.startServer(createTokenText(token));


        JO messageJson = new JO();
        messageJson.put("contract", contractName);

        JO params = new JO();
        params.put(Constants.MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_VERIFICATION_REQUEST);
        params.put(Constants.URL_KEY, TESTSERVER_URL);
        params.put(Constants.CHALLENGE_TEXT_KEY, challengeText);
        params.put(Constants.SIGNATURE_KEY, signature);
        messageJson.put("params", params);


        String fullHash = ContractTestHelper.bobPaysContract(messageJson.toJSONString(), IGNIS);
        generateBlock();


        ChildTransaction transaction = findLastChildTransaction(PaymentTransactionType.ORDINARY);
        Assert.assertNotNull(transaction);


        performBasicTransactionChecks(transaction, fullHash);


        JO response = extractMessageObject(transaction);
        Assert.assertNotNull(response);
        Assert.assertEquals(Constants.ERROR_TEXT_INVALID_TOKEN_FORMAT, response.getString("errorDescription"));
        Assert.assertEquals(Constants.ERROR_CODE_INVALID_TOKEN_FORMAT, response.getInt("errorCode"));
    }

    private void checkVerificationInvalidTokenFormatError2(String contractName) {
        JO transactionResponse = getChallengeTextResponse(contractName);

        String challengeText = transactionResponse.getString(Constants.CHALLENGE_TEXT_KEY);
        String signature = transactionResponse.getString(Constants.SIGNATURE_KEY);
        String token = Token.generateToken(BOB.getSecretPhrase(), challengeText);
        token = token.substring(0, token.length() - 8);


        TestServer server = new TestServer();
        server.setOneShot(true);
        server.startServer(createTokenText(token));


        JO messageJson = new JO();
        messageJson.put("contract", contractName);

        JO params = new JO();
        params.put(Constants.MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_VERIFICATION_REQUEST);
        params.put(Constants.URL_KEY, TESTSERVER_URL);
        params.put(Constants.CHALLENGE_TEXT_KEY, challengeText);
        params.put(Constants.SIGNATURE_KEY, signature);
        messageJson.put("params", params);


        String fullHash = ContractTestHelper.bobPaysContract(messageJson.toJSONString(), IGNIS);
        generateBlock();


        ChildTransaction transaction = findLastChildTransaction(PaymentTransactionType.ORDINARY);
        Assert.assertNotNull(transaction);


        performBasicTransactionChecks(transaction, fullHash);


        JO response = extractMessageObject(transaction);
        Assert.assertNotNull(response);
        Assert.assertEquals(Constants.ERROR_TEXT_INVALID_TOKEN_FORMAT, response.getString("errorDescription"));
        Assert.assertEquals(Constants.ERROR_CODE_INVALID_TOKEN_FORMAT, response.getInt("errorCode"));
    }

    private void checkVerificationInvalidUrlError(String contractName) {
        JO transactionResponse = getChallengeTextResponse("https", contractName);

        String challengeText = transactionResponse.getString(Constants.CHALLENGE_TEXT_KEY);
        String signature = transactionResponse.getString(Constants.SIGNATURE_KEY);
        String token = Token.generateToken(BOB.getSecretPhrase(), challengeText);


        JO messageJson = new JO();
        messageJson.put("contract", contractName);

        JO params = new JO();
        params.put(Constants.MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_VERIFICATION_REQUEST);
        params.put(Constants.URL_KEY, TESTSERVER_URL);
        params.put(Constants.CHALLENGE_TEXT_KEY, challengeText);
        params.put(Constants.SIGNATURE_KEY, signature);
        messageJson.put("params", params);


        String fullHash = ContractTestHelper.bobPaysContract(messageJson.toJSONString(), IGNIS);
        generateBlock();


        ChildTransaction transaction = findLastChildTransaction(PaymentTransactionType.ORDINARY);
        Assert.assertNotNull(transaction);


        performBasicTransactionChecks(transaction, fullHash);


        JO response = extractMessageObject(transaction);
        Assert.assertNotNull(response);
        Assert.assertEquals(Constants.ERROR_TEXT_INVALID_URL, response.getString("errorDescription"));
        Assert.assertEquals(Constants.ERROR_CODE_INVALID_URL, response.getInt("errorCode"));
    }

    private void checkVerificationInvalidSignatureError(String contractName) {
        JO transactionResponse = getChallengeTextResponse(contractName);

        String challengeText = transactionResponse.getString(Constants.CHALLENGE_TEXT_KEY);
        String signature = transactionResponse.getString(Constants.SIGNATURE_KEY);


        JO messageJson = new JO();
        messageJson.put("contract", contractName);

        JO params = new JO();
        params.put(Constants.MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_VERIFICATION_REQUEST);
        params.put(Constants.URL_KEY, TESTSERVER_URL);
        params.put(Constants.CHALLENGE_TEXT_KEY, challengeText);
        params.put(Constants.SIGNATURE_KEY, "0000");
        messageJson.put("params", params);


        String fullHash = ContractTestHelper.bobPaysContract(messageJson.toJSONString(), IGNIS);
        generateBlock();


        ChildTransaction transaction = findLastChildTransaction(PaymentTransactionType.ORDINARY);
        Assert.assertNotNull(transaction);


        performBasicTransactionChecks(transaction, fullHash);


        JO response = extractMessageObject(transaction);
        Assert.assertNotNull(response);
        Assert.assertEquals(Constants.ERROR_TEXT_INVALID_SIGNATURE, response.getString("errorDescription"));
        Assert.assertEquals(Constants.ERROR_CODE_INVALID_SIGNATURE, response.getInt("errorCode"));
    }

    private void checkVerificationUnderpaidError(String contractName) {
        JO transactionResponse = getChallengeTextResponse(contractName);

        String challengeText = transactionResponse.getString(Constants.CHALLENGE_TEXT_KEY);
        String signature = transactionResponse.getString(Constants.SIGNATURE_KEY);

        JO messageJson = new JO();
        messageJson.put("contract", contractName);

        JO params = new JO();
        params.put(Constants.MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_VERIFICATION_REQUEST);
        params.put(Constants.URL_KEY, TESTSERVER_URL);
        params.put(Constants.CHALLENGE_TEXT_KEY, challengeText);
        params.put(Constants.SIGNATURE_KEY, signature);
        messageJson.put("params", params);


        int payAmount = CONTRACT_PRICE / 2;
        String fullHash = ContractTestHelper.bobPaysContract(messageJson.toJSONString(), IGNIS, payAmount);
        generateBlock();


        ChildTransaction transaction = findLastChildTransaction(PaymentTransactionType.ORDINARY);
        Assert.assertNotNull(transaction);


        performBasicTransactionChecks(transaction, fullHash, payAmount);


        JO response = extractMessageObject(transaction);
        Assert.assertNotNull(response);
        Assert.assertEquals(Constants.ERROR_TEXT_UNDERPAID, response.getString("errorDescription"));
        Assert.assertEquals(Constants.ERROR_CODE_UNDERPAID, response.getInt("errorCode"));
    }

    private void checkVerificationWrongAccountError(String contractName) {
        JO transactionResponse = getChallengeTextResponse(contractName, DAVE);

        String challengeText = transactionResponse.getString(Constants.CHALLENGE_TEXT_KEY);
        String signature = transactionResponse.getString(Constants.SIGNATURE_KEY);

        JO messageJson = new JO();
        messageJson.put("contract", contractName);

        JO params = new JO();
        params.put(Constants.MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_VERIFICATION_REQUEST);
        params.put(Constants.URL_KEY, TESTSERVER_URL);
        params.put(Constants.CHALLENGE_TEXT_KEY, challengeText);
        params.put(Constants.SIGNATURE_KEY, signature);
        messageJson.put("params", params);


        ContractTestHelper.bobPaysContract(messageJson.toJSONString(), IGNIS);
        generateBlock();


        ChildTransaction transaction = findLastChildTransaction(PaymentTransactionType.ORDINARY);
        Assert.assertNotNull(transaction);


        JO response = extractMessageObject(transaction);
        Assert.assertNotNull(response);
        Assert.assertEquals(Constants.ERROR_TEXT_WRONG_ACCOUNT, response.getString("errorDescription"));
        Assert.assertEquals(Constants.ERROR_CODE_WRONG_ACCOUNT, response.getInt("errorCode"));
    }

    private void checkVerificationChainError(String contractName) {
        JO messageJson = new JO();
        messageJson.put("contract", contractName);


        String fullHash = ContractTestHelper.bobPaysContract(messageJson.toJSONString(), AEUR);
        generateBlock();


        ChildTransaction transaction = findLastChildTransaction(PaymentTransactionType.ORDINARY);
        Assert.assertNotNull(transaction);


        performBasicTransactionChecks(AEUR, transaction, fullHash);


        JO response = extractMessageObject(transaction);
        Assert.assertNotNull(response);
        Assert.assertEquals(Constants.ERROR_TEXT_WRONG_CHAIN, response.getString("errorDescription"));
        Assert.assertEquals(Constants.ERROR_CODE_WRONG_CHAIN, response.getInt("errorCode"));
    }


    @Ignore // takes more then 1.5 minutes
    @Test
    public void VerificationTimeExpirationTest() {
        JO setupParams = createSetupParams(CONTRACT_PRICE, CONTRACT_CHAIN, 1, CONTRACT_CONTRACT_DOMAIN);
        String contractName = ContractTestHelper.deployContract(IdentityVerification.class, setupParams);

        checkVerificationTimeExpirationError(contractName);
    }

    private void checkVerificationTimeExpirationError(String contractName) {
        JO transactionResponse = getChallengeTextResponse(contractName);

        String challengeText = transactionResponse.getString(Constants.CHALLENGE_TEXT_KEY);
        String signature = transactionResponse.getString(Constants.SIGNATURE_KEY);

        JO messageJson = new JO();
        messageJson.put("contract", contractName);

        JO params = new JO();
        params.put(Constants.MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_VERIFICATION_REQUEST);
        params.put(Constants.URL_KEY, TESTSERVER_URL);
        params.put(Constants.CHALLENGE_TEXT_KEY, challengeText);
        params.put(Constants.SIGNATURE_KEY, signature);
        messageJson.put("params", params);

        try {
            Thread.sleep(90000); // 1.5 minutes
        } catch (InterruptedException e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }

        String fullHash = ContractTestHelper.bobPaysContract(messageJson.toJSONString(), IGNIS);
        generateBlock();


        ChildTransaction transaction = findLastChildTransaction(PaymentTransactionType.ORDINARY);
        Assert.assertNotNull(transaction);


        performBasicTransactionChecks(transaction, fullHash);


        JO response = extractMessageObject(transaction);
        Assert.assertNotNull(response);
        Assert.assertEquals(Constants.ERROR_TEXT_TIME_EXPIRED, response.getString("errorDescription"));
        Assert.assertEquals(Constants.ERROR_CODE_TIME_EXPIRED, response.getInt("errorCode"));
    }


    @Test
    public void verificationRequestTest() {
        String contractName = ContractTestHelper.deployContract(IdentityVerification.class, createSetupParams());

        checkVerificationRequest(contractName);
        checkTransactionOverpaidError(contractName);
    }

    private String checkVerificationRequest(String contractName) {
        return checkVerificationRequest(0, contractName, CONTRACT_PRICE);
    }

    private String checkVerificationRequest(int propertyIndex, String contractName) {
        return checkVerificationRequest(propertyIndex, contractName, CONTRACT_PRICE);
    }

    private String checkVerificationRequest(int propertyIndex, String contractName, int price) {
        JO transactionResponse = getChallengeTextResponse(contractName);

        String challengeText = transactionResponse.getString(Constants.CHALLENGE_TEXT_KEY);
        String signature = transactionResponse.getString(Constants.SIGNATURE_KEY);
        String token = Token.generateToken(BOB.getSecretPhrase(), challengeText);


        TestServer server = new TestServer();
        server.setOneShot(true);
        server.startServer(createTokenText(token));


        JO messageJson = new JO();
        messageJson.put("contract", contractName);

        JO params = new JO();
        params.put(Constants.MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_VERIFICATION_REQUEST);
        params.put(Constants.URL_KEY, TESTSERVER_URL);
        params.put(Constants.CHALLENGE_TEXT_KEY, challengeText);
        params.put(Constants.SIGNATURE_KEY, signature);
        messageJson.put("params", params);


        String fullHash = ContractTestHelper.bobPaysContract(messageJson.toJSONString(), IGNIS, price);
        generateBlock();


        ChildTransaction transaction = findLastChildTransaction(AccountPropertyTransactionType.ACCOUNT_PROPERTY_SET);
        Assert.assertNotNull(transaction);


        Assert.assertEquals(IGNIS.getId(), transaction.getChain().getId());
        Assert.assertEquals(ALICE.getId(), transaction.getSenderId());
        Assert.assertEquals(BOB.getId(), transaction.getRecipientId());
        Assert.assertEquals(fullHash, Convert.toHexString(transaction.getReferencedTransactionId().getFullHash()));


        AccountPropertyAttachment accountPropertyAttachment = (AccountPropertyAttachment) transaction.getAttachment();
        Assert.assertEquals(Constants.PROPERTY_NAME + "[" + propertyIndex + "]", accountPropertyAttachment.getProperty());
        Assert.assertEquals(challengeText, accountPropertyAttachment.getValue());


        return fullHash;
    }

    private void checkTransactionOverpaidError(String contractName) {
        int overpayPrice = CONTRACT_PRICE * 2;


        String requestTransactionHash = checkVerificationRequest(1, contractName, overpayPrice);
        ChildTransaction transaction = findLastChildTransaction(PaymentTransactionType.ORDINARY);
        performBasicTransactionChecks(transaction, requestTransactionHash, overpayPrice - CONTRACT_PRICE);


        JO response = extractMessageObject(transaction);
        Assert.assertNotNull(response);
        Assert.assertEquals(Constants.ERROR_TEXT_OVERPAID, response.getString("errorDescription"));
        Assert.assertEquals(Constants.ERROR_CODE_OVERPAID, response.getInt("errorCode"));
    }


    @Test
    public void propertyTest() {
        String contractName = ContractTestHelper.deployContract(IdentityVerification.class, createSetupParams());

        checkVerificationRequest(contractName);
        checkVerificationRequest(1, contractName);
        createInvalidProperties();
        checkVerificationRequest(2, contractName);
    }

    private void createInvalidProperties() {
        String[] invalidProperties = {"invalidProperty", Constants.PROPERTY_NAME + "[i]"};

        for (String invalidProperty : invalidProperties) {
            SetAccountPropertyCall.create(IGNIS.getId())
                    .property(invalidProperty)
                    .value("dummy")
                    .secretPhrase(ALICE.getSecretPhrase())
                    .recipient(BOB.getRsAccount())
                    .feeNQT(IGNIS.ONE_COIN)
                    .call().toJSONString();
            generateBlock();
        }
    }


    @Test
    public void setupParameterTest() {
        JO setupParams = createSetupParams(-1 * CONTRACT_PRICE, -1 * CONTRACT_CHAIN, -1 * CONTRACT_EXPIRATION_MINUTES, "Identity:Verification");
        String contractName = ContractTestHelper.deployContract(IdentityVerification.class, setupParams);

        checkVerificationRequest(contractName);
    }


    @Test
    public void infoRequestTest() {
        String contractName = ContractTestHelper.deployContract(IdentityVerification.class, createSetupParams());

        checkInfoResponseStructure(contractName);
        checkInfoResponseValues(contractName);
    }

    private void checkInfoResponseStructure(String contractName) {
        JO response = getInfoResponse(contractName);

        Assert.assertEquals(response.getString(Constants.MESSAGE_TYPE_KEY), Constants.MESSAGE_TYPE_INFO_RESPONSE);
        Assert.assertNotNull(response.getString(Constants.VERSION_KEY));
        Assert.assertNotNull(response.getString(Constants.PRICE_KEY));
        Assert.assertNotNull(response.getString(Constants.CHAIN_KEY));
        Assert.assertNotNull(response.getString(Constants.EXPIRATION_MINUTES_KEY));
        Assert.assertNotNull(response.getString(Constants.CONTRACT_DOMAIN_KEY));
        Assert.assertNotNull(response.getArray(Constants.RESOURCES_KEY).toJSONArray().toString());
    }

    private JO getInfoResponse(String contractName) {
        JO messageJson = new JO();
        messageJson.put("contract", contractName);

        JO params = new JO();
        params.put(Constants.MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_INFO_REQUEST);
        messageJson.put("params", params);


        String fullHash = ContractTestHelper.bobPaysContract(messageJson.toJSONString(), IGNIS);
        generateBlock();


        ChildTransaction transaction = findLastChildTransaction(PaymentTransactionType.ORDINARY);
        Assert.assertNotNull(transaction);


        performBasicTransactionChecks(transaction, fullHash);


        return extractMessageObject(transaction, false);
    }


    private void checkInfoResponseValues(String contractName) {
        JO response = getInfoResponse(contractName);

        Assert.assertEquals(Constants.MESSAGE_TYPE_INFO_RESPONSE, response.getString(Constants.MESSAGE_TYPE_KEY));
        Assert.assertEquals(Constants.VERSION, response.getString(Constants.VERSION_KEY));
        Assert.assertEquals(CONTRACT_PRICE, response.getInt(Constants.PRICE_KEY));
        Assert.assertEquals(CONTRACT_CHAIN, response.getInt(Constants.CHAIN_KEY));
        Assert.assertEquals(CONTRACT_EXPIRATION_MINUTES, response.getInt(Constants.EXPIRATION_MINUTES_KEY));
        Assert.assertEquals(CONTRACT_CONTRACT_DOMAIN, response.getString(Constants.CONTRACT_DOMAIN_KEY));
        Assert.assertTrue(response.getArray(Constants.RESOURCES_KEY).toJSONArray().containsAll(Arrays.asList(ResourceFactory.getResourceTypes())));
    }

}

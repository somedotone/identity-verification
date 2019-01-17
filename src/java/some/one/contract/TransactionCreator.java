package some.one.contract;

import nxt.account.Account;
import nxt.addons.JO;
import nxt.addons.TransactionContext;
import nxt.crypto.EncryptedData;
import nxt.http.callers.SendMoneyCall;
import nxt.http.callers.SetAccountPropertyCall;
import nxt.util.Convert;

public class TransactionCreator {


    public static JO createPayment(TransactionContext context, int chain, long recipientId, long amountNQT) {
        return createPayment(context, chain, recipientId, amountNQT, null, false);
    }

    public static JO createPayment(TransactionContext context, long amountNQT, String message) {
        return createPayment(context, context.getTransaction().getChainId(), context.getSenderId(), amountNQT, message, true);
    }

    public static JO createPayment(TransactionContext context, long amountNQT, String message, boolean encrypt) {
        return createPayment(context, context.getTransaction().getChainId(), context.getSenderId(), amountNQT, message, encrypt);
    }

    public static JO createPayment(TransactionContext context, int chain, long recipientId, long amountNQT, String message, boolean encrypt) {
        SendMoneyCall sendMoneyCall = SendMoneyCall.create(chain)
                .recipient(recipientId)
                .amountNQT(amountNQT);

        if(message != null && context != null) {
            if(encrypt) {
                byte[] publicKeyBytes = Account.getPublicKey(recipientId);
                EncryptedData encryptedData = context.getConfig().encryptTo(publicKeyBytes, Convert.toBytes(message, true), true);
                sendMoneyCall.encryptedMessageData(encryptedData.getData())
                        .encryptedMessageNonce(encryptedData.getNonce())
                        .encryptedMessageIsPrunable(true);
            } else {
                sendMoneyCall.message(message).messageIsText(true).messageIsPrunable(true);
            }
        }

        return context.createTransaction(sendMoneyCall);
    }


    public static JO createAccountProperty(TransactionContext context, String propertyName, String propertyValue) {
        return createAccountProperty(context, context.getTransaction().getChainId(), context.getSenderId(), propertyName, propertyValue);
    }

    public static JO createAccountProperty(TransactionContext context, int chain, long recipientId, String propertyName, String propertyValue) {
        SetAccountPropertyCall setAccountPropertyCall = SetAccountPropertyCall.create(chain)
                .recipient(recipientId)
                .property(propertyName)
                .value(propertyValue);

        return context.createTransaction(setAccountPropertyCall);
    }
}

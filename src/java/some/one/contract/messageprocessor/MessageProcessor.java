package some.one.contract.messageprocessor;

import nxt.addons.JO;
import some.one.contract.exception.ContractException;

public abstract class MessageProcessor {
    abstract void validate() throws ContractException;
    abstract void perform() throws ContractException;
    abstract JO response() throws ContractException;


    public final JO process() throws ContractException {
        validate();
        perform();
        return response();
    }
}

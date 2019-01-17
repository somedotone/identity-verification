package some.one.contract.resources;

import some.one.contract.exception.ContractException;

public interface Resource {
    String getToken(String url) throws ContractException;
}

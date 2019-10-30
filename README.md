# IdentityVerification

This repository hosts the source code of the [lightweight contract](https://ardordocs.jelurida.com/Lightweight_Contracts), created to participate in Ardors [online hackathon](https://www.jelurida.com/ardor-hackathon-2018). It should fullfill the requirements from Coding Challenge 3 (Identity Verification), where a contract should be developed to implement a blockchain based reputation system for Identity verification. For informations about [Ardor](https://ardorplatform.org) and the contract system, please follow the links.

## Brief Description

This section gives you a brief description of what the contract offers and how to interact with it. For a detailed technical description or a how to, please have a look at the project [wiki](https://github.com/somedotone/IdentityVerification/wiki). There is a [IdentityVerifiactionClient](https://github.com/somedotone/IdentityVerificationClient) (development) tool written in Java to support you with the interaction with the contract.

### Hackathon Challenge

The original challenge description, posted on [jeluridas](https://www.jelurida.com) hackathon website, is the following:

> Develop a lightweight contract which given a signed token, validates the ownership of a well known account on a centralized service.
>
> The contract should act as an interface between the blockchain and a well known social network, e-commerce site or any other online resource which can establish the credentials of a blockchain account owner.
>
> Users who would like to prove their reputation, will ask the contract for challenge text signed by the contract, and create a token which signs this text using their account passphrase.
>
> The user will then post the signed token text in some public location accessible only to their account. For example to prove ownership of a social network account, post the token text as a message on the social network or as part of your public account description or status. To prove ownership of an e-commerce account, post the token as part of your public shop description.
>
> Next the user will pay the contract Ignis, attaching as a message a public URL which can be used by the contract to verify the existence of the signed token published earlier. The contract will extract the token from the public URL and validate that it is indeed signing the challenge message provided by the contract. If the validation succeeds the contract will set an account property on the sender account confirming its reputation by including the original challenge text as the value.

and is summarized in the following diagram:

![](/assets/sequence-diagram.svg)

### Supported Resources

Five online resource types are supported in this contract version. Three concrete types: Facebook, Twitter, GitHub and two generic types: http and https. The difference between them is how the url is validated. In case of a concrete resource type, the contract checks if the url starts with the url of the online resource. For example if facebook is selected, the contract checks if the announced url starts with "[https://facebook.com/](https://facebook.com/)" or "[https://www.facebook.com/](https://www.facebook.com/)". In case of a generic type, the contract only validates that an url starts with "http://" or "https://" respectively.

### Workflow

1. \(optional\) gathering contract informations:  
There is an infoRequest message implemented to return all necessary informations for the verification process. Just send a payment transaction with at least the fee amount of the returning payment transaction to the contract runner account and attach the following message:  
`{"contract":"IdentityVerification","params":{"type":"infoRequest"}}`
You will get a payment transaction returned including the overpaid amount (amount from infoRequest message - transaction fees) and a public message containing the requested informations. (See [wiki](https://github.com/somedotone/IdentityVerification/wiki) for parameter description)

2. obtaining challenge text:  
To obtain the challenge text, send a payment transaction with at least the fee amount of the returning payment transaction to the contract runner account and attach the following message:  
`{"contract":"IdentityVerification","params":{"accountRS":"<accountRS involved verification>","resources":"<resource type>","type":"challengeTextRequest"}}`  
You will get a payment transaction returned including the overpaid amount (amount from challengeTextRequest message - transaction fees) and a private message containing the challenge text and the signature. (See [wiki](https://github.com/somedotone/IdentityVerification/wiki) for parameter description)

3. creating token:  
Use the challengeText field inside the attached challengeTextResponse message as token generation data and generate the token with the account involved in the verification process. After generation, wrap the token with the following token tag \*\* Ardor Identity Verification Token \*\*. A valid token could look like this:  
** Ardor Identity Verification Token **
a0fkjvcaofrhlboq0lsnpfvnpv8mudogm1353fl3vseug3ihtkv924kmugv7g1g1bel21tt6qkgan3hdcbl9qam8c542euca3dl1v06a8vn0d0fsjalr91vou8vhlc6pgf881r4vprn3uvsg8mpu5rharhpth643
** Ardor Identity Verification Token **

4. requesting verification:  
After posting the token on an online resource, send a payment transaction with at least the verification price to the contract runner account and attach the following message:  
`{"contract":"IdentityVerification","params":{"signature":"<signature value from challengeTextResponse message>","challengeText":"<challenge text value from challengeTextResponse message>","type":"accountVerificationRequest","url":"<url of token>"}}`  
If the token could be claimed and everything went well, the contract runner attaches an account property with the name idVerConChallengeText appended by an index and the challenge text as value. If you've paid more then the price, you'll get a payment transaction including the overpaid price returned.

### Error handling

Whenever an error occurs (except when a message does not trigger the contract), the contract sends back all the funds received from a user account (minus fees for the transaction) and attaches an error message.

## Demonstration

There is a successfully proceed verification process on the Ardor testnet. It links [some.one](https://github.com/somedotone) with its second testnet account ARDOR-4TGA-X2NT-875X-BH5X5. The contract created from the source code inside this repository was uploaded with the transaction identified by its fullHash: 8dde06360b7d4fe4523c2eecaa44a9f6b093e1fe917d2dbf00793751cef1298b.

Following transactions are involved in the verification process:

1. gathering contract informations:  
fullHash (fH) of **infoRequest** transaction: 0947c6921e6dc25174a8d8dcad822583040ecc065ac8e14c263dd65af220dd5c  
fH of **infoResponse** transaction: 5eefb338293ac631e2e47072b79b2c7a3dbffb29ce92619deb0138a6b57278da

2. obtaining challenge text:  
fH of **challengeTextRequest** transaction: da17846067f22c5ef82a94df3772166945312bdfaedc71ea4322586087b6fd14  
shared key: 47927c48ec41892be976605cb5fd8e9bbb35a6bb9edef54b9e4c3e5c62212a5a  
fH of **challengeTextResponse** transaction: 0b6193a141ece61f710bfc88f5e34b497e32fc30213b6852f0d2a62a65d54abc  
shared key: 71ac8d1f3524ddec9cc1cc6feebfa9013105cf8831dea60ea0078ab7d096423e

3. creating token:  
token is posted [here](https://github.com/somedotone/ardor-identity-verification/blob/master/README.md)

4. requesting verification:  
fH of **accountVerificationRequest** transaction:  
e5b2f7c279b60d52d260cd3228780c9e877db61891f9e3aadbfe1e857b060bcc  
fH of **account property** transaction:  
701cab45a45075fe1bad9717714d85f2d46aa471d005b0e316dc036fe118e61e

## Further Information

The contract was created for Ardor version 2.2.1

Have a look at the [informations repo](https://github.com/somedotone/informations)

Have a look at the [wiki](https://github.com/somedotone/IdentityVerification/wiki)

Have a look at our [youtube channel](https://www.youtube.com/channel/UCVvvRL0EmG3yz5Y_ENY-G1A) for an explanation video (english and german)

## Further steps

- updating contract to Ardor version 2.2.5

- creating a Java client library to easily interact with the contract (already in progress)
- creating a user friendly web client (see [here](https://github.com/somedotone/WebClient))
- adapting the contract to let it run with one account on two / multiple nodes (to have a backup runner)
- implementing a proper logging system
- decoupling the verification account from the accountVerificationRequest message to enable outsourcing of message handling. This could lead to services who pay the transaction fees and the price for a verification, so that a user can verify an account by just creating the verification token. A third party, for example, can provide this service for free or charge a user with a fiat currency, so that the user doesn't have to have any cryptocurrencies. Many other scenarios are imaginable.
- stress testing the contract
- rename parameters to improve readability

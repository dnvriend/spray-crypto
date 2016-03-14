# spray-crypto
A simple REST service that encrypts and decrypts payloads using AES. 
Updated on 2016-03-14 to check whether `spray` still works on Akka v2.4.2
and it does!

# Usage
Launch with:

    $ ./activator run
    
# Install httpie
    
    $ brew update
    $ brew install httpie
        
# Encrypt
Send a POST to:

    $ http post http://localhost:8080/crypto/aes/encrypt < encrypt.txt

Result should be:

    HTTP/1.1 200 OK
    Content-Length: 83
    Content-Type: application/json; charset=UTF-8
    Date: Tue, 24 Jun 2014 22:15:01 GMT
    Server: spray-can/1.3.1
    
    {
        "crypto": "AES",
        "response": "QtWBFG/l3syaAKnoocglRAOUmca7eEAdYtcqQxGXl7U="
    }

# Decrypt:
Send a POST to:

    http post http://localhost:8080/crypto/aes/decrypt < decrypt.txt
    
Result should be:

    HTTP/1.1 200 OK
    Content-Length: 53
    Content-Type: application/json; charset=UTF-8
    Date: Tue, 24 Jun 2014 22:15:51 GMT
    Server: spray-can/1.3.1
    
    {
        "crypto": "AES",
        "response": "Hello World!\n"
    }
    
~~Note: Encryption and decryption is a bit slow on my 2009 MacBook Intel Core Duo, so the timeout is set a bit high 
(15 seconds), but it works!~~ My Macbook is a bit faster now (Macbook Pro 2014, 2,5 GHz Intel Core i7, 16GB RAM and 500GB SSD, yes its faster!)

Have fun!
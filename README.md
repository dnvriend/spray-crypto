# spray-crypto
This time with even more crypters! Try replacing crypto=aes with crypto=blow

Launch with:

    $ ./activator run
    
# Install httpie
    
    $ brew update
    $ brew install httpie
        
# Encrypt
Send a POST to:

    $ http post http://localhost:8080/crypto/encrypt crypto=aes msg="Hello World"

Result should be:

    TTP/1.1 200 OK
    Content-Length: 84
    Content-Type: application/json; charset=UTF-8
    Date: Wed, 25 Jun 2014 05:34:52 GMT
    Server: spray-can/1.3.1
    
    {
        "crypto": "aes",
        "encrypted": "0GV5zeP6PqaduI+Vn48r60LZS6pyA8Rl10WYIGPgye8="
    }

# Decrypt:
Send a POST to:

    http post http://localhost:8080/crypto/decrypt crypto=aes msg="GTEg2imgn5CzObt0C1JF4tHYd7ZcZ7N4ZCmkU6eLiiw="
        
Result should be:

    HTTP/1.1 200 OK
    Content-Length: 51
    Content-Type: application/json; charset=UTF-8
    Date: Wed, 25 Jun 2014 05:35:55 GMT
    Server: spray-can/1.3.1
    
    {
       "crypto": "AES",
       "decrypted": "Hello World"
    }
    
Note: Encryption and decryption is a bit slow on my 2009 MacBook Intel Core Duo, so the timeout is set a bit high 
(15 seconds), but it works!

Have fun!
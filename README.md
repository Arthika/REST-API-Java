### REST API Java Examples
This is Arthika's repository for examples, sample code written in Java that demonstrates in a simple way how to use  our REST API.

* pricePolling
* priceStreaming
* orderPolling
* orderStreaming
* positionPolling
* positionStreaming
* Order Execution (Under development)
* Order Cancellation
* Order Replacement
* Authentication

### Pre-requisites:
Will users need previous registration, account, strategy set-up...? After all, these xamples require a pre-existing strategy
JDK, IDE...

### How to:

**1. Clone this repository to the location of your choice.** 

The repository contains all the examples lsted above together with the classes needed. 

**2. Modify the following variables in the Java program you would like to run** 

From here on we will assume it is priceStreaming.java.
```
URL = "http://demo.arthikatrading.com:81/cgi-bin/IHFTRestStreamer/getPrice"
strategy=demo;
securities = "EUR_USD", "GBP_USD"
this.token = token;
this.security = security;
this.tinterface = tinterface;
```
**3. Run the examples using the '.sh' script such as this one:**
```javascript
$ export JARS=lib/commons-codec-1.9.jar:lib/commons-logging-1.2.jar:lib/fluent-hc-4.5.jar:lib/httpclient-4.5.jar:lib/httpclient-cache-4.5.jar:lib/httpclient-win-4.5.jar:lib/httpcore-4.4.1.jar:lib/httpmime-4.5.jar:lib/jackson-all-1.9.9.jar:lib/jna-4.1.0.jar:lib/jna-platform-4.1.0.jar

$ javac -cp $JARS src/priceStreaming.java
```

**4. Execute the sample**
```javascript
$ export JARS=lib/commons-codec-1.9.jar:lib/commons-logging-1.2.jar:lib/fluent-hc-4.5.jar:lib/httpclient-4.5.jar:lib/httpclient-cache-4.5.jar:lib/httpclient-win-4.5.jar:lib/httpcore-4.4.1.jar:lib/httpmime-4.5.jar:lib/jackson-all-1.9.9.jar:lib/jna-4.1.0.jar:lib/jna-platform-4.1.0.jar

$ java -cp $JARS src/priceStreaming

Response timestamp: 1441712414.982956 Contents:
Security: EUR_USD Price: 1.11618 Side: ask Liquidity: 1000000
Security: EUR_USD Price: 1.11613 Side: bid Liquidity: 1000000
Response timestamp: 1441712415.983567 Contents:
Heartbeat!
Response timestamp: 1441712416.194543 Contents:
Security: EUR_USD Price: 1.11618 Side: ask Liquidity: 1000000
Security: EUR_USD Price: 1.11614 Side: bid Liquidity: 500000
```
#### Going further
Using IDE's such as Eclipse or Netbeans


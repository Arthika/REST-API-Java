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
Nota: habría que modificar los .java para que al principio estén las variables a modificar y no tenerlas distribuidas por el código
URL = "http://demo.arthikatrading.com:81/cgi-bin/IHFTRestStreamer/getPrice"
user=fede; strategy=fedenice;
securities = "EUR_USD", "GBP_USD"
this.token = token;
this.security = security;
this.tinterface = tinterface;
```
**3. Run the examples using the '.sh' script such as this one:**
```Java
javac -cp "commons-codec-1.9.jar":"commons-logging-1.2.jar":"fluent-hc-4.5.jar":"gson-2.3.1.jar":"httpclient-4.5.jar":"httpclient-cache-4.5.jar":"httpclient-win-4.5.jar":"httpcore-4.4.1.jar":"httpmime-4.5.jar":"jackson-all-1.9.9.jar":"jna-4.1.0.jar":"jna-platform-4.1.0.jar" priceStreaming.java

java -cp ".":"commons-codec-1.9.jar":"commons-logging-1.2.jar":"fluent-hc-4.5.jar":"gson-2.3.1.jar":"httpclient-4.5.jar":"httpclient-cache-4.5.jar":"httpclient-win-4.5.jar":"httpcore-4.4.1.jar":"httpmime-4.5.jar":"jackson-all-1.9.9.jar":"jna-4.1.0.jar":"jna-platform-4.1.0.jar" priceStreaming
```

**4. Run the script**
```
Arthika $ ./priceStreaming.sh 
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
Using IDE's such as Eclipse of Netbeans


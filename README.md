## REST API Java Examples
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

## Pre-requisites:
Will users need previous registration, account, strategy set-up...? After all, these xamples require a pre-existing strategy
JDK, IDE...

## How to:

1. Clone this repository to the location of your choice.
2. Modify the following variables in the Java program you would like to run, we will assume it is priceStreaming.java:
```
Nota: habría que modificar los .java para que al principio estén las variables a modificar y no tenerlas distribuidas por el código
domain
access_token
account_id
instruments
```
3. Run the examples using the '.sh' script such as this one:
```Java
javac -cp "commons-codec-1.9.jar":"commons-logging-1.2.jar":"fluent-hc-4.5.jar":"gson-2.3.1.jar":"httpclient-4.5.jar":"httpclient-cache-4.5.jar":"httpclient-win-4.5.jar":"httpcore-4.4.1.jar":"httpmime-4.5.jar":"jackson-all-1.9.9.jar":"jna-4.1.0.jar":"jna-platform-4.1.0.jar" priceStreaming.java

java -cp ".":"commons-codec-1.9.jar":"commons-logging-1.2.jar":"fluent-hc-4.5.jar":"gson-2.3.1.jar":"httpclient-4.5.jar":"httpclient-cache-4.5.jar":"httpclient-win-4.5.jar":"httpcore-4.4.1.jar":"httpmime-4.5.jar":"jackson-all-1.9.9.jar":"jna-4.1.0.jar":"jna-platform-4.1.0.jar" priceStreaming
```

4. Modify... Once you are familiar with these ... jump to the more complete Application code in the following repository

Sample Output

EUR_USD
2014-03-21T17:56:09.932922Z
1.37912
1.37923
-------
USD_CAD
2014-03-21T17:56:20.776248Z
1.12011
1.12029
-------
USD_JPY
2014-03-21T17:56:13.668154Z
102.262
102.275


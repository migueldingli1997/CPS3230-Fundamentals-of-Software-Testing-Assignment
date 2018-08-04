# CPS3230 (Fundamentals of Software Testing) Assignment
This is the group submission for the CPS3230 Assignment.

## Note for testing
To run any of the Cucumber Tests or the ModelJUnit test suite,  the webserver needs to be running. To do this, the main method in `webapp.StartJettyHandler` needs to be run. After the server has turned on, the web app is available on `http://localhost:8080`.

Furthermore, chromedriver needs to be installed and placed in the working directory. Normally, this is the root folder. Chromedriver has not been included since it is platform dependent. It can be downloaded from [here](https://sites.google.com/a/chromium.org/chromedriver/). You will also need Google Chrome installed.

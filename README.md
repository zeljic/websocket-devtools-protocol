Create empty dir somewhere and go to the dir:

`mkdir ~/chome-user-dir` and `cd ~/chrome-user-dir`

Run google chrome in remote debugging mode:

`google-chrome --remote-debugging-port=9111 --user-data-dir=.`

Open url `http://127.0.0.1:9111/json/version` and copy `webSocketDebuggerUrl` url to App.java

`URI wsUri = URI.create("ws://127.0.0.1:9111/devtools/browser/07666ea7-dff6-4fe2-8e7f-50b49e953744");`

Run `App.java` main function.

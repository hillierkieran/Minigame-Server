## The Scala.js client

*For those who are brave, foolish, or just really like Scala!*

### If you are on turing:

**On turing**, sbt might have issues getting necessary components through UNE's delightful authenticated web proxy.

To help it get through, these commands should tell sbt on turing to get artifacts from Artifactory on hopper instead:

```sh
mkdir -p ~/.config/coursier/
cp ~cosc250/mirror.properties ~/.config/coursier/
mkdir -p ~/.sbt
cp ~cosc250/repositories ~/.sbt/
```

### If you are in a container

The devcontainer config is set-up to install the Metals extension. But - Metals will try to use "bloop" as a build-server; but because this is a Scala.js project I recommend telling it to use sbt instead:

View -> Command Pallette ... -> Metals: switch build server
and pick "sbt" from the pop-up

Metals will import the build. It'll also try to start an sbt build server.

To connect to it and run sbt commands from the terminal, open a terminal and:

```sh
sbt --java-client
```

That way, it'll connect to the same instance of sbt, rather than trying to start a competing one. 

### Building the code:

From the sbt prompt:

```
deployFast
```

will do a quick compile of the code to JS and put the script in target/compiled.js
which is where index.html will load it from.

```
deployFull
```

takes longer, trying to make the JavaScript file it creates smaller. I don't think you'll really need `deployFull`

To set sbt watching for changes in your code, and automatically producing new JavaScript

```
~ deployFast
```

The "~ " is sbt's shorthand for "watch my code for changes, and run this whenever it changes".

### Loading the code

If you open index.html in your browser, it'll load the compiled code from target/compiled.js

To tell the app where the server is:

* append '?localhost:1234" (or whatever your game server's port is) to the end of the URL.
  In Main.scala, I extract the location of the game server from the "search" part of the URL

  ```scala
  val serverUrl = dom.window.location.search.drop(1)
  ```

### Serving the client from a mini-web server (e.g. from a container)

There's going to be two servers involved: a webserver and our game server

* We're going to run a local webserver to serve up an index.html page and load our client code
* This code is then going to try to connect to whatever server we specify

To start a webserver, we're going to launch a Node.js webserver that'll serve up the current directory:

```sh
npx http-server -p 9001 -c-1
```

`-p` picks a port. On turing, you'll need to find a port that's not in use.
`-c-1` means "don't cache anything" - it sets the server's cache timeout to -1s

When that's running, going to http://localhost:9001/ (or whichever port you picked) will load the client.

**But** - we need to tell the client where the real MinigameNetwork server is. We're going to do that by sticking it in 
a hash fragment after the url.

i.e.

http://localhost:9001/?localhost:51234

the second one is the address of the MinigameNetwork server.

### Why have a Scala.js client

Two reasons:

1. Some people might happen to prefer Scala. (Ok, probably not too many.)
2. Although a container can't run a graphical app, it can run a webserver. i.e. you can run this inside a container and open your web browser from outside the container. That lets those who like devcontainers work from two open windows of VS Code, each running a different container.

### A quick tour

Some top-level objects are available:

* **MainWindow** is the main "window" (mimicking the border-layout window in the Swing client)
* **ClientRegistry** is where you register your game clients

... still being written

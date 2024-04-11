# interview

## Background
I had some background in functional programming using Erlang but http4s and cats effect were totally new to me.


I spent some couple of days reading few websites to get familiar with syntax and motivation of the library.

I used proxy pattern as the main design decision i.e. all call are routed to our container service and response/exceptions are sent back to client.

I didn't add tests to the code due to lack of time at my part, though I prefer writing tests before writing any API.

The code runs in a usual way
```
$ sbt run
```

Kindly make sure to run docker container which provides the proxy server as well.
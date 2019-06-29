# Scala-Zio-Asynchronous-Version
- This is a simple implementation of the elevio article-reviewer app in scala
- It's written in `FP` style (using `scalaz.ZIO` effects system and `Fiber` asynchronous features)

## Requirements
- Please provide `X_API_KEY` environment variable on your system with the value of your account's api-key
- Please provide `AUTHORIZATION` environment variable on your system with the value of your account's authorization token
- Please provide `API_ENDPOINT` environment variable on your system with the value of elevio's api-endpoint route

## Usage
- `$ sbt compile` # for compiling the sources of the project
- `$ sbt test`    # for running the project's test suite
- `$ sbt run`     # for running the main class of the project
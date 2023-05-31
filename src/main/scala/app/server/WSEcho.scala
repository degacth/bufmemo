package app.server

import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}

object WSEcho:
  def echo(implicit mat: Materializer): Flow[Message, Message, Any] = Flow[Message].mapConcat {
    case tm: TextMessage => TextMessage(tm.toString.toUpperCase()) :: Nil
    case bm: BinaryMessage =>
      bm.dataStream.runWith(Sink.ignore)
      Nil
  }

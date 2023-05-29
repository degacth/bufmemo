package app.server

import akka.Done
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route

import scala.concurrent.{ExecutionContext, Future}

class HttpServer(routes: Route)(implicit val as: ActorSystem[_]):
  private implicit val ec: ExecutionContext = as.executionContext
  private type Binding = Future[ServerBinding]

  def serve(host: String, port: Int): Binding = Http().newServerAt(host, port).bind(routes)
  def unbind(binding: Binding): Future[Done] = binding.flatMap(_.unbind())

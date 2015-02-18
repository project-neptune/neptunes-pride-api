package controllers

import controllers.Authentication._
import play.api.libs.json.Json
import play.api.mvc._
import sdk.{NPClient, AuthToken}

import scala.concurrent._

object AuthenticatedAction {
  private def extractToken[T](request: Request[T]): Option[AuthToken] = {
    request.headers.get("X-Auth-Token").map(AuthToken)
  }

  def apply[T](block: (Request[AnyContent], NPClient) => Result): Action[AnyContent] = Action { request =>
    val oToken = extractToken(request)

    oToken match {
      case None =>
        Unauthorized(Json.obj(
          "error" -> Json.obj(
            "message" -> "Login failed."
          )
        ))
      case Some(token) =>
        block(request, new NPClient(token))
    }
  }

  def async[T](block: (Request[AnyContent], NPClient) => Future[Result])(implicit ec: ExecutionContext): Action[AnyContent] = Action.async { request =>
    val oToken = extractToken(request)

    oToken match {
      case None =>
        Future {
          Unauthorized(Json.obj(
            "error" -> Json.obj(
              "message" -> "Login failed."
            )
          ))
        }
      case Some(token) =>
        block(request, new NPClient(token))
    }
  }
}

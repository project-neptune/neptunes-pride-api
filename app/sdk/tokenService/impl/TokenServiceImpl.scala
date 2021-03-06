package sdk.tokenService.impl

import akka.pattern.ask
import akka.util.Timeout
import play.api.Play.current
import play.api.libs.concurrent.Akka
import sdk.exception.InvalidTokenException
import sdk.tokenService.actor.AuthActor
import sdk.tokenService.TokenService
import sdk.{AuthCookie, AuthToken}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object TokenServiceImpl extends TokenService {
  import AuthActor._

  private val authActor = Akka.system.actorOf(AuthActor.props)

  private implicit val timeout = Timeout(2 seconds)

  def getToken(oCookie: Option[AuthCookie])(implicit ec: ExecutionContext): Future[AuthToken] =
    oCookie.map { cookie =>
      (authActor ? GenerateToken(cookie)).mapTo[AuthToken]
    }.getOrElse {
      Future.failed(new InvalidTokenException)
    }

  def lookupCookie(token: AuthToken)(implicit ec: ExecutionContext): Future[AuthCookie] =
    for {
      oCookie <- (authActor ? LookupCookie(token)).mapTo[Option[AuthCookie]]
    } yield {
      oCookie.getOrElse(throw new InvalidTokenException)
    }
}

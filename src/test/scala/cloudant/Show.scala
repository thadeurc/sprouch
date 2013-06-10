package cloudant

import org.scalatest.FunSuite
import akka.dispatch.Future
import spray.json.JsonFormat
import sprouch._
import sprouch.dsl._
import spray.json.JsonWriter

class Show extends FunSuite with CouchSuiteHelpers {
  import JsonProtocol._
  
  test("show functions") {
    implicit val dispatcher = (actorSystem.dispatcher)
        
    withNewDbFuture("db")(implicit dbf => {
      val data = Test(foo=1, bar="foo")
      val designDocContent = DesignDoc(lists = None, shows = Some(Map("asHtml" -> """
          function(doc, req) {
            return {
              body: ('<h1>' + req.query.h + '</h1><ul><li>' + doc.foo + '</li><li>' + doc.bar + '</li></ul>'),
              headers: { 'content-type': 'text/html' }
      			};
          }
      """)))
      val designDoc = new NewDocument("my shows", designDocContent)
      val dl = new SphinxDocLogger("../api-reference/src/api/inc/show")
      for {
        db <- dbf
        view <- db.createDesign(designDoc)
        doc <- data.create
        val query = "h=heading"
        queryRes <- c.withDl(dl) {
          db.show("my shows", "asHtml", doc.id, query)
        }
      } yield {
        assert(queryRes.entity.asString === "<h1>heading</h1><ul><li>1</li><li>foo</li></ul>")
        assert(queryRes.headers.find(_.name.toLowerCase == "content-type").get.value === "text/html")
        
      }
    })
  }
}

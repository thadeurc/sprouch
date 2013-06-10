package cloudant

import org.scalatest.FunSuite
import akka.dispatch.Future
import spray.json.JsonFormat
import sprouch._

class DatabaseMethodsDoc extends FunSuite with CouchSuiteHelpers {
  import JsonProtocol._
  implicit val dispatcher = actorSystem.dispatcher
  
  test("get db") {
    val dl = new SphinxDocLogger("../api-reference/src/api/inc/DbGet")
    await(for {
      _ <- ignoreFailure(c.createDb("db"))
      db <- c.withDl(dl) {
        c.getDb("db")
      }
    } yield {
      assert(db.name === "db")
    })
    
  }
  
  test("get all dbs") {
    val dl = new SphinxDocLogger("../api-reference/src/api/inc/allDbs")
    await(for {
      _ <- ignoreFailure(c.createDb("heyho"))
      all <- c.allDbs()
      
    } yield {
      assert(all.contains("heyho"), "list of all dbs includes heyho")
      assert(all.size > 1, "at least 2 dbs")
    })
  }
  
  test("create db") {
    val dl = new SphinxDocLogger("../api-reference/src/api/inc/DbPut")
    await(for {
      _ <- ignoreFailure(c.deleteDb("db"))
      db <- c.withDl(dl) {
        c.createDb("db")
      }
    } yield {
      assert(db.name === "db")
    })
  }
  
  test("delete db") {
    val dl = new SphinxDocLogger("../api-reference/src/api/inc/DbDelete")
    await(for {
      _ <- ignoreFailure(c.createDb("db"))
      ok <- c.withDl(dl) {
        c.deleteDb("db")
      }
    } yield {
      assert(ok.ok)
    })
  }
  
  test("get all docs") {
    await(for {
      _ <- c.deleteDb("test")
      db <- c.createDb("test")
      docs <- c.withDl(new SphinxDocLogger("../api-reference/src/api/inc/bulkDocs")) {
        db.bulkPut((0 to 2).map(n => NewDocument(randomPerson())))
      }
      newDocs <- c.withDl(new SphinxDocLogger("../api-reference/src/api/inc/bulkDocs2")) {
        db.bulkPut(docs.map(_.updateData(_.copy(gender="female"))))
      }
      all <- c.withDl(new SphinxDocLogger("../api-reference/src/api/inc/allDocs")) {
        db.allDocs[Person](flags = ViewQueryFlag(include_docs = false))
      }
      
    } yield {
      assert(docs.map(_.id).toSet === all.rows.map(_.id).toSet)
    })
    
  }
    
}

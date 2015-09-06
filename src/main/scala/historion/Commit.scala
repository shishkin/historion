package historion

import java.time.ZonedDateTime

case class Sha1(value: String)

case class Author(name: String)

case class Commit(
  id: Sha1,
  timestamp: ZonedDateTime,
  author: Author,
  message: String)

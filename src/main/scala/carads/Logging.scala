package carads

import org.slf4j.LoggerFactory

trait Logging {
  val log = LoggerFactory.getLogger(this.getClass.getName)
}

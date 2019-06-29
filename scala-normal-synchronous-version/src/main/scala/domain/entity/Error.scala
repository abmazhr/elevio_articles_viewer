package domain.entity

object Error {
  case class HttpFetchingAllArticlesError() {
    val cause: String = "Error http fetching all articles"
  }

  case class HttpFetchingArticleError() {
    val cause: String = "Error http fetching article"
  }

  case class HttpFetchingArticleSearchResultError() {
    val cause: String = "Error http fetching article search result"
  }
}

package application.adapters.http_client

import domain.entity.Article.{ArticleObj, ArticleRef, ArticleSearchResultObj}
import domain.entity.Error.{
  HttpFetchingAllArticlesError,
  HttpFetchingArticleError,
  HttpFetchingArticleSearchResultError
}
import zio.UIO

object GenericHttpClient {
  type AllArticlesResult   = UIO[Either[HttpFetchingAllArticlesError, List[ArticleRef]]]
  type ArticleObject       = UIO[Either[HttpFetchingArticleError, ArticleObj]]
  type ArticleSearchResult = UIO[Either[HttpFetchingArticleSearchResultError, List[ArticleSearchResultObj]]]

  trait GetAllArticles {
    val currentPage: Int
    def process: AllArticlesResult
  }

  trait GetArticleById {
    val id: Int
    def process: ArticleObject
  }

  trait GetArticleByKeyword {
    val keyword: String
    def process: ArticleSearchResult
  }
}

package application.adapters.http_client

import domain.entity.Article.{ArticleObj, ArticleRef, ArticleSearchResultObj}
import domain.entity.Error.{
  HttpFetchingAllArticlesError,
  HttpFetchingArticleError,
  HttpFetchingArticleSearchResultError
}

object GenericHttpClient {
  type AllArticlesResult   = Either[HttpFetchingAllArticlesError, List[ArticleRef]]
  type ArticleObject       = Either[HttpFetchingArticleError, ArticleObj]
  type ArticleSearchResult = Either[HttpFetchingArticleSearchResultError, List[ArticleSearchResultObj]]

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

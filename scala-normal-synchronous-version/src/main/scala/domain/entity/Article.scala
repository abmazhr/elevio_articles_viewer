package domain.entity

object Article {
  final case class Author(id: Int, name: String, email: String)
  final case class TranslationObj(summary: Option[String], body: String)

  final case class ArticleRef(
      id: Int,
      title: String,
      notes: Option[String],
      keywords: List[String],
      updated_at: String
  )
  final case class ArticleObj(
      id: Int,
      author: Author,
      created_at: String,
      title: String,
      translations: List[TranslationObj]
  )
  final case class ArticleSearchResultObj(id: Int)
}

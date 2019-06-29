package domain.entity

final case class Config(httpClientHeaders: Map[String, String], apiEndpoint: String)

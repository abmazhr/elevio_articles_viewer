package application.ui

import application.adapters.http_client.sttp_http_client.{
  GetAllArticlesClient,
  GetArticleWithIdClient,
  GetArticleWithKeywordClient
}
import application.utils.MenuUtils
import com.softwaremill.sttp.{Id, SttpBackend}
import domain.entity.Article.{ArticleObj, ArticleRef}
import domain.entity.Config
import domain.entity.Error.{
  HttpFetchingAllArticlesError,
  HttpFetchingArticleError,
  HttpFetchingArticleSearchResultError
}
import zio.DefaultRuntime

import scala.io.StdIn

final case class CLIApp(
    implicit val config: Config,
    implicit val httpBackend: SttpBackend[Id, Nothing],
    implicit val runtime: DefaultRuntime
) {
  val std: StdIn.type                       = scala.io.StdIn
  val invalidChoiceMessage: String          = "Error => Invalid choice, Please try again ."
  val invalidFetchingArticleMessage: String = "Error => Couldn't fetch the article, Please try again later ."

  def mainView: CLIApp = {
    val menuChoicesNumber: Int = 3
    val viewMenuLines = List(
      "#####################################################################################",
      "#                                Article Viewer App                                 #",
      "#####################################################################################",
      "# 1) Get all articles for this account [X_API_KEY & AUTHORIZATION env variables]    #",
      "# 2) Get all articles with a keyword   [X_API_KEY & AUTHORIZATION env variables]    #",
      "# 3) Exit the app                                                                   #",
      "#####################################################################################"
    )

    println(MenuUtils.getMenuWireFrame(viewMenuLines))
    print("⇝ ")
    val choiceString: String = std.readLine()

    try {
      val choiceInt: Int = choiceString.toInt
      if (choiceInt > 0 && choiceInt <= menuChoicesNumber) {
        MenuUtils.clearScreen()
        choiceInt match {
          case 1 => this.getAllArticlesView()
          case 2 => this.getAllArticlesWithKeywordView
          case 3 => println("Exiting ..."); sys.exit()
        }
      } else {
        clearScreenWithErrorMessage(invalidChoiceMessage)
        this.mainView
      }
    } catch {
      case _: NumberFormatException => clearScreenWithErrorMessage(invalidChoiceMessage); this.mainView
      case _: Throwable             => sys.exit()
    }

    this
  }

  def getAllArticlesView(cachedArticleRefs: List[ArticleRef] = List.empty): CLIApp = {
    if (cachedArticleRefs.isEmpty) {
      val viewMenuHeaderLines = List(
        "#####################################################################################",
        "#                              FETCHING ALL ARTICLES                                #",
        "#####################################################################################"
      )

      println(MenuUtils.getMenuWireFrame(viewMenuHeaderLines, trimLastEndLine = true))

      runtime.unsafeRun(GetAllArticlesClient().process.currentResult) match {
        case Left(err: HttpFetchingAllArticlesError) =>
          println(s"Error => ${err.cause}")
          print("⇝ Press any key to go back")
          std.readLine()
          MenuUtils.clearScreen()
          mainView

        case Right(articleRefs) =>
          if (articleRefs.isEmpty) {
            val viewMenuHeaderLines = List(
              "#####################################################################################",
              "#                   SORRY THERE ARE NO ARTICLES IN YOUR ACCOUNT                     #",
              "#####################################################################################"
            )

            println(MenuUtils.getMenuWireFrame(viewMenuHeaderLines, trimLastEndLine = true))
            print("⇝ Press any key to go back")
            std.readLine()
            MenuUtils.clearScreen()
            mainView
          } else this.getAllArticlesView(articleRefs)
      }
    } else {
      val viewMenuHeaderLines = List(
        "#####################################################################################",
        "#                         ALL ARTICLES IN YOUR ACCOUNT                              #",
        "#####################################################################################"
      )

      println(MenuUtils.getMenuWireFrame(viewMenuHeaderLines, trimLastEndLine = true))
    }

    val menuChoicesNumber: Int = 2
    val viewMenuRestOfLines =
      cachedArticleRefs.map { articleRef =>
        s"[#${articleRef.id}] ${articleRef.title}"
      } ::: List(
        "#####################################################################################",
        "# 1) View article by it's id                                                        #",
        "# 2) Go back to main menu view                                                      #",
        "#####################################################################################"
      )

    println(MenuUtils.getMenuWireFrame(viewMenuRestOfLines))
    print("⇝ ")
    val choiceString: String = std.readLine()

    try {
      val choiceInt: Int = choiceString.toInt
      if (choiceInt > 0 && choiceInt <= menuChoicesNumber) {
        choiceInt match {
          case 1 => this.getArticleWithId(cachedArticleRefs.map(_.id), cachedArticleRefs)
          case 2 => MenuUtils.clearScreen(); this.mainView
        }
      } else {
        clearScreenWithErrorMessage(invalidChoiceMessage)
        this.getAllArticlesView(cachedArticleRefs)
      }
    } catch {
      case _: NumberFormatException =>
        clearScreenWithErrorMessage(invalidChoiceMessage); this.getAllArticlesView(cachedArticleRefs)
      case _: Throwable => sys.exit()
    }

    this
  }

  def getAllArticlesWithKeywordView: CLIApp = {
    print("⇝ keyword: ")
    val keyword = std.readLine()
    val viewMenuHeaderLines = List(
      "#####################################################################################",
      "#                              FETCHING ALL ARTICLES                                #",
      "#####################################################################################"
    )

    println(MenuUtils.getMenuWireFrame(viewMenuHeaderLines, trimLastEndLine = true))

    def innerLoop(cachedArticleObjects: List[ArticleObj] = List.empty): Unit = {
      if (cachedArticleObjects.isEmpty) {
        runtime.unsafeRun(GetArticleWithKeywordClient(keyword).process.currentResult) match {
          case Left(err: HttpFetchingArticleSearchResultError) =>
            println(s"Error => ${err.cause}")
            print("⇝ Press any key to go back")
            std.readLine()
            MenuUtils.clearScreen()
            mainView

          case Right(searchResultObjs) =>
            val result = searchResultObjs.map { searchResultObj =>
              runtime.unsafeRun(GetArticleWithIdClient(searchResultObj.id).process.currentResult) match {
                case Right(articleObj) => articleObj
              }
            }
            if (result.isEmpty) {
              val viewMenuHeaderLines = List(
                "#####################################################################################",
                "#                   SORRY THERE ARE NO MATCHES FOR THE KEYWORD                      #",
                "#####################################################################################"
              )

              println(MenuUtils.getMenuWireFrame(viewMenuHeaderLines, trimLastEndLine = true))
              print("⇝ Press any key to go back")
              std.readLine()
              MenuUtils.clearScreen()
              mainView
            } else innerLoop(result)
        }
      } else {
        val viewMenuHeaderLines = List(
          "#####################################################################################",
          "#                      ALL ARTICLES WITH YOUR KEYWORD                               #",
          "#####################################################################################"
        )

        println(MenuUtils.getMenuWireFrame(viewMenuHeaderLines, trimLastEndLine = true))
      }

      val menuChoicesNumber: Int = 2
      val viewMenuRestOfLines =
        cachedArticleObjects.zipWithIndex.map { articleObjWithIndex =>
          s"[#${articleObjWithIndex._2 + 1}] ${articleObjWithIndex._1.title}"
        } ::: List(
          "#####################################################################################",
          "# 1) View article by it's id                                                        #",
          "# 2) Go back to main menu view                                                      #",
          "#####################################################################################"
        )

      println(MenuUtils.getMenuWireFrame(viewMenuRestOfLines))
      print("⇝ ")
      val choiceString: String = std.readLine()

      try {
        val choiceInt: Int = choiceString.toInt
        if (choiceInt > 0 && choiceInt <= menuChoicesNumber) {
          choiceInt match {
            case 1 =>
              print("⇝ [article id]:: #")
              val _choiceString: String = std.readLine()
              val _choiceInt: Int       = _choiceString.toInt
              if (_choiceInt > 0 && _choiceInt <= cachedArticleObjects.length) {
                val article: ArticleObj = cachedArticleObjects(_choiceInt - 1)
                val aggregatedSummary =
                  article.translations.foldLeft("")(
                    (acc, obj) => acc.concat(obj.body.substring(0, 60).concat("..."))
                  )
                val viewMenuLines = List(
                  "#####################################################################################",
                  s"                       ${article.title}                                             ",
                  "#####################################################################################",
                  s"Title:        ${article.title}                                                      ",
                  s"Author Name:  ${article.author.name}                                                ",
                  s"Author Email: ${article.author.email}                                               ",
                  s"Summary:      $aggregatedSummary                                                    ",
                  s"Created At:   ${article.created_at}                                                 ",
                  "#####################################################################################"
                )

                println(MenuUtils.getMenuWireFrame(viewMenuLines))
                print("⇝ Press any key to go back")
                std.readLine()
                MenuUtils.clearScreen()
                innerLoop(cachedArticleObjects)
              } else clearScreenWithErrorMessage(invalidChoiceMessage); innerLoop(cachedArticleObjects)

            case 2 => MenuUtils.clearScreen(); this.mainView
          }
        } else {
          clearScreenWithErrorMessage(invalidChoiceMessage)
          innerLoop(cachedArticleObjects)
        }
      } catch {
        case _: NumberFormatException =>
          clearScreenWithErrorMessage(invalidChoiceMessage); innerLoop(cachedArticleObjects)
        case _: Throwable => sys.exit()
      }
    }

    innerLoop()

    this
  }

  def getArticleWithId(ids: List[Int], articleRefs: List[ArticleRef]): CLIApp = {
    print("⇝ [article id]:: #")
    val choiceString: String = std.readLine()

    try {
      val choiceInt: Int = choiceString.toInt
      if (ids.contains(choiceInt)) {
        val viewMenuHeaderLines = List(
          "#####################################################################################",
          "#                            FETCHING THE ARTICLE                                   #",
          "#####################################################################################"
        )

        println(MenuUtils.getMenuWireFrame(viewMenuHeaderLines, trimLastEndLine = true))

        runtime.unsafeRun(GetArticleWithIdClient(choiceInt).process.currentResult) match {
          case Left(err: HttpFetchingArticleError) =>
            println(s"Error => ${err.cause}")
            print("⇝ Press any key to go back")
            std.readLine()
            MenuUtils.clearScreen()
            mainView

          case Right(article) =>
            val aggregatedSummary =
              article.translations.foldLeft("")(
                (acc, obj) => acc.concat(obj.body.substring(0, 60).concat("..."))
              )
            val viewMenuLines = List(
              "#####################################################################################",
              s"                       ${article.title}                                             ",
              "#####################################################################################",
              s"Title:        ${article.title}                                                      ",
              s"Author Name:  ${article.author.name}                                                ",
              s"Author Email: ${article.author.email}                                               ",
              s"Summary:      $aggregatedSummary                                                    ",
              s"Created At:   ${article.created_at}                                                 ",
              "#####################################################################################"
            )

            println(MenuUtils.getMenuWireFrame(viewMenuLines))
            print("⇝ Press any key to go back")
            std.readLine()
            this.getAllArticlesView(articleRefs)
        }
      } else {
        println(invalidChoiceMessage)
        this.getArticleWithId(ids, articleRefs)
      }
    } catch {
      case _: NumberFormatException => println(invalidChoiceMessage); this.getArticleWithId(ids, articleRefs)
      case _: Throwable             => sys.exit()
    }

    this
  }

  private def clearScreenWithErrorMessage(errorMessage: String): Unit = {
    MenuUtils.clearScreen()
    println(errorMessage)
  }
}

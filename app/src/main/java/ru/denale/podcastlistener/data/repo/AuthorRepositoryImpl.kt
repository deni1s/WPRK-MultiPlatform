package ru.denale.podcastlistener.data.repo

import ru.denale.podcastlistener.data.Author
import ru.denale.podcastlistener.data.repo.source.AuthorDataSource
import io.reactivex.Single

class AuthorRepositoryImpl(val dataSource: AuthorDataSource) : AuthorRepository {
    override fun getAuthors(offset: Int, limit: Int): Single<List<Author>> =
        dataSource.getAuthors(offset, limit)

    override fun getAuthors(): Single<List<Author>> = dataSource.getAuthors()

    private fun getAuthorsMock(): Single<List<Author>> {
        val authorsList = mutableListOf<Author>()
        for (i in 0..30) {
            authorsList.add(
                Author(
                    i.toString(),
                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSuOf7P4PmewSFiIuZGwdojnbPq_FK3kggoKISHvxQ&s",
                    "Волк"
                )
            )
        }
        return Single.just(authorsList)
    }
}
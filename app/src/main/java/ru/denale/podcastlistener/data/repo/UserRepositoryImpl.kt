package ru.denale.podcastlistener.data.repo

import ru.denale.podcastlistener.data.User
import ru.denale.podcastlistener.data.repo.source.UserDataSource
import io.reactivex.Single

class UserRepositoryImpl(private val userDataSource: UserDataSource) : UserRepository {
    override fun getUserInfo(): Single<User> {
        return userDataSource.getUserInfo()
    }
}
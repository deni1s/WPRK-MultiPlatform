package ru.denale.podcastlistener.data.repo.source

import ru.denale.podcastlistener.data.User
import io.reactivex.Single

interface UserDataSource {
    fun getUserInfo(): Single<User>
}
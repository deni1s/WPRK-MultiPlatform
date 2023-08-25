package ru.denale.podcastlistener.data.repo

import ru.denale.podcastlistener.data.User
import io.reactivex.Single

interface UserRepository {
    fun getUserInfo(): Single<User>
}
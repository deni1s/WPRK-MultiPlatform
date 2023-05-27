package ge.denale.podcastlistener.data.repo

import ge.denale.podcastlistener.data.User
import io.reactivex.Single

interface UserRepository {
    fun getUserInfo(): Single<User>
}
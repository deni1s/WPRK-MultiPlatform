package ge.denale.podcastlistener.data.repo.source

import ge.denale.podcastlistener.data.User
import io.reactivex.Single

interface UserDataSource {
    fun getUserInfo(): Single<User>
}
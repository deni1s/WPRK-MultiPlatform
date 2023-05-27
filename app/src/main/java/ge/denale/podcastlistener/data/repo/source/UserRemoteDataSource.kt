package ge.denale.podcastlistener.data.repo.source

import ge.denale.podcastlistener.data.User
import ge.denale.podcastlistener.services.http.ApiService
import io.reactivex.Single

class UserRemoteDataSource(val apiService: ApiService) : UserDataSource {
    override fun getUserInfo(): Single<User> {
        return apiService.getUserInfo()
    }
}
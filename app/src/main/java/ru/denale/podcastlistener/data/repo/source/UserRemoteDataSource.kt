package ru.denale.podcastlistener.data.repo.source

import ru.denale.podcastlistener.data.User
import ru.denale.podcastlistener.services.http.ApiService
import io.reactivex.Single

class UserRemoteDataSource(val apiService: ApiService) : UserDataSource {
    override fun getUserInfo(): Single<User> {
        return apiService.getUserInfo()
    }
}
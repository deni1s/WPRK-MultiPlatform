package ru.denale.podcastlistener.feature.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.denale.podcastlistener.R
import ru.denale.podcastlistener.common.EXTRA_AUTHOR_ID_KEY_DATA
import ru.denale.podcastlistener.common.EXTRA_GENRE_ID_KEY_DATA
import ru.denale.podcastlistener.common.EXTRA_MUSIC_TYPE
import ru.denale.podcastlistener.common.SCREEN_PODCAST_ID_DATA
import ru.denale.podcastlistener.common.SCREEN_TITLE_DATA
import ru.denale.podcastlistener.common.setupWithNavController
import ru.denale.podcastlistener.feature.activities.musics.MusicsActivity
import ru.denale.podcastlistener.feature.activities.playmusic.PlayMusic2
import ru.denale.podcastlistener.services.AUTHOR_ID_KEY
import ru.denale.podcastlistener.services.CATEGORY_ID_KEY
import ru.denale.podcastlistener.services.PODCAST_ID_KEY
import ru.denale.podcastlistener.services.WAVE_ID_KEY

const val ENTERANCE_COUNT = "enterance_count"
const val USER_ID_KEY = "user_id"
const val FIRST_ADD_ENTERANCE_COUNT = -1

class MainActivity : AppCompatActivity() {
    private var currentNavController: LiveData<NavController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            initializeMainActivity()
        } // Else, need to wait for onRestoreInstanceState
        tryOpenNotification()
    }

    private fun tryOpenNotification() {
        intent.extras?.let {  bundle ->
            bundle.getString(CATEGORY_ID_KEY)?.let {
                startActivity(Intent(this, MusicsActivity::class.java).apply {
                    putExtra(SCREEN_TITLE_DATA, bundle.getString(SCREEN_TITLE_DATA) ?: "Категория")
                    putExtra(EXTRA_GENRE_ID_KEY_DATA, it)
                })
            }
            bundle.getString(AUTHOR_ID_KEY)?.let {
                startActivity(Intent(this, MusicsActivity::class.java).apply {
                    putExtra(SCREEN_TITLE_DATA, bundle.getString(SCREEN_TITLE_DATA) ?: "Автор")
                    putExtra(EXTRA_AUTHOR_ID_KEY_DATA, it)
                })
            }
            bundle.getString(WAVE_ID_KEY)?.let {
                startActivity(Intent(this, PlayMusic2::class.java).apply {
                    putExtra(EXTRA_MUSIC_TYPE, it)
                })
            }
            bundle.getString(PODCAST_ID_KEY)?.let {
                startActivity(Intent(this, PlayMusic2::class.java).apply {
                    putExtra(SCREEN_PODCAST_ID_DATA, it)
                })
            }
        }
    }

    private fun initializeMainActivity() {
        setupBottomNavigationBar()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Now that BottomNavigationBar has restored its instance state
        // and its selectedItemId, we can proceed with setting up the
        // BottomNavigationBar with Navigation
        setupBottomNavigationBar()
    }

    /**
     * Called on first creation and when restoring state.
     */
    private fun setupBottomNavigationBar() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationMain)

        val navGraphIds = listOf(R.navigation.home, R.navigation.category, R.navigation.profile)

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = bottomNavigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host,
            intent = intent
        )
        bottomNavigationView.isVisible = false

        currentNavController = controller
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }
}
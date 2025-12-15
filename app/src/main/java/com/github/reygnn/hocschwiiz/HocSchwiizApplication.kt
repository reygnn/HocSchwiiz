package com.github.reygnn.hocschwiiz

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application-Klasse für Hilt Dependency Injection.
 * Wird beim App-Start vom Android-System instanziiert.
 *
 * Nicht zu verwechseln mit [MainContent] (Root-Composable der UI).
 */
@HiltAndroidApp
class HocSchwiizApplication : Application()
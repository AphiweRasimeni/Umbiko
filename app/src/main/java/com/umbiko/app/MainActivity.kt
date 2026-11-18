package com.umbiko.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.umbiko.app.di.LocalAppContainer
import com.umbiko.app.ui.nav.Routes
import com.umbiko.app.ui.nav.UmbikoNavGraph
import com.umbiko.app.ui.theme.UmbikoTheme
import com.umbiko.app.util.LocaleManager
import kotlinx.coroutines.launch

/**
 * Single-activity host. It also:
 *  - decides the start destination (noticeboard vs login),
 *  - shows a biometric lock screen when "Unlock with biometrics" is on,
 *  - asks for the Android 13+ notification permission,
 *  - applies the user's chosen language whenever the saved settings change.
 *
 * Extends AppCompatActivity (a ComponentActivity) so per-app languages work on
 * Android 12 and below and BiometricPrompt has the FragmentActivity it needs.
 *
 * Reference: Android Developers (2025) Show a biometric authentication dialog.
 * https://developer.android.com/identity/sign-in/biometric-auth [Accessed 5 November 2026].
 */
class MainActivity : AppCompatActivity() {

    private var unlocked by mutableStateOf(true)

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.i(TAG, "POST_NOTIFICATIONS granted=$granted")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as UmbikoApplication).container
        val loggedIn = container.repository.isLoggedIn
        val startDestination = if (loggedIn) Routes.HOME else Routes.LOGIN

        val needsUnlock = loggedIn && container.tokenManager.biometricUnlock && biometricAvailable()
        // Survives rotation / language change so the user isn't re-prompted for those.
        unlocked = savedInstanceState?.getBoolean(KEY_UNLOCKED) ?: !needsUnlock
        if (!unlocked) promptBiometric()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        lifecycleScope.launch { container.repository.syncSessionExtras() }
        lifecycleScope.launch {
            container.repository.observeSettings().collect { settings ->
                settings?.let { LocaleManager.apply(it.languageCode) }
            }
        }

        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                UmbikoTheme {
                    if (unlocked) {
                        UmbikoNavGraph(startDestination = startDestination)
                    } else {
                        LockedScreen(onUnlock = ::promptBiometric)
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_UNLOCKED, unlocked)
    }

    private fun biometricAvailable(): Boolean =
        BiometricManager.from(this).canAuthenticate(AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS

    private fun promptBiometric() {
        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    Log.i(TAG, "Biometric unlock succeeded")
                    unlocked = true
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Log.w(TAG, "Biometric error $errorCode: $errString")
                }
            },
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.unlock_title))
            .setSubtitle(getString(R.string.unlock_subtitle))
            .setAllowedAuthenticators(AUTHENTICATORS)
            .build()
        prompt.authenticate(info)
    }

    private companion object {
        const val TAG = "MainActivity"
        const val KEY_UNLOCKED = "unlocked"
        const val AUTHENTICATORS = BIOMETRIC_WEAK or DEVICE_CREDENTIAL
    }
}

@Composable
private fun LockedScreen(onUnlock: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.padding(bottom = 16.dp))
        Text(stringResource(R.string.unlock_title), style = MaterialTheme.typography.titleLarge)
        Button(onClick = onUnlock, modifier = Modifier.padding(top = 24.dp)) {
            Text(stringResource(R.string.unlock_button))
        }
    }
}

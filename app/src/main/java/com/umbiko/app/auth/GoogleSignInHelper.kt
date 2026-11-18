package com.umbiko.app.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.umbiko.app.BuildConfig

/**
 * Single sign-on with Google (PoE requirement). Shows the system account picker and
 * returns a Google ID token, which the Umbiko API verifies at POST /auth/google.
 *
 * Reference: Android Developers (2025) Sign in your user with Credential Manager.
 * https://developer.android.com/identity/sign-in/credential-manager-siwg [Accessed 5 November 2026].
 */
object GoogleSignInHelper {
    private const val TAG = "GoogleSignIn"

    suspend fun getIdToken(context: Context): Result<String> {
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        return try {
            val credential = CredentialManager.create(context).getCredential(context, request).credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                Result.success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
            } else {
                Log.w(TAG, "Unexpected credential type: ${credential.type}")
                Result.failure(IllegalStateException("Unexpected credential type"))
            }
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Google sign-in failed: ${e.message}")
            Result.failure(e)
        }
    }
}

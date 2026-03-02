package com.kaltura.kflow.presentation.socialLogin

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.kaltura.client.types.APIException
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.Base64

class SocialLoginViewModel(private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {
    val googleSignInEvent= MutableLiveData<Resource<String>>()

    fun initiateGoogleSignInProcess(webClientID: String, context: Context) {

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(webClientID)
            .setNonce(generateSecureRandomNonce())
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        viewModelScope.launch {
            val e = signIn(request, context)
            if (e is NoCredentialException) {
                val googleIdOptionFalse: GetGoogleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientID)
                    .setNonce(generateSecureRandomNonce())
                    .build()

                val requestFalse: GetCredentialRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOptionFalse)
                    .build()

                val tryRequestFalse = signIn(requestFalse, context)
                if(tryRequestFalse != null) {
                    googleSignInEvent.postValue(Resource.Error(createAPIException(tryRequestFalse)))
                }
            } else if(e != null)
                googleSignInEvent.postValue(Resource.Error(createAPIException(e)))
        }
    }
    fun initiateGoogleChooseAccountSignInProcess(webClientID: String, context: Context) {

        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption
            .Builder(serverClientId = webClientID)
            .setNonce(generateSecureRandomNonce())
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        viewModelScope.launch {
            val e = signIn(request, context)
            if (e is NoCredentialException) {
                val googleIdOptionFalse: GetGoogleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientID)
                    .setNonce(generateSecureRandomNonce())
                    .build()

                val requestFalse: GetCredentialRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOptionFalse)
                    .build()

                val tryRequestFalse = signIn(requestFalse, context)
                if(tryRequestFalse != null) {
                    googleSignInEvent.postValue(Resource.Error(createAPIException(tryRequestFalse)))
                }
            } else if(e != null)
                googleSignInEvent.postValue(Resource.Error(createAPIException(e)))
        }
    }
    suspend fun signIn(request: GetCredentialRequest, context: Context): Exception? {
        val credentialManager = CredentialManager.create(context)
        val failureMessage = "Sign in failed!"
        var e: Exception? = null
        //using delay() here helps prevent NoCredentialException when the BottomSheet Flow is triggered
        //on the initial running of our app
        Thread.sleep(250)
        try {
            // The getCredential is called to request a credential from Credential Manager.
            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )
            Log.i(TAG, result.toString())
            getGoogleToken(result.credential)
            Toast.makeText(context, "Sign in successful!", Toast.LENGTH_SHORT).show()
            return e

        } catch (e: GetCredentialException) {
            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
            Log.e(TAG, failureMessage + ": Failure getting credentials", e)
            return e

        } catch (e: GoogleIdTokenParsingException) {
            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
            Log.e(TAG, failureMessage + ": Issue with parsing received GoogleIdToken", e)
            return e

        } catch (e: NoCredentialException) {
            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
            Log.e(TAG, failureMessage + ": No credentials found", e)
            return e

        } catch (e: GetCredentialCustomException) {
            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
            Log.e(TAG, failureMessage + ": Issue with custom credential request", e)
            return e

        } catch (e: GetCredentialCancellationException) {
            Toast.makeText(context, ": Sign-in cancelled", Toast.LENGTH_SHORT).show()
            Log.e(TAG, failureMessage + ": Sign-in was cancelled", e)
            return e
        }

    }
    private fun getGoogleToken(credential: Credential) {
        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                Log.i(TAG, "Google Token :$idToken")

                googleSignInEvent.postValue(Resource.Success(idToken))
                // Send this ID token to your backend server for verification
            } catch (e: Exception) {
                Log.e(TAG, "Error: Can't parse Google Token", e)
                googleSignInEvent.postValue(Resource.Error(createAPIException(e)))
            }
        }
    }
    fun generateSecureRandomNonce(byteLength: Int = 32): String {
        val randomBytes = ByteArray(byteLength)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SecureRandom.getInstanceStrong().nextBytes(randomBytes)
            return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
        }
        return ""
    }
    fun createAPIException(objectException: Exception) : APIException{
        var resultException = APIException()

        resultException.setMessage(objectException.message)
        resultException.stackTrace = objectException.stackTrace

        return resultException
    }
}
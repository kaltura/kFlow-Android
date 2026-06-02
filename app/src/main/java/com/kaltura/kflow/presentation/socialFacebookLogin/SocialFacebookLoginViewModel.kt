package com.kaltura.kflow.presentation.socialFacebookLogin
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.kaltura.client.types.APIException
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource

class SocialFacebookLoginViewModel (private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {
    val facebookSignInEvent= MutableLiveData<Resource<String>>()

    fun initiateFacebookSignInProcess(facebookAppID: String,facebookSecret: String,context: Context, callbackManager: CallbackManager?) {

        FacebookSdk.setApplicationId(facebookAppID)
        FacebookSdk.setClientToken(facebookSecret)
        FacebookSdk.sdkInitialize(context)
        val loginManager = LoginManager.getInstance()
        // Register the Facebook callback (receives LoginResult / error / cancel)
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val token: AccessToken = result.accessToken
                // 1) Send token.token to your backend to exchange for session/JWT
                // 2) Or use it to call Facebook Graph API
                facebookSignInEvent.postValue(Resource.Success(token.token.toString()))
            }
            override fun onCancel() {
                // User cancelled — show a friendly message or stay on screen
                facebookSignInEvent.postValue(Resource.Error(createAPIException(Exception("User cancelled"))))
            }
            override fun onError(error: FacebookException) {
                // Network denied, key hash invalid, or user error.
                // Log and show a message
                facebookSignInEvent.postValue(Resource.Error(createAPIException(error)))
            }
        })
        if (getTokenStatus())
            facebookSignInEvent.postValue(Resource.Success(AccessToken.getCurrentAccessToken()?.token.toString()))
         else
            facebookSignInEvent.postValue(Resource.Error(createAPIException(Exception("Login Required"))))
    }

    fun createAPIException(objectException: Exception): APIException {
        val resultException = APIException()

        resultException.setMessage(objectException.message)
        resultException.stackTrace = objectException.stackTrace

        return resultException
    }
    fun getTokenStatus(): Boolean{
        val accessToken = AccessToken.getCurrentAccessToken()
        return accessToken != null && !accessToken.isExpired
    }

}
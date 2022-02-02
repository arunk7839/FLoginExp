package com.c1ctech.floginexp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.facebook.login.LoginResult
import com.c1ctech.floginexp.databinding.ActivityMainBinding
import java.util.*
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.facebook.*
import org.json.JSONObject
import org.json.JSONException
import com.facebook.GraphResponse
import com.facebook.GraphRequest

class MainActivity : AppCompatActivity() {

    lateinit var accessTokenTracker: AccessTokenTracker
    lateinit var callbackManager: CallbackManager

    lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)

        // initializes the Facebook SDK
        FacebookSdk.sdkInitialize(applicationContext)

        setContentView(activityMainBinding.root)

        // create an instance of callbackManager
        // It manages the callbacks into the FacebookSdk from onActivityResult()'s method.
        callbackManager = CallbackManager.Factory.create()

        // set the permissions to use when the user logs in.
        activityMainBinding.loginButton.setPermissions(listOf("email", "public_profile"))

        // Registers a login callback to the given callback manager.
        activityMainBinding.loginButton.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult?> {

                // called when the dialog completes without error.
                override fun onSuccess(result: LoginResult?) {
                    Log.e("message: ", "success")

                }

                // called when the dialog is canceled.
                override fun onCancel() {
                    Log.e("message: ", "onCancel")
                }

                // called when the dialog finishes with an error.
                override fun onError(exception: FacebookException) {
                    Log.e("message: ", exception.localizedMessage)
                }
            })

        accessTokenTracker = object : AccessTokenTracker() {

            // The method that will be called with the access token changes.
            override fun onCurrentAccessTokenChanged(
                oldAccessToken: AccessToken?,
                currentAccessToken: AccessToken?
            ) {
                // condition is true when user logs out
                if (currentAccessToken == null) {
                    activityMainBinding.userName.text = ""
                    activityMainBinding.userEmail.text = ""
                    Toast.makeText(this@MainActivity, "LogOut", Toast.LENGTH_SHORT).show()
                } else {
                    // getting information regarding the logged-in person
                    loadUserProfile(currentAccessToken)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        // stops tracking the current access token
        accessTokenTracker.stopTracking()

    }

    private fun loadUserProfile(currentAccessToken: AccessToken) {
        // creates a new Request configured to retrieve a user's own profile.
        val request = GraphRequest.newMeRequest(
            currentAccessToken, object : GraphRequest.GraphJSONObjectCallback {
                override fun onCompleted(`object`: JSONObject?, response: GraphResponse?) {

                    Log.d("TAG", `object`.toString())

                    try {
                        val first_name = `object`!!.getString("first_name")
                        val last_name = `object`!!.getString("last_name")
                        val email = `object`!!.getString("email")

                        activityMainBinding.userName.setText("$first_name $last_name")
                        activityMainBinding.userEmail.setText(email)

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            })

        val parameters = Bundle()
        parameters.putString("fields", "first_name,last_name,email,id")

        // set parameters for this request.
        request.parameters = parameters

        // Executes the request asynchronously.
        request.executeAsync()
    }
}


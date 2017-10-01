package com.rapsealk.mobilesw

import android.support.v7.app.AppCompatActivity

import android.content.Intent
import android.os.Bundle
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*

import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseAuthListener: FirebaseAuth.AuthStateListener? = null

    private var mGoogleApiClient: GoogleApiClient? = null

    private val GOOGLE_SIGN_IN_CODE = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseAuthListener = FirebaseAuth.AuthStateListener() { auth: FirebaseAuth ->
            var user: FirebaseUser? = auth.currentUser
            if (user != null) {
                // var intent = Intent(this, MainActivity::class.java)
                // startActivity(intent)
                finish()
            } else {

            }
        }

        // Google Auth
        var gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, object : GoogleApiClient.OnConnectionFailedListener {
                    override fun onConnectionFailed(result: ConnectionResult) {
                        //
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        google_login_button.setOnClickListener { view ->
            var signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN_CODE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == GOOGLE_SIGN_IN_CODE) {
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent)
            if (result.isSuccess) {
                var account: GoogleSignInAccount? = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        var credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
        mFirebaseAuth?.signInWithCredential(credential)!!
                .addOnCompleteListener(this, object : OnCompleteListener<AuthResult> {
                    override fun onComplete(task: Task<AuthResult>) {
                        if (task.isSuccessful) {
                            var user: FirebaseUser? = mFirebaseAuth?.currentUser
                            // TODO
                            toast(user?.displayName + "님 환영합니다.")
                            // var intent = Intent(this@LoginActivity, MainActivity::class.java)
                            // startActivity(intent)
                            finish()
                        } else {
                            toast("Authentication failed")
                        }
                    }
                })
    }
}

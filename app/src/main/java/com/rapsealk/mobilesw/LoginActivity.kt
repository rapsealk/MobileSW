package com.rapsealk.mobilesw

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.android.gms.auth.api.Auth
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

    private var progressDialog: ProgressDialog? = null

    override fun onStart() {
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        progressDialog = ProgressDialog(this)
        progressDialog?.isIndeterminate = true
        progressDialog?.setProgressStyle(ProgressDialog.STYLE_SPINNER)

        // Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseAuthListener = FirebaseAuth.AuthStateListener { auth: FirebaseAuth ->
            var user: FirebaseUser? = auth.currentUser
            if (user != null) {
                // TODO
            } else {
                // TODO
            }
        }

        // FIXME : MOVE TO REGISTER_ACTIVITY & DisplayName
        btnRegister.setOnClickListener { v: View? ->
            progressDialog?.setMessage("회원가입 중")
            progressDialog?.show()
            var email = editTextEmail.text.toString()
            var password = editTextPassword.text.toString()
            mFirebaseAuth?.createUserWithEmailAndPassword(email, password)
                    ?.addOnCompleteListener(this, object : OnCompleteListener<AuthResult> {
                        override fun onComplete(task: Task<AuthResult>) {
                            progressDialog?.dismiss()
                            if (task.isSuccessful()) {
                                toast("회원가입이 완료되었습니다.")
                                btnLogin.performClick()
                            } else {
                                toast("회원가입에 실패했습니다.")
                            }
                        }
                    })
        }

        btnLogin.setOnClickListener { v: View? ->
            progressDialog?.setMessage("로그인 중")
            progressDialog?.show()
            var email = editTextEmail.text.toString()
            var password = editTextPassword.text.toString()
            mFirebaseAuth?.signInWithEmailAndPassword(email, password)
                    ?.addOnCompleteListener(this, object : OnCompleteListener<AuthResult> {
                        override fun onComplete(task: Task<AuthResult>) {
                            progressDialog?.dismiss()
                            if (task.isSuccessful()) {
                                welcomeUser(mFirebaseAuth?.currentUser)
                            } else {
                                toast("로그인에 실패했습니다.")
                            }
                        }
                    })
        }

        // Google Auth
        var gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, object : GoogleApiClient.OnConnectionFailedListener {
                    override fun onConnectionFailed(result: ConnectionResult) {
                        // TODO
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        google_login_button.setOnClickListener { view ->
            progressDialog?.setMessage("구글 로그인 중")
            progressDialog?.show()
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
                        progressDialog?.dismiss()
                        if (task.isSuccessful) {
                            welcomeUser(mFirebaseAuth?.currentUser)
                        } else {
                            toast("Authentication failed")
                        }
                    }
                })
    }

    private fun welcomeUser(user: FirebaseUser?) {
        toast(user?.displayName + "님 환영합니다.")
        var intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

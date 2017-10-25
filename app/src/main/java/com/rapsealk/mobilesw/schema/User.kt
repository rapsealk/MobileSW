package com.rapsealk.mobilesw.schema

/**
 * Created by rapsealk on 2017. 10. 26..
 */
data class User (
        val uid: String,
        val email: String,
        val emailVerified: Boolean,
        val displayName: String,
        val photoURL: String,
        val disabled: Boolean,
        val metadata: Metadata,
        val providerData: ArrayList<ProviderData>
)

data class Metadata (
        val lastSignInTime: String,
        val creationTime: String
)

data class ProviderData (
        val uid: String,
        val displayName: String,
        val email: String,
        val photoURL: String,
        val providerId: String
)
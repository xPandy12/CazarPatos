package com.marcosalazar.cazarpatos

import android.app.Activity
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class EncryptedSharedPreferencesManager (val actividad: Activity): FileHandler{
    val masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    val sharedPreferences = EncryptedSharedPreferences.create(
        "secret_shared_prefs",
        masterKeyAlias,
        actividad,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    override fun SaveInformation(datosAGrabar:Pair<String,String>){

        val editor = sharedPreferences.edit()
        editor.putString(LOGIN_KEY , datosAGrabar.first)
        editor.putString(PASSWORD_KEY, datosAGrabar.second)
        editor.apply()
    }
    override fun ReadInformation():Pair<String,String>{
        val email = sharedPreferences.getString(LOGIN_KEY,"").toString()
        val clave = sharedPreferences.getString(PASSWORD_KEY,"").toString()
        return (email to clave)
    }
}
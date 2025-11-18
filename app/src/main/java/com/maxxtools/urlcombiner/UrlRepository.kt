package com.maxxtools.urlcombiner // Bitte durch deinen echten Paketnamen ersetzen

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Diese Klasse ist für das Speichern, Laden und Verwalten der
 * Liste von Basis-URLs in den SharedPreferences verantwortlich.
 */
class UrlRepository(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("UrlCombinerPreferences", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val keyUrls = "baseUrlList"

    /**
     * Ruft die Liste der gespeicherten Basis-URLs ab.
     * @return Eine Liste von [BaseUrlItem]-Objekten. Gibt eine leere Liste zurück, wenn nichts gespeichert ist.
     */
    fun getUrls(): List<BaseUrlItem> {
        val json = sharedPreferences.getString(keyUrls, null)
        return if (json != null) {
            val type = object : TypeToken<List<BaseUrlItem>>() {}.type
            gson.fromJson(json, type)
        } else {
            // Wenn noch nichts gespeichert ist, eine leere Liste zurückgeben.
            emptyList()
        }
    }

    /**
     * Speichert eine gegebene Liste von Basis-URLs.
     * @param urls Die Liste von [BaseUrlItem]-Objekten, die gespeichert werden soll.
     */
    fun saveUrls(urls: List<BaseUrlItem>) {
        val json = gson.toJson(urls)
        sharedPreferences.edit().putString(keyUrls, json).apply()
    }
}
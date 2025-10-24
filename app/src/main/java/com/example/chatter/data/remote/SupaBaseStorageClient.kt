package com.example.chatter.data.remote

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

class SupaBaseStorageClient(private val context: Context) {
    companion object {
        const val BUCKET_NAME = "chatter_images"
    }

    private val url = "https://nnmunfptcaijujubpdif.supabase.co"
    private val key =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5ubXVuZnB0Y2FpanVqdWJwZGlmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA1NDU5MTYsImV4cCI6MjA3NjEyMTkxNn0.CTzrGw-K6sLbRc1uJCy5H_4Kr0KGUgrw2r6xKoNCbAM"

    val supabase = createSupabaseClient(
        url, key
    ) {
        install(Storage)
    }

    suspend fun uploadImage(uri: Uri): String? {
        try {
            val extension = uri.path?.substringAfterLast(".") ?: "jpg"
            val fileName = "${System.currentTimeMillis()}.$extension"
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            supabase.storage.from(BUCKET_NAME).upload(fileName, inputStream.readBytes())
            return supabase.storage.from(BUCKET_NAME).publicUrl(fileName)
        } catch (e: Exception){
            e.printStackTrace()
            return null
        }

    }


}
package edu.vt.cs.cs5254.gallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import edu.vt.cs.cs5254.gallery.api.FlickrApi
import edu.vt.cs.cs5254.gallery.api.FlickrResponse
import edu.vt.cs.cs5254.gallery.api.GalleryItem
import edu.vt.cs.cs5254.gallery.api.PhotoResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "FlickrFetchr"

object FlickrFetchr {
    private val flickrApi: FlickrApi
    var responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()

    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        flickrApi = retrofit.create(FlickrApi::class.java)
    }

    // Using a boolean allows us to use the same function for loading and reload
    // if true, it will bypass the check knowing it's a reload request
    // else if false it will know it's a load request and will check if the LiveData is empty
    fun fetchPhotos(reload : Boolean){

        if(!reload && responseLiveData.value != null){
            return
        }

        val flickrRequest: Call<FlickrResponse> = flickrApi.fetchPhotos()
        flickrRequest.enqueue(object : Callback<FlickrResponse> {
            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
                Log.e(TAG, "Failed to fetch photos", t)
            }

            override fun onResponse(
                call: Call<FlickrResponse>,
                response: Response<FlickrResponse>
            ) {
                Log.d(TAG, "Response received: ${response.body()}")
                val flickrResponse: FlickrResponse? = response.body()
                val photoResponse: PhotoResponse? = flickrResponse?.photos
                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems
                    ?: mutableListOf()
                galleryItems = galleryItems.filterNot {
                    it.url.isBlank()
                }
                responseLiveData.value = galleryItems
            }
        })
        Log.d(TAG, "Response Live Data: $responseLiveData")
    }


    @WorkerThread
    fun fetchPhoto(url: String): Bitmap? {
        val response: Response<ResponseBody> = flickrApi.fetchUrlBytes(url).execute()
        val bitmap = response.body()?.byteStream()?.use(BitmapFactory::decodeStream)
        Log.i(TAG, "Decoded Bitmap = $bitmap from Response: $response")
        return bitmap
    }
}
package edu.vt.cs.cs5254.gallery.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface FlickrApi {
    @GET("services/rest/?method=flickr.interestingness.getList" +
            "&api_key=cc27ac96c9c94c9e15bb3152e28d569a\n" +
            "\n" +
            "&format=json" +
            "&nojsoncallback=1" +
            "&extras=url_s,geo")
    fun fetchPhotos(): retrofit2.Call<FlickrResponse>

    @GET
    fun fetchUrlBytes(@Url url : String):retrofit2.Call<ResponseBody>
}
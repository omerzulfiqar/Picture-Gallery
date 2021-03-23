package edu.vt.cs.cs5254.gallery.api

import com.google.gson.annotations.SerializedName
import edu.vt.cs.cs5254.gallery.api.GalleryItem

class PhotoResponse {
    @SerializedName("photo")
    lateinit var galleryItems : List<GalleryItem>
}
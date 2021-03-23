package edu.vt.cs.cs5254.gallery

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import edu.vt.cs.cs5254.gallery.api.GalleryItem

private const val TAG = "PhotoGalleryViewModel"

class PhotoGalleryViewModel : ViewModel() {
    val galleryItemsLiveData: LiveData<List<GalleryItem>> = FlickrFetchr.responseLiveData

    // Loading and reloading handler
    fun loadPhotos(reload : Boolean) {
        if(reload) Log.d(TAG, "Reloading...")
        FlickrFetchr.fetchPhotos(reload)
    }

}
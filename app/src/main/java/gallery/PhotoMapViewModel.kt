package edu.vt.cs.cs5254.gallery

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.vt.cs.cs5254.gallery.api.GalleryItem
private const val TAG = "PhotoMapViewModel"

class PhotoMapViewModel : ViewModel() {
    private val galleryItemsLiveData: LiveData<List<GalleryItem>> = FlickrFetchr.responseLiveData

    // Keeping an eye on galleryItemsLiveData
    // Creating a Map of the ID -> GalleryItem and wrapping it as Mutable Live Map Data
    val geoGalleryItemMapLiveData: LiveData<Map<String, GalleryItem>> =
        Transformations.switchMap(galleryItemsLiveData) { items ->
            val geoGalleryItemMap =
                items.filterNot { it.latitude == "0" && it.longitude == "0" }.associateBy { it.id }
            MutableLiveData<Map<String, GalleryItem>>(geoGalleryItemMap)
        }

    // Load and Reload Handler
    fun loadPhotos(reload: Boolean) {
        if (reload) Log.d(TAG, "Reloading...")
        FlickrFetchr.fetchPhotos(reload)
    }
}


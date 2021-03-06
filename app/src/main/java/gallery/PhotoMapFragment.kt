package edu.vt.cs.cs5254.gallery

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import edu.vt.cs.cs5254.gallery.api.GalleryItem

private const val TAG = "PhotoMapFragment"

class PhotoMapFragment : MapViewFragment(), GoogleMap.OnMarkerClickListener {

    //View Fields
    private lateinit var photoMapViewModel: PhotoMapViewModel
    private lateinit var thumbnailDownloader: ThumbnailDownloader<Marker>
    var geoGalleryItemMap = emptyMap<String, GalleryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val responseHandler = Handler()
        photoMapViewModel =
            ViewModelProvider(this@PhotoMapFragment).get(PhotoMapViewModel::class.java)
        thumbnailDownloader = ThumbnailDownloader(responseHandler) { marker, bitmap ->
            setMarkerIcon(marker, bitmap)
        }
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateMapView(
            inflater,
            container,
            savedInstanceState,
            R.layout.fragment_photo_map,
            R.id.map_view
        )
        viewLifecycleOwner.lifecycle.addObserver(thumbnailDownloader.viewLifecycleObserver)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onMapViewCreated(
            view,
            savedInstanceState
        ) { googleMap ->
            googleMap.setOnMarkerClickListener(this@PhotoMapFragment)
            updateUI()
        }

        photoMapViewModel.geoGalleryItemMapLiveData.observe(
            viewLifecycleOwner,
            Observer { galleryItemsMap ->
                geoGalleryItemMap = galleryItemsMap
                Log.d(TAG, "Have gallery items from PhotoMapViewModel ${geoGalleryItemMap.values}")
                // Calling updateUI() again easily allows the observer to note the changes after pressing the reload button
                updateUI()
            })
    }

    // Creating menu options
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_gallery, menu)

        val reloadOption = menu.findItem(R.id.menu_reload)
        reloadOption.apply {
            isVisible = true
            isEnabled = true
            Log.d(TAG, "Reload Option Created!")
        }
    }

    // Handling reload option selection
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_reload -> {
                photoMapViewModel.loadPhotos(true)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewLifecycleOwner.lifecycle.removeObserver(thumbnailDownloader.viewLifecycleObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }

    private fun updateUI() {
        // if the fragment is not currently added to its activity, or
        // if there are not gallery items, do not update the UI
        // if the map has not been initialized, the app will crash, so to prevent that we must return to the gallery
        if (!isAdded || geoGalleryItemMap.isEmpty() || !super.mapIsInitialized()) {
            return
        }

        Log.i(TAG, "The Map has " + geoGalleryItemMap.size + " items")

        // remove all markers, overlays, etc. from the map
        googleMap.clear()

        val bounds = LatLngBounds.Builder()
        for (item in geoGalleryItemMap.values) {
            // logging the information of each gallery item with a valid lat-lng
            Log.i(
                TAG, "Item id=${item.id} "
                        + "lat=${item.latitude} long=${item.longitude} "
                        + "title=${item.title}"
            )
            // create a lan-lng point for the item and add it to the lat-lng bounds
            val itemPoint = LatLng(item.latitude.toDouble(), item.longitude.toDouble())
            bounds.include(itemPoint)

            // create a marker for the item and add it to the map
            val itemMarker = MarkerOptions().position(itemPoint).title(item.title)
            val marker = googleMap.addMarker(itemMarker)
            marker.tag = item.id
            // placing the thumbnail on the map by queuing it to the respective marker
            thumbnailDownloader.queueThumbnail(marker, item.url)
        }

        Log.i(TAG, "Expecting ${geoGalleryItemMap.size} markers on the map")
    }

    // Marker Click Handler
    override fun onMarkerClick(marker: Marker?): Boolean {
        val itemID = marker?.tag as String
        Log.d(TAG, "Marker $itemID was clicked")
        val item = geoGalleryItemMap.get(itemID)
        val itemUri = item?.photoPageUri
        return if (itemUri == null) false
        else {
            val intent = PhotoPageActivity.newIntent(requireContext(), itemUri)
            startActivity(intent)
            true
        }
    }
}
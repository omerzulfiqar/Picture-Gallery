package edu.vt.cs.cs5254.gallery

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs.cs5254.gallery.api.GalleryItem

private const val TAG = "PhotoGalleryFragment"

class PhotoGalleryFragment : Fragment() {

    // View Fields
    private lateinit var photoRecyclerView: RecyclerView
    private lateinit var photoGalleryViewModel: PhotoGalleryViewModel
    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        retainInstance = true
        val responseHandler = Handler()
        photoGalleryViewModel =
            ViewModelProvider(this@PhotoGalleryFragment).get(PhotoGalleryViewModel::class.java)
        photoGalleryViewModel.loadPhotos(false)
        thumbnailDownloader = ThumbnailDownloader(responseHandler) { PhotoHolder, bitmap ->
            val drawable = BitmapDrawable(resources, bitmap)
            PhotoHolder.bindDrawable(drawable)
        }
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_gallery, container, false)
        viewLifecycleOwner.lifecycle.addObserver(thumbnailDownloader.viewLifecycleObserver)
        // initializing my recycler view
        photoRecyclerView = view.findViewById(R.id.photo_recycler_view)
        photoRecyclerView.layoutManager = GridLayoutManager(context, 3)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoGalleryViewModel.galleryItemsLiveData.observe(
            viewLifecycleOwner,
            Observer { galleryItems ->
                Log.d(TAG, "Got the following  gallery items from ViewModel: $galleryItems")
                // Eventually, update data backing the recycler view
                photoRecyclerView.adapter = PhotoAdapter(galleryItems)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_reload -> {
                photoGalleryViewModel.loadPhotos(true)
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

    // View Holder
    private inner class PhotoHolder(itemImageView: ImageView) :
        RecyclerView.ViewHolder(itemImageView), View.OnClickListener {
        private lateinit var galleryItem: GalleryItem

        init {
            itemView.setOnClickListener(this)
        }

        val bindDrawable: (Drawable) -> Unit = itemImageView::setImageDrawable

        fun bindGalleryItem(item: GalleryItem) {
            galleryItem = item
        }

        override fun onClick(v: View?) {
            val intent = PhotoPageActivity.newIntent(requireContext(), galleryItem.photoPageUri)
            startActivity(intent)
        }
    }

    // View Adapter
    private inner class PhotoAdapter(private val galleryItems: List<GalleryItem>) :
        RecyclerView.Adapter<PhotoHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            val view =
                layoutInflater.inflate(R.layout.list_item_gallery, parent, false) as ImageView
            return PhotoHolder(view)
        }

        override fun getItemCount(): Int {
            return galleryItems.size
        }

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val galleryItem = galleryItems[position]
            holder.bindGalleryItem(galleryItem)
            // Using a blank white image as the placeholder image
            val placeHolder: Drawable =
                ContextCompat.getDrawable(requireContext(), R.drawable.blank)
                    ?: ColorDrawable()
            holder.bindDrawable(placeHolder)
            thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
        }

    }
}





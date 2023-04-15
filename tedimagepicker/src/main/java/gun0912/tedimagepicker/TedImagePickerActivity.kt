package gun0912.tedimagepicker

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firefly.fire_rx.subscribeOnIOAndObserveOnMain
import com.firefly.viewutils.gone
import com.firefly.viewutils.visible
import com.google.android.material.appbar.MaterialToolbar
import com.gun0912.tedonactivityresult.model.ActivityResult
import com.tedpark.tedonactivityresult.rx2.TedRxOnActivityResult
import gun0912.tedimagepicker.adapter.AlbumAdapter
import gun0912.tedimagepicker.adapter.GridSpacingItemDecoration
import gun0912.tedimagepicker.adapter.MediaAdapter
import gun0912.tedimagepicker.adapter.SelectedMediaAdapter
import gun0912.tedimagepicker.base.BaseRecyclerViewAdapter
import gun0912.tedimagepicker.base.FastScroller
import gun0912.tedimagepicker.builder.TedImagePickerBaseBuilder
import gun0912.tedimagepicker.builder.type.*
import gun0912.tedimagepicker.extenstion.setLock
import gun0912.tedimagepicker.extenstion.toggle
import gun0912.tedimagepicker.model.Album
import gun0912.tedimagepicker.model.Media
import gun0912.tedimagepicker.util.GalleryUtil
import gun0912.tedimagepicker.util.MediaUtil
import gun0912.tedimagepicker.util.TextFormatUtil
import gun0912.tedimagepicker.util.ToastUtil
import io.reactivex.disposables.Disposable
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


internal class TedImagePickerActivity : AppCompatActivity() {

    private val albumAdapter by lazy { AlbumAdapter(builder) }
    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var selectedMediaAdapter: SelectedMediaAdapter

    private lateinit var builder: TedImagePickerBaseBuilder<*>

    private lateinit var disposable: Disposable

    private var selectedPosition = 0
    private var mSelectedAlbum: Album? = null
    private var mIsAlbumOpened: Boolean = false

    private lateinit var mToolbar: MaterialToolbar
    private lateinit var mViewDoneTop: Button
    private lateinit var mViewDoneBottom: Button
    private lateinit var mLayoutContent: ViewGroup
    private lateinit var mRecyclerViewMedia: RecyclerView
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mLayoutSelectedAlbumDropDown: ViewGroup
    private lateinit var mDropdownIcon: ImageView
    private lateinit var mRecyclerViewAlbum: RecyclerView
    private lateinit var mRecyclerViewAlbumDropDown: RecyclerView
    private lateinit var mRecyclerViewSelectedMedia: RecyclerView
    private lateinit var mFastScroller: FastScroller
    private lateinit var mViewSelectedMedia: FrameLayout
    private lateinit var mViewSelectedAlbum: LinearLayout
    private lateinit var mSelectedAlbumDropdownImageCount: TextView
    private lateinit var mImageCount: TextView
    private lateinit var mSelectedAlbumName: TextView
    private lateinit var mSelectedAlbumDropDownAlbumName: TextView
    private lateinit var mViewBottom: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSavedInstanceState(savedInstanceState)
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            requestedOrientation = builder.screenOrientation
        }
        startAnimation()

        setContentView(R.layout.activity_ted_image_picker)

        mToolbar = findViewById(R.id.toolbar)
        mViewDoneTop = findViewById(R.id.view_done_top)
        mViewDoneBottom = findViewById(R.id.view_done_bottom)
        mLayoutContent = findViewById(R.id.layout_content)
        mRecyclerViewMedia = mLayoutContent.findViewById(R.id.rv_media)
        mDrawerLayout = findViewById(R.id.drawer_layout)
        mLayoutSelectedAlbumDropDown = findViewById(R.id.layout_selected_album_drop_down)
        mDropdownIcon = mLayoutSelectedAlbumDropDown.findViewById(R.id.dropdown_icon)
        mRecyclerViewAlbum = findViewById(R.id.rv_album)
        mRecyclerViewAlbumDropDown = findViewById(R.id.rv_album_drop_down)
        mRecyclerViewSelectedMedia = mLayoutContent.findViewById(R.id.rv_selected_media)
        mFastScroller = mLayoutContent.findViewById(R.id.fast_scroller)
        mViewSelectedMedia = mLayoutContent.findViewById(R.id.view_selected_media)
        mViewSelectedAlbum = findViewById(R.id.view_selected_album)
        mSelectedAlbumDropdownImageCount = mLayoutSelectedAlbumDropDown.findViewById(R.id.image_count)
        mImageCount = findViewById(R.id.image_count)
        mSelectedAlbumName = findViewById(R.id.selected_album_name)
        mSelectedAlbumDropDownAlbumName = mLayoutSelectedAlbumDropDown.findViewById(R.id.album_name)
        mViewBottom = findViewById(R.id.view_bottom)

        mLayoutSelectedAlbumDropDown.gone()
        mViewSelectedAlbum.gone()
        mRecyclerViewAlbumDropDown.gone()
        mDropdownIcon.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp)

        setupToolbar()
        setupTitle()
        setupRecyclerView()
        setupListener()
        setupSelectedMediaView()
        setupButton()
        setupAlbumType()
        loadMedia()
    }

    private fun startAnimation() {
        if (builder.startEnterAnim != null && builder.startExitAnim != null) {
            overridePendingTransition(builder.startEnterAnim!!, builder.startExitAnim!!)
        }
    }

    private fun setupToolbar() {
        mToolbar.setNavigationOnClickListener { finish() }

    }

    private fun setupTitle() {
        val title = builder.title ?: getString(builder.titleResId)
        mToolbar.title = title
    }

    private fun setupButton() {
        mViewDoneTop.text = builder.buttonText ?: getString(builder.buttonTextResId)
        mViewDoneBottom.text = builder.buttonText ?: getString(builder.buttonTextResId)

        if(builder.buttonDrawableOnly){
            mViewDoneBottom.visible()
            mViewDoneTop.visible()

            mViewDoneTop.gone()
            mViewDoneBottom.gone()
        } else {
            mViewDoneBottom.gone()
            mViewDoneTop.gone()

            mViewDoneTop.visible()
            mViewDoneBottom.visible()
        }

        setupButtonVisibility()
    }

    private fun setupButtonVisibility() {
        val showButton = if(builder.selectType == SelectType.SINGLE){
            false
        } else {
            mediaAdapter.selectedUriList.isNotEmpty()
        }

        if(showButton && builder.buttonGravity == ButtonGravity.TOP){
            mViewDoneTop.visible()
        } else {
            mViewDoneTop.gone()
        }

        if(showButton && builder.buttonGravity == ButtonGravity.BOTTOM){
            mViewDoneBottom.visible()
        } else {
            mViewDoneBottom.gone()
        }
    }

    private fun loadMedia(isRefresh: Boolean = false) {
        disposable = GalleryUtil.getMedia(this, builder.mediaType)
            .subscribeOnIOAndObserveOnMain()
            .subscribe { albumList: List<Album> ->
                albumAdapter.replaceAll(albumList)
                setSelectedAlbum(selectedPosition)
                if (!isRefresh) {
                    setSelectedUriList(builder.selectedUriList)
                }
                mRecyclerViewMedia.visible()

            }
    }

    private fun setSelectedUriList(uriList: List<Uri>?) =
        uriList?.forEach { uri: Uri -> onMultiMediaClick(uri) }

    private fun setSavedInstanceState(savedInstanceState: Bundle?) {

        val bundle: Bundle? = when {
            savedInstanceState != null -> savedInstanceState
            else -> intent.extras
        }

        builder = bundle?.getParcelable(EXTRA_BUILDER)
            ?: TedImagePickerBaseBuilder<TedImagePickerBaseBuilder<*>>()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(EXTRA_BUILDER, builder)
        super.onSaveInstanceState(outState)
    }

    private fun setupRecyclerView() {
        setupAlbumRecyclerView()
        setupMediaRecyclerView()
        setupSelectedMediaRecyclerView()
    }


    private fun setupAlbumRecyclerView() {

        val albumAdapter = albumAdapter.apply {
            onItemClickListener = object : BaseRecyclerViewAdapter.OnItemClickListener<Album> {
                override fun onItemClick(data: Album, itemPosition: Int, layoutPosition: Int) {
                    this@TedImagePickerActivity.setSelectedAlbum(itemPosition)
                    mDrawerLayout.close()
                    mDropdownIcon.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp)
                    mRecyclerViewAlbumDropDown.gone()
                }
            }
        }
        mRecyclerViewAlbum.run {
            adapter = albumAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    mDrawerLayout.setLock(newState == RecyclerView.SCROLL_STATE_DRAGGING)
                }
            })
        }

        mRecyclerViewAlbumDropDown.adapter = albumAdapter

    }

    private fun setupMediaRecyclerView() {
        mediaAdapter = MediaAdapter(this, builder).apply {
            onItemClickListener = object : BaseRecyclerViewAdapter.OnItemClickListener<Media> {
                override fun onItemClick(data: Media, itemPosition: Int, layoutPosition: Int) {
                    mRecyclerViewAlbumDropDown.gone()
                    mDropdownIcon.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp)
                    this@TedImagePickerActivity.onMediaClick(data.uri)
                }

                override fun onHeaderClick() {
                    onCameraTileClick()
                }
            }

            onMediaAddListener = {
                mRecyclerViewSelectedMedia.smoothScrollToPosition(selectedMediaAdapter.itemCount)
            }

        }

        mRecyclerViewMedia.run {
            layoutManager = GridLayoutManager(this@TedImagePickerActivity, IMAGE_SPAN_COUNT)
            addItemDecoration(GridSpacingItemDecoration(IMAGE_SPAN_COUNT, 8))
            itemAnimator = null
            adapter = mediaAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    (layoutManager as? LinearLayoutManager)?.let {
                        val firstVisiblePosition = it.findFirstCompletelyVisibleItemPosition()
                        if (firstVisiblePosition <= 0) {
                            return
                        }
                        val media = mediaAdapter.getItem(firstVisiblePosition)
                        val dateString = SimpleDateFormat(
                            builder.scrollIndicatorDateFormat,
                            Locale.getDefault()
                        ).format(Date(TimeUnit.SECONDS.toMillis(media.dateAddedSecond)))
                        mFastScroller.setBubbleText(dateString)
                    }
                }
            })
        }

        mFastScroller.recyclerView = mRecyclerViewMedia

    }

    private fun setupSelectedMediaRecyclerView() {
        if(builder.selectType == SelectType.MULTI){
            mRecyclerViewSelectedMedia.visible()
        } else {
            mRecyclerViewSelectedMedia.gone()
        }

        selectedMediaAdapter = SelectedMediaAdapter().apply {
            onClearClickListener = { uri ->
                onMultiMediaClick(uri)
            }
        }
        mRecyclerViewSelectedMedia.run {
            layoutManager = LinearLayoutManager(
                this@TedImagePickerActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = selectedMediaAdapter

        }
    }

    @SuppressLint("CheckResult")
    private fun onCameraTileClick() {
        val cameraMedia = when(builder.mediaType){
            MediaType.IMAGE -> CameraMedia.IMAGE
            MediaType.VIDEO -> CameraMedia.VIDEO
            MediaType.IMAGE_AND_VIDEO -> CameraMedia.IMAGE
        }
        val (cameraIntent, uri) = MediaUtil.getMediaIntentUri(
            this@TedImagePickerActivity,
            cameraMedia,
            builder.savedDirectoryName
        )
        TedRxOnActivityResult.with(this@TedImagePickerActivity)
            .startActivityForResult(cameraIntent)
            .subscribe { activityResult: ActivityResult ->
                if (activityResult.resultCode == Activity.RESULT_OK) {
                    MediaUtil.scanMedia(this, uri)
                        .subscribeOnIOAndObserveOnMain()
                        .subscribe {
                            loadMedia(true)
                            onMediaClick(uri)
                        }
                }
            }
    }


    private fun onMediaClick(uri: Uri) {
        when (builder.selectType) {
            SelectType.SINGLE -> onSingleMediaClick(uri)
            SelectType.MULTI -> onMultiMediaClick(uri)
        }
    }

    private fun onMultiMediaClick(uri: Uri) {
        mediaAdapter.toggleMediaSelect(uri)

        (mRecyclerViewSelectedMedia.adapter as? BaseRecyclerViewAdapter<Uri, *>)?.replaceAll(
            mediaAdapter.selectedUriList,
            true
        )

        mRecyclerViewSelectedMedia
        updateSelectedMediaView()
        setupButtonVisibility()
    }

    private fun setupSelectedMediaView() {
        mViewSelectedMedia.run {
            if (mediaAdapter.selectedUriList.size > 0) {
                layoutParams.height =
                    resources.getDimensionPixelSize(R.dimen.ted_image_picker_selected_view_height)
            } else {
                layoutParams.height = 0
            }
            requestLayout()
        }
    }

    private fun updateSelectedMediaView() {
        mViewSelectedMedia.post {
            mViewSelectedMedia.run {
                if (mediaAdapter.selectedUriList.size > 0) {
                    slideView(
                        this,
                        layoutParams.height,
                        resources.getDimensionPixelSize(R.dimen.ted_image_picker_selected_view_height)
                    )
                } else {
                    slideView(this, layoutParams.height, 0)
                }
            }
        }
    }

    private fun slideView(view: View, currentHeight: Int, newHeight: Int) {
        val valueAnimator = ValueAnimator.ofInt(currentHeight, newHeight).apply {
            addUpdateListener {
                view.layoutParams.height = it.animatedValue as Int
                view.requestLayout()
            }
        }

        AnimatorSet().apply {
            interpolator = AccelerateDecelerateInterpolator()
            play(valueAnimator)
        }.start()
    }

    private fun onSingleMediaClick(uri: Uri) {
        val data = Intent().apply {
            putExtra(EXTRA_SELECTED_URI, uri)
        }
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    override fun finish() {
        super.finish()
        finishAnimation()
    }

    private fun finishAnimation() {
        if (builder.finishEnterAnim != null && builder.finishExitAnim != null) {
            overridePendingTransition(builder.finishEnterAnim!!, builder.finishExitAnim!!)
        }
    }

    private fun onMultiMediaDone() {


        val selectedUriList = mediaAdapter.selectedUriList
        if (selectedUriList.size < builder.minCount) {
            val message = builder.minCountMessage ?: getString(builder.minCountMessageResId)
            ToastUtil.showToast(message)
        } else {

            val data = Intent().apply {
                putParcelableArrayListExtra(
                    EXTRA_SELECTED_URI_LIST,
                    ArrayList(selectedUriList)
                )
            }
            setResult(Activity.RESULT_OK, data)
            finish()
        }

    }


    private fun setSelectedAlbum(selectedPosition: Int) {
        val album = albumAdapter.getItem(selectedPosition)
        if (this.selectedPosition == selectedPosition && mSelectedAlbum == album) {
            return
        }

        mSelectedAlbum = album

        mLayoutSelectedAlbumDropDown.visible()
        mViewSelectedAlbum.visible()
        mSelectedAlbumName.text = mSelectedAlbum?.name
        mSelectedAlbumDropDownAlbumName.text = mSelectedAlbum?.name

        mSelectedAlbumDropdownImageCount.text = TextFormatUtil.getMediaCountText(builder.imageCountFormat, mSelectedAlbum?.mediaUris?.size ?: 0)
        mImageCount.text = TextFormatUtil.getMediaCountText(builder.imageCountFormat, mSelectedAlbum?.mediaUris?.size ?: 0)

        this.selectedPosition = selectedPosition
        albumAdapter.setSelectedAlbum(album)
        mediaAdapter.replaceAll(album.mediaUris)
        mRecyclerViewMedia.layoutManager?.scrollToPosition(0)
    }

    private fun setupListener() {
        mViewSelectedAlbum.setOnClickListener {
            mDrawerLayout.toggle()
        }

        mViewDoneTop.setOnClickListener {
            onMultiMediaDone()
        }
        mViewDoneBottom.setOnClickListener {
            onMultiMediaDone()
        }

        mLayoutSelectedAlbumDropDown.setOnClickListener {

            mIsAlbumOpened = !mIsAlbumOpened

            if(mIsAlbumOpened){
                mRecyclerViewAlbumDropDown.visible()
                mDropdownIcon.setImageResource(R.drawable.ic_arrow_drop_up_black_24dp)
            } else {
                mRecyclerViewAlbumDropDown.gone()
                mDropdownIcon.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp)
            }
        }

    }

    private fun setupAlbumType() {
        if (builder.albumType == AlbumType.DRAWER) {
            mLayoutSelectedAlbumDropDown.gone()
        } else {
            mViewBottom.gone()
            mDrawerLayout.setLock(true)
        }
    }


    override fun onBackPressed() {
        if (isAlbumOpened()) {
            closeAlbum()
        } else {
            super.onBackPressed()
        }

    }

    private fun isAlbumOpened(): Boolean =
        if (builder.albumType == AlbumType.DRAWER) {
            mDrawerLayout.isOpen
        } else {
            mIsAlbumOpened
        }

    private fun closeAlbum() {

        if (builder.albumType == AlbumType.DRAWER) {
            mDrawerLayout.close()
        } else {
            mIsAlbumOpened = false
        }
    }

    override fun onDestroy() {
        if (!disposable.isDisposed) {
            disposable.dispose()
        }
        super.onDestroy()
    }


    companion object {
        private const val IMAGE_SPAN_COUNT = 3
        private const val EXTRA_BUILDER = "EXTRA_BUILDER"
        private const val EXTRA_SELECTED_URI = "EXTRA_SELECTED_URI"
        private const val EXTRA_SELECTED_URI_LIST = "EXTRA_SELECTED_URI_LIST"

        internal fun getIntent(context: Context, builder: TedImagePickerBaseBuilder<*>) =
            Intent(context, TedImagePickerActivity::class.java)
                .apply {
                    putExtra(EXTRA_BUILDER, builder)
                }

        internal fun getSelectedUri(data: Intent): Uri? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data.getParcelableExtra(EXTRA_SELECTED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                data.getParcelableExtra(EXTRA_SELECTED_URI)
            }

        internal fun getSelectedUriList(data: Intent): List<Uri>? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data.getParcelableArrayListExtra(EXTRA_SELECTED_URI_LIST, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                data.getParcelableArrayListExtra(EXTRA_SELECTED_URI_LIST)
            }
    }

}


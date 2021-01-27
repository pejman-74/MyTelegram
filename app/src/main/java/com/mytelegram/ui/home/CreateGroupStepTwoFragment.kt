package com.mytelegram.ui.home

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.github.florent37.viewanimator.ViewAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mytelegram.BuildConfig
import com.mytelegram.R
import com.mytelegram.data.model.resouces.Resource
import com.mytelegram.databinding.FragmentCreateGroupStepTwoBinding
import com.mytelegram.ui.base.BaseFragment
import com.mytelegram.util.handelApiError
import com.yalantis.ucrop.UCrop
import java.io.ByteArrayOutputStream
import java.io.File


class CreateGroupStepTwoFragment :
    BaseFragment<HomeViewModel, FragmentCreateGroupStepTwoBinding>() {

    private val args: CreateGroupStepTwoFragmentArgs by navArgs()
    private var croppedImageUri: Uri? = null
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val stepTowCreateGroupItemAdapter = StepTowCreateGroupItemAdapter()
        stepTowCreateGroupItemAdapter.setData(args.selectedUsers.asList())

        vBinding.tvMemberCount.text = resources.getString(R.string.members, args.selectedUsers.size)
        vBinding.rvCreateGroupStepTwo.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = stepTowCreateGroupItemAdapter
        }
        vBinding.toolbarBtnBack.setOnClickListener {
            parentFragment?.findNavController()?.navigateUp()
        }

        vBinding.pfabCreateGroupStepTow.setOnClickListener {
            if (vBinding.itGroupName.text!!.isNotBlank()) {
                var avatarBase64String = ""
                croppedImageUri?.let {
                    avatarBase64String = getBitmap(requireContext(), it)
                        .toBase64String(Bitmap.CompressFormat.JPEG, 100)
                }
                vModel.createRoom(
                    vBinding.itGroupName.text.toString().trim(), avatarBase64String,
                    args.selectedUsers.map {
                        it.userId
                    }.toTypedArray()
                )
                    .observe(viewLifecycleOwner, { resourceResult ->
                        vBinding.pfabCreateGroupStepTow.showLoadingAnimation(resourceResult is Resource.Loading)
                        when (resourceResult) {
                            is Resource.Success -> {
                                findNavController().navigate(CreateGroupStepTwoFragmentDirections.actionGlobalHomeFragment())
                            }
                            is Resource.Failure -> {
                                handelApiError(resourceResult)
                            }
                            Resource.Loading -> {
                            }
                        }
                    })
            } else
            ViewAnimator.animate(vBinding.itGroupName).shake().duration(500).start()
        }

        vBinding.selectAvatarIv.setOnClickListener {
            /*
            * user select an image from gallery or directly use camera to take photo.
            * then the image uri sent to UCrop to crop image
            * */
            selectImage(requireContext())

        }
    }


    private fun selectImage(context: Context) {
        val options = arrayOf<CharSequence>(
            getString(R.string.take_photo),
            getString(R.string.choose_from_gallery),
            getString(R.string.cancel)
        )
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle(getString(R.string.choose_your_profile_picture))
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == getString(R.string.take_photo) -> {
                    takePhoto()
                }
                options[item] == getString(R.string.choose_from_gallery) -> {
                    pickFromGallery()
                }
                options[item] == getString(R.string.cancel) -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }


    private lateinit var takenImageFileUri: Uri
    private fun takePhoto() {

        val takenImageFile = File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "_TakenImg.jpg"
        )
        takenImageFileUri = FileProvider.getUriForFile(
            requireContext().applicationContext, BuildConfig.APPLICATION_ID + ".provider",
            takenImageFile
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, takenImageFileUri)
        intent.putExtra("return-data", true)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)

    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun lunchUCrop(sourceUri: Uri) {
        val croppedImgFile = File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "_croppedImg.jpg"
        )
        UCrop.of(sourceUri, Uri.fromFile(croppedImgFile)).withAspectRatio(1F, 1F)
            .withOptions(UCrop.Options().apply {
                setCircleDimmedLayer(true)
                setShowCropGrid(false)
                setShowCropFrame(true)
                withMaxResultSize(512, 512)
            })
            .start(requireActivity(), this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            return
        }
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                lunchUCrop(takenImageFileUri)
            }
            GALLERY_REQUEST_CODE -> {
                data?.data?.let {
                    lunchUCrop(it)
                }
            }
            UCrop.REQUEST_CROP -> {
                croppedImageUri = UCrop.getOutput(data!!)
                vBinding.selectAvatarIv.load(croppedImageUri)
            }
        }

    }

    private fun Bitmap.toBase64String(format: Bitmap.CompressFormat, quality: Int): String {
        ByteArrayOutputStream().use { out ->
            compress(format, quality, out)
            val bytes = out.toByteArray()
            out.flush()
            return Base64.encodeToString(bytes, Base64.DEFAULT)

        }
    }


    private fun getBitmap(context: Context, imageUri: Uri) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(
                    context.contentResolver,
                    imageUri
                )
            )

        } else {
            context
                .contentResolver
                .openInputStream(imageUri).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
        }


    override fun getViewModel() = activityViewModels<HomeViewModel>()

    override fun getViewBinding(layoutInflater: LayoutInflater, container: ViewGroup?) =
        FragmentCreateGroupStepTwoBinding.inflate(layoutInflater, container, false)

    companion object {
        private const val GALLERY_REQUEST_CODE = 1
        private const val CAMERA_REQUEST_CODE = 0
    }


}
package org.skyfaced.circlechart

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Chart(
    val maxProgress: Int = 500,
    val folders: List<Folder> = listOf(
        Folder(
            "Image", 470,
            listOf(
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
                File("image.jpg", Uri.parse("")),
            ),
        ),
        Folder("Video", 343, listOf(
            File("video.mp4"),
            File("video.mp4"),
            File("video.mp4"),
            File("video.mp4"),
            File("video.mp4"),
            File("video.mp4"),
        )),
        Folder("Text", 2, listOf(
            File("text.txt"),
            File("text.txt"),
            File("text.txt"),
        )),
        Folder("Other", 500, listOf(
            File("other.zip"),
            File("other.7z"),
            File("other"),
        )),
        Folder("Long name for other", 2, listOf(
            File("other.zip"),
            File("other.7z"),
            File("other"),
        )),
        Folder("Other2", 33, listOf(
            File("other.zip"),
            File("other.7z"),
            File("other"),
        )),
    ),
)

data class Folder(
    val name: String,
    val size: Int,
    val files: List<File>,
)

data class File(
    val name: String,
    val preview: Uri = Uri.EMPTY,
)

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(Chart())
    val state = _state.asStateFlow()

//    fun changeFolder(idx: Int) {
//        if (idx > state.value.folders.size) throw IllegalStateException("Index can't be greater than folder size")
//        if (idx < 0) throw IllegalStateException("Index can't be less than zero")
//
//        _state.value =
//            state.value.copy(selectedFolder = idx, currentFolder = state.value.folders[idx])
//    }
}
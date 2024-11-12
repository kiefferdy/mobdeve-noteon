package com.example.noteon

import android.content.Context
import androidx.appcompat.app.AlertDialog

class NoteOptionsDialog(private val context: Context) {
    fun show(
        note: Note,
        currentFolderId: Long = 0,
        isTrashView: Boolean = false,
        onUpdate: () -> Unit
    ) {
        if (isTrashView) {
            showTrashOptions(note, onUpdate)
        } else {
            showNormalOptions(note, currentFolderId, onUpdate)
        }
    }

    private fun showTrashOptions(note: Note, onUpdate: () -> Unit) {
        AlertDialog.Builder(context)
            .setItems(arrayOf(
                context.getString(R.string.restore),
                context.getString(R.string.delete_permanently)
            )) { _, which ->
                when (which) {
                    0 -> {
                        DataHandler.restoreNoteFromTrash(note.id)
                        onUpdate()
                    }
                    1 -> showDeletePermanentlyDialog(note, onUpdate)
                }
            }
            .show()
    }

    private fun showDeletePermanentlyDialog(note: Note, onUpdate: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(R.string.delete_permanently)
            .setMessage(R.string.delete_permanently_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                DataHandler.deleteNotePermanently(note.id)
                onUpdate()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showNormalOptions(note: Note, currentFolderId: Long, onUpdate: () -> Unit) {
        val options = arrayOf(
            context.getString(if (note.isFavorite) R.string.remove_from_favorites else R.string.add_to_favorites),
            context.getString(R.string.move_to_folder),
            context.getString(R.string.move_to_trash)
        )

        AlertDialog.Builder(context)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        DataHandler.toggleNoteFavorite(note.id)
                        onUpdate()
                    }
                    1 -> showMoveToFolderDialog(note, currentFolderId, onUpdate)
                    2 -> {
                        DataHandler.moveNoteToTrash(note.id)
                        onUpdate()
                    }
                }
            }
            .show()
    }

    private fun showMoveToFolderDialog(note: Note, currentViewFolderId: Long, onUpdate: () -> Unit) {
        val options = mutableListOf<String>()
        val folderIds = mutableListOf<Long>()

        // Add "Remove from folder" option if note is in a folder
        if (note.folderId != 0L) {
            options.add(context.getString(R.string.remove_from_folder))
            folderIds.add(0)
        }

        // Add all folders except the current one
        DataHandler.getAllFolders().forEach { folder ->
            if (folder.id != note.folderId) {
                options.add(folder.name)
                folderIds.add(folder.id)
            }
        }

        AlertDialog.Builder(context)
            .setTitle(R.string.move_to_folder)
            .setItems(options.toTypedArray()) { _, which ->
                val selectedFolderId = folderIds[which]
                DataHandler.moveNoteToFolder(note.id, selectedFolderId)
                onUpdate()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
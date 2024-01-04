package com.example.materialleanbackfilexplorer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import android.widget.TextView
import android.widget.ImageView
import android.widget.PopupMenu

class FileXplorerFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FileAdapter
    private var currentDirectory: File = File("/")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_file_xplorer, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = FileAdapter()
        recyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigateTo(currentDirectory)
    }

    private fun navigateTo(directory: File) {
        currentDirectory = directory
        val files = directory.listFiles()
        if (files != null) {
            adapter.setFiles(files.toList())
        }
    }

    private inner class FileAdapter : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {
        private var files: List<File> = emptyList()

        fun setFiles(files: List<File>) {
            this.files = files
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.file_item, parent, false)
            return FileViewHolder(view)
        }

        override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
            val file = files[position]
            holder.bind(file)
        }

        override fun getItemCount(): Int = files.size

        inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val fileName: TextView = itemView.findViewById(R.id.file_name)
            private val fileIcon: ImageView = itemView.findViewById(R.id.file_icon)

            fun bind(file: File) {
                fileName.text = file.name
                fileIcon.setImageResource(if (file.isDirectory) R.drawable.ic_directory else R.drawable.ic_file)
                itemView.setOnClickListener {
                    if (file.isDirectory) {
                        navigateTo(file)
                    } else {
                        // Open the file with an appropriate app
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.fromFile(file)
                        startActivity(intent)
                    }
                }
                itemView.setOnLongClickListener { view ->
                    val popup = PopupMenu(view.context, view)
                    popup.menuInflater.inflate(R.menu.file_operations_menu, popup.menu)
                    popup.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.delete -> {
                                file.delete()
                                navigateTo(currentDirectory) // Refresh the file list
                                true
                            }
                            R.id.copy -> {
                                file.copyTo(File(currentDirectory, file.name))
                                navigateTo(currentDirectory) // Refresh the file list
                                true
                            }
                            //move
                            R.id.move -> {
                                file.copyTo(File(currentDirectory, file.name))
                                file.delete()
                                navigateTo(currentDirectory) // Refresh the file list
                                true
                            }
                            //rename
                            R.id.rename -> {
                                file.renameTo(File(currentDirectory, file.name))
                                navigateTo(currentDirectory) // Refresh the file list
                                true
                            }
                            //share
                            R.id.share -> {
                                val intent = Intent(Intent.ACTION_SEND)
                                intent.type = "text/plain"
                                intent.putExtra(Intent.EXTRA_TEXT, file.absolutePath)
                                startActivity(Intent.createChooser(intent, "Share file"))
                                true
                            }
                            else -> false
                        }
                    }
                    popup.show()
                    return@setOnLongClickListener true
                }
            }
        }
    }
}
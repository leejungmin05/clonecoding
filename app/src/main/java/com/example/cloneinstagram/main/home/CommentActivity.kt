package com.example.cloneinstagram.main.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.cloneinstagram.R
import com.example.cloneinstagram.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.item_comment.view.*

class CommentActivity : AppCompatActivity() {
    var contentUid : String ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        contentUid = intent.getStringExtra("contentUid")
        comment_recyclerView.adapter = CommentRecylcerviewAdapter()
        comment_recyclerView.layoutManager = LinearLayoutManager(this)


        comment_btn_send?.setOnClickListener{
            var comment =ContentDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = comment_edit_message.text.toString()
            comment.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("images").document(contentUid!!).collection("comments").document().set(comment)

            comment_edit_message.setText("")
        }
    }
    inner class CommentRecylcerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var comments :ArrayList<ContentDTO.Comment> = arrayListOf()
        init {
            FirebaseFirestore.getInstance()
                .collection("images")
                .document(contentUid!!)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    comments.clear()
                    if(querySnapshot == null) return@addSnapshotListener
                    for(snapshot in querySnapshot.documents!!){
                        comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)
                    }
                    notifyDataSetChanged()
                }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment,parent,false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View) :RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView
            view.commentviewitem_textview_comment.text=comments[position].comment
            view.commentviewitem_textview_profile.text=comments[position].userId

            FirebaseFirestore.getInstance()
                .collection("profileImages")
                .document(comments[position].uid!!)
                .get()
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        var url = task.result!!["image"]
                        Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(view.commentviewitem_imageview_profile)
                    }
                }
        }

        override fun getItemCount(): Int {
           return comments.size
        }

    }
}
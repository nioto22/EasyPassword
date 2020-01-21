package com.example.nioto.easypasword.adapters;

import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.nioto.easypasword.R;
import com.example.nioto.easypasword.model.Note;

/**
 * Created by Nioto on 12/11/2018.
 */

public class NoteAdapter extends ListAdapter<Note,NoteAdapter.NoteHolder> {
    private OnItemClickListener listener;

    public NoteAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Note> DIFF_CALLBACK = new DiffUtil.ItemCallback<Note>() {
        @Override
        public boolean areItemsTheSame(Note oldItem, Note newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(Note oldItem, Note newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getLogin().equals(newItem.getLogin()) &&
                    oldItem.getPassword().equals(newItem.getPassword()) &&
                    oldItem.getPriority()== newItem.getPriority();
        }
    };


    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);
        return new NoteHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        Note currentNote = getItem(position);
        holder.textViewTitle.setText(currentNote.getTitle());
        holder.textViewLogin.setText(currentNote.getLogin());
        holder.textViewPassword.setText(currentNote.getPassword());
        holder.textViewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        holder.textViewPriority.setText(String.valueOf(currentNote.getPriority()));
    }


    public Note getNoteAt (int position){
        return getItem(position);
    }

    class NoteHolder extends RecyclerView.ViewHolder{
        private TextView textViewTitle;
        private TextView textViewLogin;
        private TextView textViewPassword;
        private TextView textViewPriority;

        public NoteHolder(View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewLogin = itemView.findViewById(R.id.text_view_login);
            textViewPassword = itemView.findViewById(R.id.text_view_password);
            textViewPriority = itemView.findViewById(R.id.text_view_priority);

            // onclicklistener to the item view = note
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(getItem(position));
                    }
                }
            });
        }
    }

    // Interface to implement an onClick to our notes
    // To forced onItemClick method when this interface is implemented
    public  interface OnItemClickListener{
        void onItemClick(Note note);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }



}

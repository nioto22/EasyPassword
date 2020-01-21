package com.example.nioto.easypasword.ui;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.nioto.easypasword.R;
import com.example.nioto.easypasword.adapters.NoteAdapter;
import com.example.nioto.easypasword.model.Note;
import com.example.nioto.easypasword.service.TransferService;
import com.example.nioto.easypasword.viewModel.NoteViewModel;

import java.util.List;

import static com.example.nioto.easypasword.ui.AddEditNoteActivity.RESULT_DELETED;
import static com.example.nioto.easypasword.ui.AddEditNoteActivity.RESULT_SERVICE_STARTED;

//implements SearchView.OnQueryTextListener

public class MainActivity extends AppCompatActivity {

    public static final int ADD_NOTE_REQUEST = 1;
    public static final int EDIT_NOTE_REQUEST = 2;
    public final static int PERMISSION_REQUEST_CODE = 10101;
    public static final int COPY_LOGIN_KEY = 1;
    public static final int COPY_PASSWORD_KEY = 2;
    public static final String EXTRA_POS_FOR_RESULT = "EXTRA_POS_FOR_RESULT";
    // Our ViewModel
    private NoteViewModel noteViewModel;
    private RecyclerView recyclerView;
    private NoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ----------------------
        // FLOATING ACTION BUTTON
        // ----------------------
        FloatingActionButton buttonAddNote = findViewById(R.id.button_add_note);
        buttonAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
                startActivityForResult(intent, ADD_NOTE_REQUEST);
            }
        });

        // --------------
        // RECYCLERVIEW
        // --------------
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new NoteAdapter();
        recyclerView.setAdapter(adapter);

        // Warning not new ViewModel but an Instance
        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(@Nullable List<Note> notes) {
                adapter.submitList(notes);
            }
        });

        // -------------------
        // DELETE WHEN SWIPED
        // -------------------
       /* new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                noteViewModel.delete(adapter.getNoteAt(viewHolder.getAdapterPosition()));
                Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);*/

        SwipeHelper swipeHelper = new SwipeHelper(this, recyclerView) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new SwipeHelper.UnderlayButton(getApplicationContext(),
                        "",
                        R.drawable.transfert,
                        Color.parseColor("#FF3C30"),
                        new SwipeHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (Settings.canDrawOverlays(MainActivity.this)) {
                                        // Launch service right away - the user has already previously granted permission
                                        launchTransferService(pos);
                                    }
                                    else {
                                        // Check that the user has granted permission, and prompt them if not
                                        checkDrawOverlayPermission(pos);
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "Sorry, not available with your version of Android. Need Android 23 Marshmallow", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                ));

                underlayButtons.add(new SwipeHelper.UnderlayButton(getApplicationContext(),
                        "",
                        R.drawable.copypassword,
                        Color.parseColor("#FF9502"),
                        new SwipeHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                copyTextToClipBoard(pos, COPY_PASSWORD_KEY);
                            }
                        }
                ));
                underlayButtons.add(new SwipeHelper.UnderlayButton(getApplicationContext(),
                        "",
                        R.drawable.copylogin,
                        Color.parseColor("#C7C7CB"),
                        new SwipeHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                copyTextToClipBoard(pos, COPY_LOGIN_KEY);
                            }
                        }
                ));
            }
        };
        // ----------------------

        // ---------------------------------------------------
        // EDIT WHEN CLICK : LISTENER FROM ADAPTER INTERFACE
        // ---------------------------------------------------
        adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note) {
                Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
                intent.putExtra(AddEditNoteActivity.EXTRA_ID, note.getId());
                intent.putExtra(AddEditNoteActivity.EXTRA_TITLE, note.getTitle());
                intent.putExtra(AddEditNoteActivity.EXTRA_LOGIN, note.getLogin());
                intent.putExtra(AddEditNoteActivity.EXTRA_PASSWORD, note.getPassword());
                intent.putExtra(AddEditNoteActivity.EXTRA_COMMENT, note.getComment());
                intent.putExtra(AddEditNoteActivity.EXTRA_PRIORITY, note.getPriority());
                startActivityForResult(intent, EDIT_NOTE_REQUEST);
            }
        });
    }

    // --------------------------------------
    //   CLICK OPTIONS : COPY OR TRANSFER
    // --------------------------------------

    // TRANSFER

    private void launchTransferService(int pos) {
        Note note = adapter.getNoteAt(pos);
        Intent svc = new Intent(this, TransferService.class);
        svc.putExtra(AddEditNoteActivity.EXTRA_TITLE, note.getTitle());
        svc.putExtra(AddEditNoteActivity.EXTRA_LOGIN, note.getLogin());
        svc.putExtra(AddEditNoteActivity.EXTRA_PASSWORD, note.getPassword());
        this.startService(svc);
        moveTaskToBack(true);
        finish();
    }

    public void checkDrawOverlayPermission(int pos) {

        // Checks if app already has permission to draw overlays
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // If not, form up an Intent to launch the permission request
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                intent.putExtra(EXTRA_POS_FOR_RESULT, pos);
                // Launch Intent, with the supplied request code
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            }
        }
    }



    // COPY

    public void copyTextToClipBoard(int pos, int copyKey) {
        String label;
        String text;

        switch (copyKey){
            case COPY_LOGIN_KEY :
                label = "Login";
                text = adapter.getNoteAt(pos).getLogin();
                break;
            case COPY_PASSWORD_KEY :
                label = "Password";
                text = adapter.getNoteAt(pos).getPassword();
                break;
            default:
                label ="";
                text ="";
                break;
        }
        Toast.makeText(MainActivity.this, label +" copy to clipboard", Toast.LENGTH_SHORT).show();

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
    }

    // -------------------
    // ON ACTIVITY RESULT
    // -------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Password not saved", Toast.LENGTH_SHORT).show();
        } else if (resultCode == RESULT_SERVICE_STARTED) {
            finish();
        } else {
            switch (requestCode) {
                case ADD_NOTE_REQUEST:
                    if (resultCode == RESULT_OK) {
                        String title = data.getStringExtra(AddEditNoteActivity.EXTRA_TITLE);
                        String login = data.getStringExtra(AddEditNoteActivity.EXTRA_LOGIN);
                        String password = data.getStringExtra(AddEditNoteActivity.EXTRA_PASSWORD);
                        String comment = data.getStringExtra(AddEditNoteActivity.EXTRA_COMMENT);
                        int priority = data.getIntExtra(AddEditNoteActivity.EXTRA_PRIORITY, 1);

                        Note note = new Note(title, login, password, comment, priority);
                        noteViewModel.insert(note);
                        Toast.makeText(this, "Password saved", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case EDIT_NOTE_REQUEST:
                    int id = data.getIntExtra(AddEditNoteActivity.EXTRA_ID, -1);

                    if (id == -1) {
                        Toast.makeText(this, "Password can't be updated", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String title = data.getStringExtra(AddEditNoteActivity.EXTRA_TITLE);
                    String login = data.getStringExtra(AddEditNoteActivity.EXTRA_LOGIN);
                    String password = data.getStringExtra(AddEditNoteActivity.EXTRA_PASSWORD);
                    String comment = data.getStringExtra(AddEditNoteActivity.EXTRA_COMMENT);
                    int priority = data.getIntExtra(AddEditNoteActivity.EXTRA_PRIORITY, 1);

                    Note note = new Note(title, login, password, comment, priority);
                    note.setId(id);
                    if (resultCode == RESULT_OK) {
                        noteViewModel.update(note);
                        Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show();
                    } else if (resultCode == RESULT_DELETED) {
                        noteViewModel.delete(note);
                        Toast.makeText(this, "Password deleted", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case PERMISSION_REQUEST_CODE:
                    // Double-check that the user granted it, and didn't just dismiss the request
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Settings.canDrawOverlays(this)) {
                            // Launch the service
                            int pos = getIntent().getIntExtra(EXTRA_POS_FOR_RESULT, -1);
                            if (pos != -1) {
                                launchTransferService(pos);
                            }
                        } else {
                            Toast.makeText(this, "Sorry. Can't draw overlays without permission...", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }


    // -----------------------
    //          MENU
    // -----------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater menuInflater = getMenuInflater();
        //menuInflater.inflate(R.menu.main_menu, menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.search_note);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(onQueryTextListener);
        return true;
    }
    // -----------------------
    // MENU = HERE DELETE ALL
    // -----------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.export_all_notes:
                noteViewModel.deleteAllNotes();
                Toast.makeText(this, "All passwords deleted", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // -----------------------
    // MENU = HERE SEARCH
    // -----------------------
    private android.support.v7.widget.SearchView.OnQueryTextListener onQueryTextListener =
            new android.support.v7.widget.SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    getResults(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    getResults(newText);
                    return true;
                }

                private void getResults(String newText) {
                    String queryText = "%" + newText + "%";
                    noteViewModel.searchQuery(queryText).observe(
                            MainActivity.this, new Observer<List<Note>>() {
                                @Override
                                public void onChanged(@Nullable List<Note> notes) {
                                    if (notes == null) return;
                                    adapter.submitList(notes);
                                }
                            });
                }

            };
    // --------------------------
}


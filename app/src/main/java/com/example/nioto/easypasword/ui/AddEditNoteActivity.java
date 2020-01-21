package com.example.nioto.easypasword.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.nioto.easypasword.R;
import com.example.nioto.easypasword.service.TransferService;

import static com.example.nioto.easypasword.ui.MainActivity.COPY_LOGIN_KEY;
import static com.example.nioto.easypasword.ui.MainActivity.COPY_PASSWORD_KEY;
import static com.example.nioto.easypasword.ui.MainActivity.PERMISSION_REQUEST_CODE;

public class AddEditNoteActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String EXTRA_ID =
            "com.example.nioto.architecture_roomsqli_livedata_viewmodel.EXTRA_ID";
    public static final String EXTRA_TITLE =
            "com.example.nioto.architecture_roomsqli_livedata_viewmodel.EXTRA_TITLE";
    public static final String EXTRA_LOGIN =
            "com.example.nioto.architecture_roomsqli_livedata_viewmodel.EXTRA_LOGIN";
    public static final String EXTRA_PASSWORD =
            "com.example.nioto.architecture_roomsqli_livedata_viewmodel.EXTRA_PASSWORD";
    public static final String EXTRA_COMMENT =
            "com.example.nioto.architecture_roomsqli_livedata_viewmodel.EXTRA_COMMENT";
    public static final String EXTRA_PRIORITY =
            "com.example.nioto.architecture_roomsqli_livedata_viewmodel.EXTRA_PRIORITY";
    public static final int RESULT_DELETED = 3;
    public static final int RESULT_SERVICE_STARTED = 1022;


    private EditText editTextTitle;
    private EditText editTextLogin;
    private EditText editTextPassword;
    private EditText editTextComment;
    private Button buttonPriority1;
    private Button buttonPriority2;
    private Button buttonPriority3;
    private ImageButton buttonCopyLogin;
    private ImageButton buttonCopyPassword;
    private ImageButton buttonDisplayPassword;

    private int priorityValue;
    private int menuId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        configureViews();
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ID)) {
            setTitle("Edit Password");
            menuId = R.menu.edit_note_menu;
            editTextTitle.setText(intent.getStringExtra(EXTRA_TITLE));
            editTextLogin.setText(intent.getStringExtra(EXTRA_LOGIN));
            editTextPassword.setText(intent.getStringExtra(EXTRA_PASSWORD));
            editTextComment.setText(intent.getStringExtra(EXTRA_COMMENT));
            setButtonPriorityValue(intent.getIntExtra(EXTRA_PRIORITY, 1));
        } else {
            setTitle("Add Password");
            menuId = R.menu.add_note_menu;
            setButtonPriorityValue(1);
        }
    }

    private void configureViews(){
        editTextTitle = findViewById(R.id.edit_text_title);
        editTextLogin = findViewById(R.id.edit_text_login);
        editTextPassword = findViewById(R.id.edit_text_password);
        editTextComment = findViewById(R.id.edit_text_comment);
        configureButtons();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(menuId, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_note :
                saveNote();
                return true;
            case R.id.delete_note:
                deleteNote();
                return true;
            case R.id.send_note:
                sendNote();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if a request code is received is the overlay draw request
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Double-check that the user granted it, and didn't just dismiss the request
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    // Launch the service
                    launchTransferService();
                }
            } else {
                Toast.makeText(this, "Sorry. Can't draw overlays without permission...", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // -----------------
    //   MENU METHODS
    // -----------------
    private void saveNote(){
        String title = editTextTitle.getText().toString();
        String login = editTextLogin.getText().toString();
        String password = editTextPassword.getText().toString();
        String comment = editTextComment.getText().toString();
        int priority = priorityValue;

        if(title.trim().isEmpty() || password.trim().isEmpty()){
            Toast.makeText(this, "Please insert at least a title and a password", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent data = new Intent();
        data.putExtra(EXTRA_TITLE, title);
        data.putExtra(EXTRA_LOGIN, login);
        data.putExtra(EXTRA_PASSWORD, password);
        data.putExtra(EXTRA_COMMENT, comment);
        data.putExtra(EXTRA_PRIORITY, priority);

        int id = getIntent().getIntExtra(EXTRA_ID, -1);
        if (id != -1) {
            data.putExtra(EXTRA_ID, id);
        }

        setResult(RESULT_OK, data);
        finish();
    }

    private void deleteNote(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Intent data = new Intent();
                        int id = getIntent().getIntExtra(EXTRA_ID, -1);
                        if (id != -1) {
                            data.putExtra(EXTRA_ID, id);
                        }
                        setResult(RESULT_DELETED, data);
                        finish();
                    case DialogInterface.BUTTON_NEGATIVE:
                        Toast.makeText(AddEditNoteActivity.this, "Delete canceled", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this Password ?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void sendNote() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(AddEditNoteActivity.this)) {
                // Launch service right away - the user has already previously granted permission
                launchTransferService();
            } else {
                // Check that the user has granted permission, and prompt them if not
                checkDrawOverlayPermission();
            }
        } else {
            Toast.makeText(AddEditNoteActivity.this, "Sorry, not available with your version of Android. Need Android 23 Marshmallow", Toast.LENGTH_SHORT).show();
        }
    }

    public void launchTransferService(){

        String title = editTextTitle.getText().toString();
        String login = editTextLogin.getText().toString();
        String password = editTextPassword.getText().toString();

        Intent svc = new Intent(this, TransferService.class);
        svc.putExtra(AddEditNoteActivity.EXTRA_TITLE, title);
        svc.putExtra(AddEditNoteActivity.EXTRA_LOGIN, login);
        svc.putExtra(AddEditNoteActivity.EXTRA_PASSWORD, password);
        this.startService(svc);
        setResult(RESULT_SERVICE_STARTED);
        moveTaskToBack(true);
        finish();
    }

    public void checkDrawOverlayPermission() {

        // Checks if app already has permission to draw overlays
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // If not, form up an Intent to launch the permission request
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                // Launch Intent, with the supplied request code
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            }
        }
    }
    // -----------------------------------------


    // -----------------
    // CONFIGURE BUTTON
    // -----------------

    private void configureButtons() {
        buttonPriority1 = findViewById(R.id.button_priority_1);
        buttonPriority1.setOnClickListener(this);
        buttonPriority2 = findViewById(R.id.button_priority_2);
        buttonPriority2.setOnClickListener(this);
        buttonPriority3 = findViewById(R.id.button_priority_3);
        buttonPriority3.setOnClickListener(this);
        buttonCopyLogin = findViewById(R.id.button_edit_copy_login);
        buttonCopyLogin.setOnClickListener(this);
        buttonCopyPassword = findViewById(R.id.button_edit_copy_password);
        buttonCopyPassword.setOnClickListener(this);
        buttonDisplayPassword = findViewById(R.id.button_edit_see_password);
        buttonDisplayPassword.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_priority_1:
                setButtonPriorityValue(1);
                break;
            case R.id.button_priority_2:
                setButtonPriorityValue(2);
                break;
            case R.id.button_priority_3:
                setButtonPriorityValue(3);
                break;
            case R.id.button_edit_copy_login:
                copyTextToClipboard(COPY_LOGIN_KEY);
                break;
            case R.id.button_edit_copy_password:
                copyTextToClipboard(COPY_PASSWORD_KEY);
                break;
            case R.id.button_edit_see_password:
                displayPasswordClicked();
                break;
            default:
                break;
        }
    }

    private void displayPasswordClicked() {
        int tag =Integer.parseInt(buttonDisplayPassword.getTag().toString());
        switch (tag){
            case 1 :
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                buttonDisplayPassword.setImageResource(R.drawable.ic_visibility_off);
                buttonDisplayPassword.setTag("2");
                break;
            case 2 :
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                buttonDisplayPassword.setImageResource(R.drawable.ic_visibility);
                buttonDisplayPassword.setTag("1");
                break;
            default:
                break;
        }
    }


    private void copyTextToClipboard(int copyKey) {
        String label;
        String text;

        switch (copyKey){
            case COPY_LOGIN_KEY :
                label = "Login";
                text = editTextLogin.getText().toString();
                break;
            case COPY_PASSWORD_KEY :
                label = "Password";
                text = editTextPassword.getText().toString();
                break;
            default:
                label ="";
                text ="";
                break;
        }
        if (text.equals("")){
            Toast.makeText(this, "Please enter a "+ label, Toast.LENGTH_SHORT).show();
            return;
        } else {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, label + " copy to clipboard.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setButtonPriorityValue(int value) {
        changeButtonStyleWhenNoClick(buttonPriority1);
        changeButtonStyleWhenNoClick(buttonPriority2);
        changeButtonStyleWhenNoClick(buttonPriority3);
        switch (value){
            case 1:
                changeButtonStyleWhenClicked(buttonPriority1);
                priorityValue = 1;
                break;
            case 2:
                changeButtonStyleWhenClicked(buttonPriority2);
                priorityValue = 2;
                break;
            case 3:
                changeButtonStyleWhenClicked(buttonPriority3);
                priorityValue = 3;
                break;
            default:
                break;
        }
    }
    private void changeButtonStyleWhenClicked (Button button){
        button.setBackgroundResource(R.drawable.buttonshape_clicked);
        button.setEnabled(false);
    }
    private void changeButtonStyleWhenNoClick (Button button){
        button.setBackgroundResource(R.drawable.buttonshape2);
        button.setEnabled(true);
    }


    // ----------------------------
}

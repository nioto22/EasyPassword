package com.example.nioto.easypasword.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class DialogFragmentExpotrsNotes extends DialogFragment {

    // FOR DATA
    private EditText etMailRecipient;
    private String mailRecipient;
    private EditText etMailSender;
    private String mailSender;





    public void sendEmail(String mail) {
        Log.i("Send email", "");

        String[] TO = {mailRecipient};
        String[] CC = {mailSender};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setDataAndType(Uri.parse("mailto:"), "text/plain");


        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Your subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Email message goes here");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            dismiss();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(),
                    "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }


}

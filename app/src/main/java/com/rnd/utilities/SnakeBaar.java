package com.rnd.utilities;

import android.app.Activity;
import android.graphics.Color;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.rnd.R;



public class SnakeBaar {

    public void showSnackBar(Activity ac, String message, View lay) {
        Snackbar snackbar = Snackbar
                .make( ac.getWindow().getDecorView().getRootView(), message, Snackbar.LENGTH_LONG )
                .setAction( "OK", onSnackBarClickListener() );

        snackbar.setActionTextColor( ac.getResources().getColor( R.color.colorPrimary ) );
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor( Color.WHITE );
        TextView textView = (TextView) snackbarView.findViewById( com.google.android.material.R.id.snackbar_text);
        //  textView.setTextColor(getResources().getColor(R.color.buttonbackground));
        textView.setTextColor( ContextCompat.getColor( ac, R.color.colorPrimary ) );
        snackbar.show();
    }

    private View.OnClickListener onSnackBarClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(LoginActivity.this, "You clicked SnackBar Button", Toast.LENGTH_SHORT).show();

            }
        };
    }
}

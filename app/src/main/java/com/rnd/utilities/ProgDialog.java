package com.rnd.utilities;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.core.content.ContextCompat;

import com.rnd.R;


public class ProgDialog {

    ProgressDialog progress;
   public  void progDialog(Context ac) {

       try {
           if(progress == null) {
               progress = new ProgressDialog(ac);
               progress.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLUE));
               progress.setMessage("Please wait..");
               progress.setIcon(R.drawable.logo);
               progress.setInverseBackgroundForced(true);
               progress.getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(ac, R.color.white)));
               progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
               progress.setIndeterminate(true);
           }
           progress.show();
       } catch (Exception e) {}
   }

    public void hideProg(){
    progress.hide();
}

    public void destroy() {
       if(progress != null)
           progress.dismiss();
    }
}

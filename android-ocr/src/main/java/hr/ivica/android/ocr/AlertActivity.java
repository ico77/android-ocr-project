package hr.ivica.android.ocr;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class AlertActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        String alertText = getIntent().getStringExtra("alertText");
        TextView mTextView = (TextView) findViewById(R.id.alertTextView);
        mTextView.setText(alertText);

        Button okButton = (Button) findViewById(R.id.finishButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });
    }
}

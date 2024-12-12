package com.example.doodler;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private DoodleView doodleView;
    private AlertDialog.Builder currentAlertDialog;
    private ImageView widthImageView;
    private AlertDialog lineWidthDialog;
    private AlertDialog colorDialog;
    private SeekBar alphaSeekBar;
    private SeekBar redSeekBar;
    private SeekBar greenSeekBar;
    private SeekBar blueSeekBar;
    private View colorView;
    private boolean isEraserActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        doodleView = findViewById(R.id.view);

        // Initialize Undo button
        Button undoButton = findViewById(R.id.undoButton);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doodleView.undoStroke();
                Toast.makeText(MainActivity.this, "Undo action performed", Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize Redo button
        Button redoButton = findViewById(R.id.redoButton);
        redoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doodleView.redoStroke();
                Toast.makeText(MainActivity.this, "Redo action performed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.clearId) {
            doodleView.clear();
        } else if (item.getItemId() == R.id.colorId) {
            showColorDialog();
        } else if (item.getItemId() == R.id.lineWidthId) {
            showLineWidthDialog();
        } else if (item.getItemId() == R.id.eraseId) {
            isEraserActive = !isEraserActive;
            doodleView.setEraserMode(isEraserActive);

            View eraseButton = findViewById(R.id.eraseId);
            if (eraseButton != null) {
                if (isEraserActive) {
                    eraseButton.setBackgroundColor(ContextCompat.getColor(this, R.color.eraser_active));
                    Toast.makeText(this, "Eraser On", Toast.LENGTH_SHORT).show();
                } else {
                    eraseButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
                    Toast.makeText(this, "Eraser Off", Toast.LENGTH_SHORT).show();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    void showColorDialog() {
        // Existing color dialog implementation
        currentAlertDialog = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.color_dialog, null);
        alphaSeekBar = view.findViewById(R.id.alphaSeekBar);
        redSeekBar = view.findViewById(R.id.redSeekBar);
        greenSeekBar = view.findViewById(R.id.greenSeekBar);
        blueSeekBar = view.findViewById(R.id.blueSeekBar);
        colorView = view.findViewById(R.id.colorView);


        alphaSeekBar.setOnSeekBarChangeListener(widthSeekBarChange);
        redSeekBar.setOnSeekBarChangeListener(widthSeekBarChange);
        greenSeekBar.setOnSeekBarChangeListener(widthSeekBarChange);
        blueSeekBar.setOnSeekBarChangeListener(widthSeekBarChange);

        int color = doodleView.getDrawingColor();
        alphaSeekBar.setProgress(Color.alpha(color));
        redSeekBar.setProgress(Color.red(color));
        greenSeekBar.setProgress(Color.green(color));
        blueSeekBar.setProgress(Color.blue(color));

        Button setColorButton = view.findViewById(R.id.setColorButton);
        setColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doodleView.setDrawingColor(Color.argb(
                        alphaSeekBar.getProgress(),
                        redSeekBar.getProgress(),
                        greenSeekBar.getProgress(),
                        blueSeekBar.getProgress()
                ));

                colorDialog.dismiss();

                Toast.makeText(getApplicationContext(), "Color Updated", Toast.LENGTH_LONG).show();
            }
        });

        currentAlertDialog.setView(view);
        currentAlertDialog.setTitle("Choose Color");
        colorDialog = currentAlertDialog.create();
        colorDialog.show();
    }

    void showLineWidthDialog() {
        currentAlertDialog = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.width_dialog, null);
        SeekBar widthSeekBar = view.findViewById(R.id.widthSeekBarId);
        Button setLineWidthButton = view.findViewById(R.id.widthDialogBtnId);
        widthImageView = view.findViewById(R.id.imageViewId);
        setLineWidthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doodleView.setLineWidth(widthSeekBar.getProgress());
                lineWidthDialog.dismiss();
                currentAlertDialog = null;
                Toast.makeText(getApplicationContext(), "Brush Size Set to " + widthSeekBar.getProgress(), Toast.LENGTH_LONG).show();
            }
        });

        widthSeekBar.setOnSeekBarChangeListener(widthSeekBarChange);
        widthSeekBar.setProgress(doodleView.getLineWidth());

        currentAlertDialog.setView(view);
        lineWidthDialog = currentAlertDialog.create();
        lineWidthDialog.setTitle("Set Brush Size");
        lineWidthDialog.show();
    }

    //stroke size

    private SeekBar.OnSeekBarChangeListener widthSeekBarChange = new SeekBar.OnSeekBarChangeListener() {
        Bitmap bitmap = Bitmap.createBitmap(400, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            Paint p = new Paint();
            p.setColor(doodleView.getDrawingColor());
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(progress);

            bitmap.eraseColor(Color.WHITE);
            canvas.drawLine(30, 50, 370, 50, p);
            widthImageView.setImageBitmap(bitmap);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
//

    };
}

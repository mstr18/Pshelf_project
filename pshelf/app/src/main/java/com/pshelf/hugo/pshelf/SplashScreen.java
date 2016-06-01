package com.pshelf.hugo.pshelf;

/**
 * Created by HFLopes on 19/02/2016.
 */


import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends Activity {



    TextView txtLoader;
    ProgressBar pbar;


    public void inicializar()
    {
        txtLoader.setText("Opencv Carregado com sucesso");
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {

                finish();
                Intent intent = new Intent();
                intent.setClass(SplashScreen.this, PrincipalDrawer.class);
                startActivity(intent);
            }
        }, 3000);
    }

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Erro na inicialização");
        } else {

            Log.e("OpenCV", "Conectado com sucesso");
        }
    }

    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {

            switch (status)
            {
                case LoaderCallbackInterface.SUCCESS:

                    setContentView(R.layout.activity_splash_screen);
                    txtLoader = (TextView) findViewById(R.id.txtLoader);
                    pbar = (ProgressBar) findViewById(R.id.progressBar);
                    inicializar();

                break;

                case LoaderCallbackInterface.INIT_FAILED:

                    txtLoader = (TextView) findViewById(R.id.txtLoader);
                    txtLoader.setText("Erro carregando OpenCV");

                break;
            }


        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mOpenCVCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);

    }

    public void onResume() {
        super.onResume();
    }

}
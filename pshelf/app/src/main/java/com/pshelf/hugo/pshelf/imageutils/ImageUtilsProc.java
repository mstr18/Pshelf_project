package com.pshelf.hugo.pshelf.imageutils;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;
import com.pshelf.hugo.pshelf.R;
import com.pshelf.hugo.pshelf.baseutils.ImageDeteccoesDAO;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Vector;


/**
 * Created by HFLopes on 03/03/2016.
 */
public class ImageUtilsProc extends AsyncTask<Void, Integer, Void> {

    private CascadeClassifier cascadeClassifier = null;
    private ImageDeteccoes imagedetecoes = null;
    private ArrayList<File> arquivos = null;
    private ArrayList<File> arquivosTotais = null;
    private String caminho;
    private int quantArquivos = 0;
    private ImageDeteccoesDAO imgDao = null;
    private Activity activity;
    private ProgressDialog barProgressDialog;

    //Matrizes inicializadas nesse momento para evitar vazamento de memória
    private Mat mat = new Mat();
    private Mat grayscaleImage = new Mat(this.mat.height(), this.mat.width(), CvType.CV_8UC4);
    private Mat rotacionada = new Mat();

    public ImageUtilsProc (Activity activity, String caminho)
    {
        this.activity = activity;
        this.cascadeClassifier = carregarClassificadorFaces();
        this.imgDao = new ImageDeteccoesDAO(this.activity);
        this.caminho = caminho;
    }

    private CascadeClassifier carregarClassificadorFaces()
    {
        CascadeClassifier cascadeClassifier = null;
        try {
            // Copy the resource into a temp file so OpenCV can load it

            InputStream is = activity.getResources().openRawResource(R.raw.visionary_faces_5050);
            File cascadeDir = activity.getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "visionary_faces_5050.xml");
            FileOutputStream os;
            os = new FileOutputStream(mCascadeFile);


            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1)
            {
                os.write(buffer, 0, bytesRead);
            }

            is.close();
            os.close();

            // Load the cascade classifier
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        }
        catch (Exception e)
        {
            Toast.makeText(activity.getApplicationContext(), "Erro abrindo classifier", Toast.LENGTH_LONG).show();
        }
        return cascadeClassifier;
    } // Carrega o classificador de Faces


    private ArrayList<File> carregarCaminhoFotos(String directoryName)
    {

        ArrayList<File> files = new ArrayList<>();
        try {
            File directory = new File(directoryName);

            File[] fList = directory.listFiles();
            for (File file : fList) {
                if (file.isFile() && (file.toString().contains(".jpg") || file.toString().contains(".JPG"))) {
                    files.add(file);
                } else if (file.isDirectory()) {
                    files.addAll(carregarCaminhoFotos(file.getAbsolutePath()));
                }
            }
        }
        catch (NullPointerException e)
        {

        }
        return files;
    } //Lista todas as imagens de um determinado caminho

    public Mat converteMatrizParaRGB(String caminho)
    {

        Mat rgbMat = new Mat();
        rgbMat = Imgcodecs.imread(caminho);
        Imgproc.cvtColor(this.mat, this.mat, Imgproc.COLOR_BGR2RGB);

        return rgbMat;
    }

    private void lerMatrizBGR(String caminho)
    {
        this.mat = Imgcodecs.imread(caminho);
    }

    private void rotacionarFlip(Mat src, Mat dst, int angle)
    {

        if(angle == 0){
            // Rotate clockwise 270 degrees
            Core.transpose(src, dst);
            Core.flip(dst, dst, 0);

        }else if(angle == 1){
            // Rotate clockwise 180 degrees
            Core.flip(src, dst, -1);

        }else if(angle == 2){
            // Rotate clockwise 90 degrees
            Core.transpose(src, dst);
            Core.flip(dst, dst, 1);

        }else if(angle == 3){
            dst = src;
        }

    } //Rotaciona matrizes

    private void calcularHistograma()
    {

        Mat image = this.mat;
        Mat src = new Mat(image.height(), image.width(), CvType.CV_8UC2);
        Imgproc.cvtColor(image, src, Imgproc.COLOR_RGB2GRAY);

        Vector<Mat> bgr_planes = new Vector<>();
        Core.split(src, bgr_planes);

        MatOfInt histSize = new MatOfInt(256);
        final MatOfFloat histRange = new MatOfFloat(0f, 256f);
        boolean accumulate = false;
        Mat b_hist = new  Mat();
        Imgproc.calcHist(bgr_planes, new MatOfInt(0), new Mat(), b_hist, histSize, histRange, accumulate);

        int hist_w = 512;
        int hist_h = 600;
        long bin_w;
        bin_w = Math.round((double) (hist_w / 256));

        Mat histImage = new Mat(hist_h, hist_w, CvType.CV_8UC1);

        Core.normalize(b_hist, b_hist, 3, histImage.rows(), Core.NORM_MINMAX);

        for (int i = 1; i < 256; i++) {

            Imgproc.line(histImage, new Point(bin_w * (i - 1),hist_h - Math.round(b_hist.get( i-1,0)[0])),
                    new Point(bin_w * (i), hist_h-Math.round(Math.round(b_hist.get(i, 0)[0]))),
                    new Scalar(255, 0, 0), 2, 8, 0);

        }
        src.release();
        image.release();
        histImage.release();
    }

    private double calcularPerceiverBrightness(String caminho)
    {
        Mat mRgb = new Mat();
        mRgb = converteMatrizParaRGB(caminho);

        //Divide as camadas RGB


        Mat mR = new Mat();
        Core.extractChannel(mRgb, mR, 0);
        Mat mG = new Mat();
        Core.extractChannel(mRgb, mG, 1);
        Mat mB = new Mat();
        Core.extractChannel(mRgb, mB, 2);


        Scalar scalar0 = Core.mean(mR);
        Scalar scalar1 = Core.mean(mG);
        Scalar scalar2 = Core.mean(mB);

        double r = scalar0.val[0];
        double g = scalar1.val[0];
        double b = scalar2.val[0];


        mRgb.release();
        mR.release();
        mG.release();
        mB.release();

        final double luminosidadePercebida = Math.sqrt(

                        (r * r * 0.241) +
                        (g * g * 0.691) +
                        (b * b * 0.068)

        );

        double luminosidadePorcento = (luminosidadePercebida * 100) / 255;

        return luminosidadePorcento;
    }

    private double calcularLuminosity(String caminho)
    {
        Mat mRgb = new Mat();
        mRgb = converteMatrizParaRGB(caminho);

        //Divide as camadas RGB


        Mat mR = new Mat();
        Core.extractChannel(mRgb, mR, 0);
        Mat mG = new Mat();
        Core.extractChannel(mRgb, mG, 1);
        Mat mB = new Mat();
        Core.extractChannel(mRgb, mB, 2);


        Scalar scalar0 = Core.mean(mR);
        Scalar scalar1 = Core.mean(mG);
        Scalar scalar2 = Core.mean(mB);

        double r = scalar0.val[0];
        double g = scalar1.val[0];
        double b = scalar2.val[0];


        mRgb.release();
        mR.release();
        mG.release();
        mB.release();


        final double luminosidade = (0.2126 * r + 0.7152 * g + 0.0722 * b);

        double luminosidadePorcento = (luminosidade * 100) / 255;
        return luminosidadePorcento;
    }

    private void transformarEmGreyScale()
    {
        // Create a grayscale image

        Imgproc.cvtColor(this.rotacionada, this.grayscaleImage, Imgproc.COLOR_RGBA2GRAY);

    }

    private int retornarFaces()
    {
        MatOfRect faces = new MatOfRect();
        transformarEmGreyScale();
        // Use the classifier to detect faces
        // The faces will be a 20% of the height of the screen

        int absoluteFaceSize = (int) (this.grayscaleImage.height() * 0.2);
        int quantFaces = 0;
        if (this.cascadeClassifier != null)
        {

            if(!isCancelled()) {
                this.cascadeClassifier.detectMultiScale(this.grayscaleImage, faces, 1.3, 15, Objdetect.CASCADE_DO_CANNY_PRUNING, new Size(50, 50), new Size());
            }


        }
        Rect[] facesArray = faces.toArray();

         //If there are any faces found, draw a rectangle around it
//       for (int i = 0; i < facesArray.length; i++)
//            Imgproc.rectangle(this.mat, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
//
//        ImageUtils a = new ImageUtils();
//        a.salvarImagem(this.mat);

        quantFaces = facesArray.length;
        faces.release();
        return quantFaces;
    }

    @Override
    protected Void doInBackground(Void[] params)
    {
        this.arquivos = carregarCaminhoFotos(this.caminho);

                this.arquivosTotais = new ArrayList<File>();

        this.imgDao.open();
        for (int i = 0; i < arquivos.size(); i++)
        {
            if (!this.imgDao.verificarPorCaminho(this.arquivos.get(i).getAbsolutePath()))  this.arquivosTotais.add( this.arquivos.get(i));
            if (isCancelled())
            {
                imgDao.close();
                break;
            }
        }

        this.imgDao.close();

        if (arquivosTotais.size() == 0)
        {
            publishProgress(2);
        }
        else
        {
            publishProgress(0);
            this.quantArquivos = this.arquivosTotais.size();
            this.barProgressDialog.setMax(quantArquivos);

            for (File f:arquivosTotais)
            {
                if(!isCancelled())
                {
                    lerMatrizBGR(f.getAbsolutePath());
                    double lum = calcularLuminosity(f.getAbsolutePath());
                    this.imagedetecoes = new ImageDeteccoes();
                    this.imagedetecoes.setLuminosidade(lum);

                    if (!mat.empty())
                    {
                        this.imagedetecoes.setCaminho(f.getAbsolutePath());

                        int faces = 0;
                        this.rotacionada = new Mat();
                        this.rotacionada = this.mat;
                        faces = faces + retornarFaces();

                       for (int rot = 0; rot <= 2; rot++)  // Rotaciona a imagem 4 vezes
                        {
                            if (isCancelled()) break;

                            this.rotacionada = new Mat();
                            this.rotacionada = this.mat;
                            rotacionarFlip(this.mat, this.rotacionada, rot);
                            faces = faces + retornarFaces();
                        }
                        this.imagedetecoes.setFaces(faces);

                        File file = new File(this.imagedetecoes.getCaminho());
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        this.imagedetecoes.setData(sdf.format(file.lastModified()));

                        imgDao.open();
                        imgDao.Inserir(this.imagedetecoes);
                        imgDao.close();
                        publishProgress(1);
                    }

                    this.mat.release();
                    this.rotacionada.release();
                    this.grayscaleImage.release();
                }
            }
        }

        return null;
    }

    @Override
    protected void onPreExecute()
    {

        this.barProgressDialog = new ProgressDialog(this.activity);
        this.barProgressDialog.setTitle("Processando imagens");
        this.barProgressDialog.setMessage("Verificando arquivos para processamento...");
        this.barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
        this.barProgressDialog.setCancelable(true);
        this.barProgressDialog.setIndeterminate(true);
        this.barProgressDialog.incrementProgressBy(1);
        this.barProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancel(false);
            }
        });

        this.barProgressDialog.show();

        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void params)
    {

        File f = activity.getDatabasePath("deteccoes.db");
        double size = f.length() / 1024;
        float tamanhoImagens = 0;

        TextView lblTamanhoMB = (TextView) activity.findViewById(R.id.lblTamTotal);
        TextView lblTamanhoBase = (TextView) activity.findViewById(R.id.lblTamanhoBase);
        TextView lblArquivosClassificados = (TextView) activity.findViewById(R.id.lblArquivosClassificados);

        lblTamanhoBase.setText("Tamanho da base: " + String.format("%.2f", size) + " KB");

        imgDao.open();
        lblArquivosClassificados.setText("Arquivos classificados: " + imgDao.retornaCursorConsulta().getCount());
        Cursor cursor = imgDao.retornaCursorConsulta();

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {

            File fSize = new File(cursor.getString(1));
            tamanhoImagens += fSize.length();

            cursor.moveToNext();
        }
        lblTamanhoMB.setText("Tamanho total dos arquivos: " + String.format("%.2f", tamanhoImagens / 1024 / 1024) + " MB");
        imgDao.close();
        this.barProgressDialog.dismiss();

    }

    @Override
    protected void onProgressUpdate(Integer... values)
    {
        switch (values[0])
        {

            case 0:
                this.barProgressDialog.setIndeterminate(false);
            break;

            case 1:
                this.barProgressDialog.setMessage("Classificando " + this.quantArquivos + " arquivos, aguarde por favor");
                this.barProgressDialog.incrementProgressBy(1);
            break;

            case 2:

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage("Não há arquivos ou já foram processados.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();

            break;
        }
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(Void params)
    {

        this.barProgressDialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Processo de classificação interrompido.\n\nArquivos processados: "+ (this.barProgressDialog.getProgress())).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();

        File f = activity.getDatabasePath("deteccoes.db");
        double size = f.length() / 1024;
        TextView lblTamanhoBase = (TextView) activity.findViewById(R.id.lblTamanhoBase);
        lblTamanhoBase.setText("Tamanho da base: " + String.format("%.2f", size) + " KB");

        TextView lblArquivosClassificados = (TextView) activity.findViewById(R.id.lblArquivosClassificados);
        imgDao.open();
        lblArquivosClassificados.setText("Arquivos classificados: " + imgDao.retornaCursorConsulta().getCount());
        imgDao.close();

        super.onCancelled(params);
    }

}

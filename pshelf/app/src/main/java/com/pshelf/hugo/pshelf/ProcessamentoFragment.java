package com.pshelf.hugo.pshelf;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.pshelf.hugo.pshelf.baseutils.ImageDeteccoesDAO;
import com.pshelf.hugo.pshelf.imageutils.ImageDeteccoes;
import com.pshelf.hugo.pshelf.imageutils.ImageUtilsProc;

import java.io.File;


/**
 * Created by HFLopes on 01/03/2016.
 */
public class ProcessamentoFragment extends Fragment {

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.activity_processamento, container, false);
        final EditText e = (EditText) rootView.findViewById(R.id.txtCaminhoDir);
        Button btnProc = (Button) rootView.findViewById(R.id.btnProcessar);
        Button btnEscolhadir = (Button) rootView.findViewById(R.id.btnEscolhaDir);
        Button btnRemoveDetect = (Button) rootView.findViewById(R.id.btnRemoverDeteccoes);

        final File f = getActivity().getDatabasePath("deteccoes.db");
        final TextView lblArquivosClassificados = (TextView)rootView.findViewById(R.id.lblArquivosClassificados);
        final TextView lblTamanhoBase = (TextView)rootView.findViewById(R.id.lblTamanhoBase);
        final TextView lblTamanhoMB = (TextView)rootView.findViewById(R.id.lblTamTotal);
        final ImageDeteccoesDAO imgDao = new ImageDeteccoesDAO(getActivity());


        btnEscolhadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean m_newFolderEnabled = true;
                final String[] m_chosenDir = {""};
                // Create DirectoryChooserDialog and register a callback
                DirectoryChooserDialog directoryChooserDialog =
                        new DirectoryChooserDialog(getActivity(),
                                new DirectoryChooserDialog.ChosenDirectoryListener() {
                                    @Override

                                    public void onChosenDir(String chosenDir) {
                                        m_chosenDir[0] = chosenDir;
                                        e.setText(m_chosenDir[0].toString());
                                        //Toast.makeText(getActivity(), "Chosen directory: " + chosenDir, Toast.LENGTH_LONG).show();
                                    }
                                });
                // Toggle new folder button enabling
                directoryChooserDialog.setNewFolderEnabled(m_newFolderEnabled);
                // Load directory chooser dialog for initial 'm_chosenDir' directory.
                // The registered callback will be called upon final directory selection.
                directoryChooserDialog.chooseDirectory(m_chosenDir[0].toString());
                m_newFolderEnabled = !m_newFolderEnabled;

            }
        });

        btnProc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String caminho = e.getText().toString();
                if (!caminho.isEmpty())
                {

                    ImageUtilsProc task = new ImageUtilsProc(getActivity(), caminho);
                    task.execute();


                } else
                    Toast.makeText(getActivity(), "O caminho da pasta nao pode ser nulo!", Toast.LENGTH_LONG).show();
            }
        });

        btnRemoveDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Tem certeza que deseja apagar todos os registros já classificados?")
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ImageDeteccoesDAO imgDao = new ImageDeteccoesDAO(getActivity());
                                imgDao.open();
                                int count = imgDao.limparTabela();

                                if (count == 0)
                                    Toast.makeText(getActivity(), "Tabela já estava vazia", Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(getActivity(), count + " registros deletados", Toast.LENGTH_LONG).show();
                                File f = getActivity().getDatabasePath("deteccoes.db");
                                double size = f.length() / 1024;

                                lblTamanhoBase.setText("Tamanho da base: " + String.format("%.2f", size) + " KB");
                                imgDao.open();
                                lblArquivosClassificados.setText("Arquivos classificados: " + imgDao.retornaCursorConsulta().getCount());

                                Cursor cursor = imgDao.retornaCursorConsulta();
                                cursor.moveToFirst();
                                float tamanhoImagens = 0;

                                while (!cursor.isAfterLast())
                                {

                                    File fSize = new File(cursor.getString(1));
                                    tamanhoImagens += fSize.length();

                                    cursor.moveToNext();
                                }
                                lblTamanhoMB.setText("Tamanho total dos arquivos: " + String.format("%.2f", tamanhoImagens / 1024 / 1024) + " MB");

                                imgDao.close();

                            }
                        }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();

            }
        });

        float tamanhoImagens = 0;
        double size = f.length() / 1024;



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

        return rootView;
    }
}

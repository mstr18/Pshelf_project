package com.pshelf.hugo.pshelf;


import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.pshelf.hugo.pshelf.adapterutils.LazyGalleryAdapter;
import java.io.File;

/**
 * Created by HFLopes on 23/03/2016.
 */
public class ClassificacaoFragment extends Fragment {


    private LazyGalleryAdapter adapter;

    private void adapterOnclick(final LazyGalleryAdapter adapter)
    {
        adapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id)
            {
                //Toast.makeText(getActivity(), "Item click: " + adapter.getItem(position).getFaces(), Toast.LENGTH_SHORT).show();
                File f = new File(adapter.getItem(position).getCaminho());
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(f), "image/*");
                startActivity(intent);
            }
        });
    }

    public void desmarcarTudo() {
        for (int i = 0; i < adapter.getCount(); i++)
        {
            adapter.setItemChecked(i, false);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.activity_classificacao, container, false);


        final GridView gv = (GridView) rootView.findViewById(R.id.gvFotos);
        final SeekBar skPessoas = (SeekBar) rootView.findViewById(R.id.skPessoas);
        final SeekBar skLuminosidade = (SeekBar) rootView.findViewById(R.id.skLuminosidade);
        final CheckBox chkTodas = (CheckBox) rootView.findViewById(R.id.chkTodas);

        skLuminosidade.setProgress(50);
        skPessoas.setProgress(30);

        final double[] luminosidade = {50};
        final double[] faces = {30};



        adapter = new LazyGalleryAdapter(savedInstanceState, (faces[0]/10)/3, luminosidade[0], getActivity());
        adapter.setAdapterView(gv);
        adapterOnclick(adapter);

        skPessoas.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                faces[0] = progresValue;

                TextView lblPessoas = (TextView) rootView.findViewById(R.id.lblPessoas);

                if ((faces[0]/10)/3 == 0) lblPessoas.setText("nenhuma pessoa");
                else if ((faces[0]/10)/3 > 0 && (faces[0]/10)/3 < 2) lblPessoas.setText("uma ou algumas");
                else if ((faces[0]/10)/3 >= 2) lblPessoas.setText("Algumas pessoas");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


                desmarcarTudo();
                adapter = new LazyGalleryAdapter(savedInstanceState, (faces[0]/10)/3, luminosidade[0], getActivity());
                adapter.setAdapterView(gv);
                adapterOnclick(adapter);

            }
        });

        skLuminosidade.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                luminosidade[0] = progresValue;

                TextView lbluminosidade = (TextView) rootView.findViewById(R.id.lblLuminosidade);
                if (luminosidade[0] <= 30) lbluminosidade.setText("Pouca luminosidade");
                else if (luminosidade[0] >= 31 && luminosidade[0] <= 60)
                    lbluminosidade.setText("Alguma luminosidade");
                else if (luminosidade[0] > 61) lbluminosidade.setText("Muita luminosidade");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


                desmarcarTudo();
                adapter = new LazyGalleryAdapter(savedInstanceState, (faces[0]/10)/3, luminosidade[0], getActivity());
                adapter.setAdapterView(gv);
                adapterOnclick(adapter);

            }
        });


        chkTodas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chkTodas.isChecked()) {

                    desmarcarTudo();
                    adapter = new LazyGalleryAdapter(savedInstanceState, 999, 999, getActivity());
                    adapter.setAdapterView(gv);
                    adapterOnclick(adapter);

                    skLuminosidade.setEnabled(false);
                    skPessoas.setEnabled(false);

                } else {


                    desmarcarTudo();
                    adapter = new LazyGalleryAdapter(savedInstanceState, (faces[0]/10)/3, luminosidade[0], getActivity());
                    adapter.setAdapterView(gv);
                    adapterOnclick(adapter);

                    skLuminosidade.setEnabled(true);
                    skPessoas.setEnabled(true);

                }
            }
        });

        return rootView;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        adapter.save(outState);
    }

}



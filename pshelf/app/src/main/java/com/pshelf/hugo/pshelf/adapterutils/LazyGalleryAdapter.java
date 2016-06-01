package com.pshelf.hugo.pshelf.adapterutils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.manuelpeinado.multichoiceadapter.MultiChoiceBaseAdapter;
import com.pshelf.hugo.pshelf.R;
import com.pshelf.hugo.pshelf.baseutils.ImageDeteccoesDAO;
import com.pshelf.hugo.pshelf.imageutils.ImageDeteccoes;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Hugo Frederico on 04/04/2016.
 */
public class LazyGalleryAdapter extends MultiChoiceBaseAdapter
{

    private List<ImageDeteccoes> data;
    public ImageLoader imageLoader;
    private AlertDialog levelDialog;
    private ImageDeteccoesDAO imgDao;
    private double faces;
    private double luminosidade;
    private Activity a;


    public LazyGalleryAdapter(Bundle savedInstanceState, double faces, double luminosidade, Activity a)
    {
        super(savedInstanceState);
        this.imgDao = new ImageDeteccoesDAO(a);

        imgDao.open();
        this.data=carregarAdapter(imgDao.retornaCursorConsultaEspecifica(faces, luminosidade));
        imgDao.close();

        this.imageLoader=new ImageLoader(a);
        this.faces = faces;
        this.luminosidade = luminosidade;
        this.a = a;
    }

    private void alterar (final Set<Long> selecionados, final ActionMode mode)
    {
        final int[] faces = {999};

        AlertDialog.Builder builder = new AlertDialog.Builder(a);
        builder.setTitle("Quantas faces existem na imagem?");
        builder.setSingleChoiceItems(R.array.faces, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {


                switch (item) {
                    case 0:
                        faces[0] = 0;
                        break;
                    case 1:
                        faces[0] = 1;

                        break;
                    case 2:
                        faces[0] = 2;
                        break;
                    case 3:
                        faces[0] = 3;
                        break;

                }
            }
        });
        builder.setPositiveButton("Alterar", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                if (faces[0] != 999)
                {
                    ImageDeteccoesDAO imgDao = new ImageDeteccoesDAO(getContext());
                    imgDao.open();

                    for (long l : selecionados) {
                        ImageDeteccoes imageDeteccoes = getItem(Integer.parseInt(l + ""));
                        imgDao.alterarFaces(imageDeteccoes, faces[0]);
                    }
                    levelDialog.dismiss();
                    mode.finish();
                    refresh();
                    notifyDataSetChanged();
                    imgDao.close();
                } else
                    Toast.makeText(a, "Nenhuma opção selecionada, \nNenhuma alteração realizada", Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                levelDialog.dismiss();
            }
        });
        levelDialog = builder.create();
        levelDialog.show();

    }

    private void deletar (final Set<Long> selecionados, final ActionMode mode)
    {

        new AlertDialog.Builder(a)
                .setTitle("Deletar arquivos:")
                .setMessage("Tem certeza que deseja apagar o(s) aqruivo(s) selecionado(s)?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        ImageDeteccoesDAO imgDao = new ImageDeteccoesDAO(getContext());
                        imgDao.open();

                        for(long l:selecionados)
                        {
                            ImageDeteccoes imageDeteccoes = getItem(Integer.parseInt(l + ""));
                            File arquivoDeteccoes = new File(imageDeteccoes.getCaminho());
                            imgDao.excluirPorId(imageDeteccoes.getId());
                            arquivoDeteccoes.delete();

                        }
                        mode.finish();
                        refresh();
                        notifyDataSetChanged();
                        imgDao.close();

                    }
                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

    private void compartilhar(Set<Long> selecionados)
    {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Imagens");
        intent.setType("image/jpeg"); /* This example is sharing jpeg images. */

        ArrayList<Uri> filesToSendUri = new ArrayList<Uri>();

        ArrayList<ImageDeteccoes> filesToSend = new ArrayList<ImageDeteccoes>();

        for(long l:selecionados)
        {
            filesToSend.add(getItem(Integer.parseInt(l + "")));
        }

        for(int i = 0; i < filesToSend.size();i++) /* List of the files you want to send */
        {
            File file = new File(filesToSend.get(i).getCaminho());
            Uri uri = Uri.fromFile(file);
            filesToSendUri.add(uri);
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, filesToSendUri);
        a.startActivity(intent);

    }

    private List<ImageDeteccoes> carregarAdapter(Cursor cursor)
    {
        List<ImageDeteccoes> lstImageDeteccoes = new ArrayList<ImageDeteccoes>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            ImageDeteccoes iDeteccoesObj = new ImageDeteccoes();
            iDeteccoesObj.setId(cursor.getInt(0));
            iDeteccoesObj.setCaminho(cursor.getString(1));
            iDeteccoesObj.setFaces(cursor.getInt(2));
            iDeteccoesObj.setLuminosidade(cursor.getDouble(3));
            iDeteccoesObj.setData(cursor.getString(4));

            lstImageDeteccoes.add(iDeteccoesObj);
            cursor.moveToNext();
        }

        //Tenha certeza que você fechou o cursor
        cursor.close();

        return  lstImageDeteccoes;
    }

    private void refresh()
    {
        imgDao.open();
        this.data = carregarAdapter(imgDao.retornaCursorConsultaEspecifica(this.faces, this.luminosidade));
        imgDao.close();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu)
    {

        MenuInflater inflater = mode.getMenuInflater();

        inflater.inflate(R.menu.action_mode, menu);

        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {

        if (item.getItemId() == R.id.menu_share) {

            Set<Long> checkedItems = getCheckedItems();
            compartilhar(checkedItems);

            return true;
        }
        if (item.getItemId() == R.id.menu_discard) {

            Set<Long> checkedItems = getCheckedItems();
            deletar(checkedItems, mode);

            return true;
        }
        if (item.getItemId() == R.id.menu_change) {

            Set<Long> checkedItems = getCheckedItems();
            alterar(checkedItems, mode);

            return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu)
    {
        return false;
    }

    @Override
    public int getCount()
    {
        return data.size();
    }

    @Override
    public ImageDeteccoes getItem(int position)
    {
        return data.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return data.get(position).getId();
    }

    @Override
    protected View getViewImpl(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        if (convertView == null) {
            int layout = R.layout.galeria_item;
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layout, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        ImageView imageView = holder.thumbImg;
        imageLoader.DisplayImage(data.get(position), imageView);
        return imageView;
    }
}
package com.pshelf.hugo.pshelf.baseutils;

import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.pshelf.hugo.pshelf.imageutils.ImageDeteccoes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HFLopes on 16/03/2016.
 */
public class ImageDeteccoesDAO {


        private SQLiteDatabase database;
        private BaseDAO dbHelper;
        private Context context;


        //Campos da tabela Agenda
        private String[] colunas = {BaseDAO.DETECCOES_ID+" _id",
                BaseDAO.DETECCOES_CAMINHO,
                BaseDAO.DETECCOES_FACES,
                BaseDAO.DETECCOES_LUMINOSIDADE,
                BaseDAO.DETECCOES_DATA};


        public ImageDeteccoesDAO(Context context) {
            dbHelper = new BaseDAO(context);
            this.context = context;

        }

        public void open() throws SQLException {
            database = dbHelper.getWritableDatabase();
        }

        public int limparTabela()
        {
            return database.delete(BaseDAO.TBL_DETECCOES, BaseDAO.DETECCOES_ID + " is not null ", null);
        }

        public void close() {
            dbHelper.close();
        }

        public long Inserir(ImageDeteccoes pValue)
        {
            ContentValues values = new ContentValues();

            //Carregar os valores nos campos que será incluído
            values.put(BaseDAO.DETECCOES_CAMINHO, pValue.getCaminho());
            values.put(BaseDAO.DETECCOES_FACES, pValue.getFaces());
            values.put(BaseDAO.DETECCOES_LUMINOSIDADE, pValue.getLuminosidade());
            values.put(BaseDAO.DETECCOES_DATA, pValue.getData());

            Cursor cursor = database.query(BaseDAO.TBL_DETECCOES, colunas,
                    BaseDAO.DETECCOES_CAMINHO.toString() +" LIKE '" + pValue.getCaminho() + "'", null, null, null, BaseDAO.DETECCOES_ID);

            if (cursor.getCount() <= 0)
            {
                cursor.close();
                return database.insert(BaseDAO.TBL_DETECCOES, null, values);
            }
            else
            {
                cursor.close();
                return -1;
            }

        }

        public boolean verificarPorCaminho(String caminho)
        {
            Cursor cursor = database.query(BaseDAO.TBL_DETECCOES, new String[]{"id"},
                    BaseDAO.DETECCOES_CAMINHO.toString() +" LIKE '" + caminho + "'", null, null, null, BaseDAO.DETECCOES_ID);

            if (cursor.getCount() <= 0)
            {
                cursor.close();
                return false;
            }
            else
            {
                cursor.close();
                return true;
            }
        }

        public ImageDeteccoes retornaObjPorId(long id)
        {
            Cursor cursor = database.query(BaseDAO.TBL_DETECCOES, colunas,
                    BaseDAO.DETECCOES_ID + " = '" + id + "'", null, null, null, BaseDAO.DETECCOES_ID);
            cursor.moveToFirst();
            if (cursor.getCount() >0)
            {
                return cursorToDeteccao(cursor);
            }else return null;
        }

        public int alterar(ImageDeteccoes  pValue)
        {

            long id = pValue.getId();
            ContentValues values = new ContentValues();

            //Carregar os novos valores nos campos que serão alterados
            values.put(BaseDAO.DETECCOES_CAMINHO, pValue.getCaminho());
            values.put(BaseDAO.DETECCOES_FACES, pValue.getFaces());
            values.put(BaseDAO.DETECCOES_LUMINOSIDADE, pValue.getLuminosidade());
            values.put(BaseDAO.DETECCOES_DATA, pValue.getData());

            //Alterar o registro com base no ID
            return database.update(BaseDAO.TBL_DETECCOES, values, BaseDAO.DETECCOES_ID + " = " + id, null);
        }

        public int alterarFaces(ImageDeteccoes objDeteccoes, int faces)
        {

            ImageDeteccoes pValue = objDeteccoes;
            ContentValues values = new ContentValues();

            values.put(BaseDAO.DETECCOES_CAMINHO, pValue.getCaminho());
            values.put(BaseDAO.DETECCOES_FACES, faces);
            values.put(BaseDAO.DETECCOES_LUMINOSIDADE, pValue.getLuminosidade());
            values.put(BaseDAO.DETECCOES_DATA, pValue.getData());

            //Alterar o registro com base no ID
            return database.update(BaseDAO.TBL_DETECCOES, values, BaseDAO.DETECCOES_ID + " = " + pValue.getId(), null);
        }

        public void excluirPorId(ImageDeteccoes pValue)
        {
            long id = pValue.getId();

            //Exclui o registro com base no ID
            database.delete(BaseDAO.TBL_DETECCOES, BaseDAO.DETECCOES_ID + " = " + id, null);
        }

        public void excluirPorId(int id)
        {

        //Exclui o registro com base no ID
        database.delete(BaseDAO.TBL_DETECCOES, BaseDAO.DETECCOES_ID + " = " + id, null);
    }

        public List<ImageDeteccoes> Consultar()
        {
            List<ImageDeteccoes> lstImageDeteccoes = new ArrayList<ImageDeteccoes>();

            //Consulta para trazer todos os dados da tabela ordenados pela coluna
            Cursor cursor = database.query(BaseDAO.TBL_DETECCOES, colunas,
                    null, null, null, null, BaseDAO.DETECCOES_ID);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                ImageDeteccoes iDeteccoesObj = cursorToDeteccao(cursor);
                lstImageDeteccoes.add(iDeteccoesObj);
                cursor.moveToNext();
            }

            //Tenha certeza que você fechou o cursor
            cursor.close();
            return  lstImageDeteccoes;
        }

        public Cursor retornaCursorConsulta()
        {
            Cursor cursor = database.query(BaseDAO.TBL_DETECCOES, colunas,
                    null, null, null, null, BaseDAO.DETECCOES_ID);
            return cursor;
        }

        public Cursor retornaCursorConsultaEspecifica(double faces, double luminosidade)
        {
            Cursor cursor;

            if (faces == 0)
            {
                cursor = database.query(BaseDAO.TBL_DETECCOES, colunas,
                        BaseDAO.DETECCOES_FACES +" = '" + faces + "' AND " + BaseDAO.DETECCOES_LUMINOSIDADE +" BETWEEN '" + luminosidade +"' AND '" + (luminosidade + 20) +"'"  , null, null, null, BaseDAO.DETECCOES_DATA);
            }
            else if (faces == 999 && luminosidade == 999)
            {
                cursor = database.query(BaseDAO.TBL_DETECCOES, colunas,
                        null, null, null, null, BaseDAO.DETECCOES_ID);
            }
            else
            {
                cursor = database.query(BaseDAO.TBL_DETECCOES, colunas,
                        BaseDAO.DETECCOES_FACES +" >= '" + faces + "' AND " + BaseDAO.DETECCOES_LUMINOSIDADE +" BETWEEN '" + luminosidade +"' AND '" + (luminosidade + 20) +"'"  , null, null, null, BaseDAO.DETECCOES_DATA);

            }

        return cursor;
    }

        public boolean apagarReferenciaInexistente(ImageDeteccoes detect)
        {

            File f = new File (detect.getCaminho());
            if (!f.exists())
            {
                excluirPorId(detect.getId());
                return false;

            }else return true;

        }

        //Converter o Cursor de dados no objeto
        private ImageDeteccoes cursorToDeteccao(Cursor cursor)
        {
            ImageDeteccoes iDeteccoesObj = new ImageDeteccoes();
            iDeteccoesObj.setId(cursor.getInt(0));
            iDeteccoesObj.setCaminho(cursor.getString(1));
            iDeteccoesObj.setFaces(cursor.getInt(2));
            iDeteccoesObj.setLuminosidade(cursor.getDouble(3));
            iDeteccoesObj.setData(cursor.getString(4));
            return iDeteccoesObj;
        }
}



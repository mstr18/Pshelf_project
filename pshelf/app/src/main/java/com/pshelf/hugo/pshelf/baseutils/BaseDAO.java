package com.pshelf.hugo.pshelf.baseutils;

/**
 * Created by HFLopes on 16/03/2016.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



//Classe responsável pela criação do Banco de Dados e tabelas

public class BaseDAO extends SQLiteOpenHelper {


    public static final String TBL_DETECCOES = "deteccoes";
    public static final String DETECCOES_ID = "id";
    public static final String DETECCOES_CAMINHO = "caminho";
    public static final String DETECCOES_FACES = "faces";
    public static final String DETECCOES_LUMINOSIDADE = "luminosidade";
    public static final String DETECCOES_DATA = "data";


    private static final String DATABASE_NAME = "deteccoes.db";
    private static final int DATABASE_VERSION = 1;

    //Estrutura da tabela Detecçoes (sql statement)
    private static final String CREATE_DETECCOES = "create table " +
            TBL_DETECCOES + "( " + DETECCOES_ID      + " integer primary key autoincrement, " +
            DETECCOES_CAMINHO     + " text not null, " +
            DETECCOES_FACES + " integer not null, " +
            DETECCOES_LUMINOSIDADE + " double not null, " +
            DETECCOES_DATA + " date not null);";

    public BaseDAO(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        //Criação da tabela
        database.execSQL(CREATE_DETECCOES);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Caso seja necessário mudar a estrutura da tabela
        //deverá primeiro excluir a tabela e depois recriá-la
        db.execSQL("DROP TABLE IF EXISTS " + TBL_DETECCOES);
        onCreate(db);
    }

}
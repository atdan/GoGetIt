package com.example.root.gogetit.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.example.root.gogetit.model.Order;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteAssetHelper {

    private static final String DB_NAME = "EatItDB.db";
    private static final int DB_VERSION = 1;
    public Database(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public List<Order> getCarts(){
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = {"ProductName","ProductId","Quantity","Price","Discount"};

        String sqlTable = "OrderDetail";
        qb.setTables(sqlTable);

        Cursor cursor = qb.query(db,sqlSelect,null,null,null,null,null);
        final List<Order> result = new ArrayList<>();
        if (cursor.moveToFirst()){
            do {
                result.add(new Order(cursor.getString(cursor.getColumnIndex("ProductId")),
                        cursor.getString(cursor.getColumnIndex("ProductName")),
                        cursor.getString(cursor.getColumnIndex("Quantity")),
                        cursor.getString(cursor.getColumnIndex("Price")),
                        cursor.getString(cursor.getColumnIndex("Discount")))
                        );
            }while (cursor.moveToNext());
        }
        return result;
    }

    public void addToCart(Order order){
        SQLiteDatabase db = getReadableDatabase();
        String query =String.format("INSERT INTO OrderDetail(ProductId,ProductName,Quantity,Price,Discount) " +
                "VALUES('%s','%s','%s','%s','%s');",
                order.getProductId(),
                order.getProductName(),
                order.getQuantity(),
                order.getPrice(),
                order.getDiscount());
        db.execSQL(query);
    }

    public void cleanCart(){
        SQLiteDatabase db = getReadableDatabase();
        String query =String.format("DELETE FROM OrderDetail");
//                order.getProductId(),
//                order.getProductName(),
//                order.getQuantity(),
//                order.getPrice(),
//                order.getDiscount());
        db.execSQL(query);
    }

    //write function to update and delete favourites

    public void addToFvourites(String foodId){
        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("INSERT INTO Favourites(FoodId) VALUES ('%s');",foodId);
        database.execSQL(query);
    }

    public void remveFromFvourites(String foodId){
        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("DELETE FROM Favourites WHERE FoodId ='%s';",foodId);
        database.execSQL(query);
    }

    public boolean isFvourite(String foodId){
        SQLiteDatabase database = getReadableDatabase();
        String query = String.format("SELECT * FROM Favourites WHERE FoodId='%s';",foodId);
        Cursor cursor = database.rawQuery(query,null);
        if (cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }
}

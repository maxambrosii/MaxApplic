package com.maxcompany.maxapplic;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button createDatabaseButton, addRecordButton, showRecordsButton, deleteDatabaseButton, deleteRecordButton;
    private SQLiteDatabase database;
    private LinearLayout recordsLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createDatabaseButton = findViewById(R.id.createDatabaseButton);
        addRecordButton = findViewById(R.id.addRecordButton);
        showRecordsButton = findViewById(R.id.showRecordsButton);
        deleteDatabaseButton = findViewById(R.id.deleteDatabaseButton);
        recordsLayout = findViewById(R.id.recordsLayout);
        deleteRecordButton = findViewById(R.id.deleteRecordButton);
        deleteRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteRecordDialog();
            }
        });

        createDatabaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDatabase();
            }
        });

        addRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (database == null) {
                    createDatabase();
                }
                showAddRecordDialog();
            }
        });

        showRecordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecords();
            }
        });

        deleteDatabaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDatabase();
            }
        });
        if(isDatabaseCreated()){
            createDatabaseButton.setEnabled(false);
        }
    }

    private void showDeleteRecordDialog() {
        if(isDatabaseCreated()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Удалить запись");

            final EditText idInput = new EditText(this);
            idInput.setHint("ID студента");

            builder.setView(idInput);

            builder.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String idStr = idInput.getText().toString();
                    try {
                        int id = Integer.parseInt(idStr);
                        if (deleteRecord(id)) {
                            showMessage("Запись с ID " + id + " удалена.");
                        } else {
                            showMessage("Записи с ID " + id + " не существует.");
                        }
                    } catch (NumberFormatException e) {
                        showMessage("Введите корректный ID студента.");
                    }
                }
            });

            builder.setNegativeButton("Отмена", null);
            builder.show();
        } else {
            showMessage("Базы данных не существует!");
        }
    }

    private boolean deleteRecord(int id) {
        int rowsAffected = database.delete("student", "id=?", new String[]{String.valueOf(id)});
        return rowsAffected > 0;
    }


    private boolean isDatabaseCreated() {
        SQLiteDatabase tempDatabase = null;
        try {
            tempDatabase = openOrCreateDatabase("Students", Context.MODE_PRIVATE, null);
            tempDatabase.execSQL("CREATE TABLE IF NOT EXISTS student (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, age INTEGER);");
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (tempDatabase != null) {
                tempDatabase.close();
            }
        }
    }
    private void createDatabase() {
        database = openOrCreateDatabase("Students", Context.MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE IF NOT EXISTS student (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, age INTEGER);");
        if(createDatabaseButton.isEnabled()) {
            createDatabaseButton.setEnabled(false);
            showMessage("База успешно создана.");
        }
    }

    private void showAddRecordDialog() {
        createDatabase();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить запись");

        final EditText nameInput = new EditText(this);
        nameInput.setHint("Имя");
        final EditText ageInput = new EditText(this);
        ageInput.setHint("Возраст");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(nameInput);
        layout.addView(ageInput);

        builder.setView(layout);

        builder.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameInput.getText().toString();
                int age = Integer.parseInt(ageInput.getText().toString());
                addRecord(name, age);
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void addRecord(String name, int age) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("age", age);
        database.insert("student", null, values);
    }

    private void showRecords() {
        if(isDatabaseCreated()) {
            recordsLayout.removeAllViews();
            Cursor cursor = database.rawQuery("SELECT id, name, age FROM student", null);
            StringBuilder records = new StringBuilder();

            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                int age = cursor.getInt(2);
                records.append("ID: ").append(id).append(", Имя: ").append(name).append(", Возраст: ").append(age).append("\n");
            }

            cursor.close();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Список записей");
            if(records != null)
                builder.setMessage(records.toString());
            else  builder.setMessage("Not found records");
            builder.setPositiveButton("Закрыть", null);
            AlertDialog recordsDialog = builder.create();
            recordsDialog.show();
        } else {
            showMessage("Записей нет, база не создана");
        }
    }
    private void deleteDatabase() {

        if (database != null) {
            database.execSQL("DROP TABLE IF EXISTS student;");
            database.close();
            createDatabaseButton.setEnabled(true);
            showMessage("База удалена.");
        } else {
            createDatabase();
            deleteDatabase();
        }
    }

    private void showMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}

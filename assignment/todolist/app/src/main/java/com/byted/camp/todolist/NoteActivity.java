package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteActivity extends AppCompatActivity {

    private EditText editText;
    private Button addBtn;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        setTitle(R.string.take_a_note);

        editText = findViewById(R.id.edit_text);
        editText.setFocusable(true);
        editText.requestFocus();
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(editText, 0);
        }

        addBtn = findViewById(R.id.btn_add);
        spinner=findViewById(R.id.priority);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence content = editText.getText();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(NoteActivity.this,
                            "No content to add", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean succeed = saveNote2Database(content.toString().trim());
                if (succeed) {
                    Toast.makeText(NoteActivity.this,
                            "Note added", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                } else {
                    Toast.makeText(NoteActivity.this,
                            "Error", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean saveNote2Database(String content) {
        // TODO 插入一条新数据，返回是否插入成功
        TodoDbHelper todoDbHelper=new TodoDbHelper(this);
        SQLiteDatabase db=todoDbHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        String spinnerPriority=spinner.getSelectedItem().toString();
        int priority=0;
        switch (spinnerPriority){
            case "LOW":priority=1;break;
            case "MID":priority=2;break;
            case "HIGH":priority=3;break;
            default:priority=0;
        }
        values.put(TodoContract.TodoEntry.Priority,priority);
        values.put(TodoContract.TodoEntry.Content,content);
        values.put(TodoContract.TodoEntry.State, 0);
        DateFormat format=new SimpleDateFormat(TodoContract.TodoEntry.DateFormat, Locale.ENGLISH);
        Date curDate=new Date(System.currentTimeMillis());
        String str=format.format(curDate);
        values.put(TodoContract.TodoEntry.Date,str);
        long newRowID=db.insert(TodoContract.TodoEntry.TABLE_NAME,null,values);
        if(newRowID>0)
            return true;
        else
            return false;
    }
}

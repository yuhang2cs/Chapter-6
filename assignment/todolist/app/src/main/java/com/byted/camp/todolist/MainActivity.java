package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.operation.activity.DatabaseActivity;
import com.byted.camp.todolist.operation.activity.DebugActivity;
import com.byted.camp.todolist.operation.activity.SettingActivity;
import com.byted.camp.todolist.operation.db.FeedReaderContract;
import com.byted.camp.todolist.ui.NoteListAdapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD = 1002;

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(MainActivity.this, NoteActivity.class),
                        REQUEST_CODE_ADD);
            }
        });

        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) {
                MainActivity.this.deleteNote(note);
            }

            @Override
            public void updateNote(Note note) {
                MainActivity.this.updateNode(note);
            }
        });
        recyclerView.setAdapter(notesAdapter);

        notesAdapter.refresh(loadNotesFromDatabase());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            case R.id.action_database:
                startActivity(new Intent(this, DatabaseActivity.class));
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

    private List<Note> loadNotesFromDatabase() {
        // TODO 从数据库中查询数据，并转换成 JavaBeans
        TodoDbHelper todoDbHelper=new TodoDbHelper(this);
        SQLiteDatabase db=todoDbHelper.getReadableDatabase();
        List<Note> notes=null;
        String[] projection = {
                BaseColumns._ID,
                TodoContract.TodoEntry.State,
                TodoContract.TodoEntry.Date,
                TodoContract.TodoEntry.Content,
                TodoContract.TodoEntry.Priority
        };


        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                TodoContract.TodoEntry.Priority+" DESC,"+
                TodoContract.TodoEntry.State;

        Cursor cursor = db.query(
                TodoContract.TodoEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        if(cursor!=null && cursor.getCount()>0)
        {
            notes=new ArrayList<>();
            while (cursor.moveToNext()) {
                Note note=new Note(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)));
                note.setContent(cursor.getString(cursor.getColumnIndex(TodoContract.TodoEntry.Content)));
                note.setDate(getDate(cursor, TodoContract.TodoEntry.Date));
                note.setPriority(cursor.getInt(cursor.getColumnIndex(TodoContract.TodoEntry.Priority)));
                int state=cursor.getInt(cursor.getColumnIndex(TodoContract.TodoEntry.State));
                if(state==1)
                    note.setState(State.DONE);
                else
                    note.setState(State.TODO);
                notes.add(note);
            }
            cursor.close();
        }
        db.close();
        return notes;
    }
    public static Date getDate(Cursor cursor, String columnName) {
        String dateString = cursor.getString(cursor
                .getColumnIndex(columnName));
        if (dateString == null) {
            return null;
        }//  w  w w  . j  a  v  a 2  s  .co m
        DateFormat format = new SimpleDateFormat(TodoContract.TodoEntry.DateFormat,
                Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private void deleteNote(Note note) {
        // TODO 删除数据
        TodoDbHelper todoDbHelper=new TodoDbHelper(this);
        SQLiteDatabase db=todoDbHelper.getWritableDatabase();
        String selection = BaseColumns._ID + " = ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = {Long.toString(note.id)};
        // Issue SQL statement.
        int deletedRows = db.delete(TodoContract.TodoEntry.TABLE_NAME, selection, selectionArgs);
        notesAdapter.refresh(loadNotesFromDatabase());
    }

    private void updateNode(Note note) {
        // 更新数据
        TodoDbHelper todoDbHelper=new TodoDbHelper(this);
        SQLiteDatabase db=todoDbHelper.getWritableDatabase();
        String selection = BaseColumns._ID + " = ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = {Long.toString(note.id)};
        // Issue SQL statement.
        ContentValues values=new ContentValues();
        values.put(TodoContract.TodoEntry.Priority,note.getPriority());
        values.put(TodoContract.TodoEntry.Content,note.getContent());
        DateFormat format=new SimpleDateFormat(TodoContract.TodoEntry.DateFormat, Locale.ENGLISH);
        String str=format.format(note.getDate());
        values.put(TodoContract.TodoEntry.Date,str);
        if(note.getState()==State.DONE)
            values.put(TodoContract.TodoEntry.State,1);
        else
            values.put(TodoContract.TodoEntry.State,0);
        db.update(TodoContract.TodoEntry.TABLE_NAME,values,selection,selectionArgs);
        notesAdapter.refresh(loadNotesFromDatabase());
    }

}

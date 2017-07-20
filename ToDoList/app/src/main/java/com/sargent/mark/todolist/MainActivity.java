package com.sargent.mark.todolist;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;


import com.sargent.mark.todolist.data.Contract;
import com.sargent.mark.todolist.data.DBHelper;

public class MainActivity extends AppCompatActivity implements AddToDoFragment.OnDialogCloseListener, UpdateToDoFragment.OnUpdateDialogCloseListener{

    private RecyclerView rv;
    private FloatingActionButton button;
    private DBHelper helper;
    private Cursor cursor;
    private SQLiteDatabase db;
    ToDoListAdapter adapter;
    private final String TAG = "mainactivity";
    //private View currentListItem; //this was supposed to keep track of the current view but didn't work so I used bind in the todolistadapter
    private Menu menu; //I will use this menu to place the spinner

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "oncreate called in main activity");
        button = (FloatingActionButton) findViewById(R.id.addToDo);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                AddToDoFragment frag = new AddToDoFragment();
                frag.show(fm, "addtodofragment");
            }
        });
        rv = (RecyclerView) findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
     public boolean onCreateOptionsMenu(Menu menu) //this inflates the menu where the category choices will be shown
    {
        getMenuInflater().inflate(R.menu.filter_menu, menu);
        this.menu = menu;
        return true;
    }

    private Cursor filterCategory(SQLiteDatabase db, String category){ //for some reason this doesn't work :(
        return db.query(
            Contract.TABLE_TODO.TABLE_NAME, null, Contract.TABLE_TODO.COLUMN_NAME_CATEGORY + "=" + "'" + category + "'",
                null,
                null,
                null,
                null,
                null);
            }

    @Override
     public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.all:
                cursor = getAllItems(db); //no filter
                break;
            case R.id.school:
                cursor = filterCategory(db, "School"); //filter by school
                break;
            case R.id.work:
                cursor = filterCategory(db, "Work"); //filter by work
                break;
            case R.id.groceries:
                cursor = filterCategory(db, "Groceries"); //filter by groceries
                break;
            case R.id.relationship:
                cursor = filterCategory(db, "Relationship"); //filter by relationship
                break;
            case R.id.bills:
                cursor = filterCategory(db, "Bills"); //filter by bills
                break;
            default:
                break;
        }
        //cursor = getAllItems(db);
        adapter.swapCursor(cursor); //this will refresh the cursor so that the view shows the filtered cursor
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (db != null) db.close();
        if (cursor != null) cursor.close();
    }

    @Override
    protected void onStart() {
        super.onStart();

        helper = new DBHelper(this);
        db = helper.getWritableDatabase();
        cursor = getAllItems(db);

        adapter = new ToDoListAdapter(cursor, new ToDoListAdapter.ItemClickListener() {

            @Override
            public void onItemClick(int pos, String description, String duedate, long id, View thisListItem) {
                Log.d(TAG, "item click id: " + id);

                String[] dateInfo = duedate.split("-");
                int year = Integer.parseInt(dateInfo[0].replaceAll("\\s",""));
                int month = Integer.parseInt(dateInfo[1].replaceAll("\\s",""));
                int day = Integer.parseInt(dateInfo[2].replaceAll("\\s",""));

                FragmentManager fm = getSupportFragmentManager();

                UpdateToDoFragment frag = UpdateToDoFragment.newInstance(year, month, day, description, id);
                frag.show(fm, "updatetodofragment");
                //currentListItem = thisListItem;//this will save the current position of the recycler view item so that we can color it later
            }
        });

        rv.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                long id = (long) viewHolder.itemView.getTag();
                Log.d(TAG, "passing id: " + id);
                removeToDo(db, id);
                adapter.swapCursor(getAllItems(db));
            }
        }).attachToRecyclerView(rv);
    }

    @Override
    public void closeDialog(int year, int month, int day, String description, String category, int isDone) {
        addToDo(db, description, formatDate(year, month, day), category, isDone);
        cursor = getAllItems(db);
        adapter.swapCursor(cursor); //changes the cursor so that all categories are shown
    }

    public String formatDate(int year, int month, int day) {
        return String.format("%04d-%02d-%02d", year, month + 1, day);
    }



    private Cursor getAllItems(SQLiteDatabase db) {
        return db.query(
                Contract.TABLE_TODO.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE
        );
    }
    //added a category string and an isDone int to the to do database.
    private long addToDo(SQLiteDatabase db, String description, String duedate, String category, int isDone) {
        ContentValues cv = new ContentValues();
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION, description);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE, duedate);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY, category);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_COMPLETED, isDone);
        return db.insert(Contract.TABLE_TODO.TABLE_NAME, null, cv);
    }

    private boolean removeToDo(SQLiteDatabase db, long id) {
        Log.d(TAG, "deleting id: " + id);
        return db.delete(Contract.TABLE_TODO.TABLE_NAME, Contract.TABLE_TODO._ID + "=" + id, null) > 0;
    }


    private int updateToDo(SQLiteDatabase db, int year, int month, int day, String description, long id, String category, int isDone){

        String duedate = formatDate(year, month - 1, day);

        ContentValues cv = new ContentValues();
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION, description);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE, duedate);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY, category); //added category column to put into content value
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_COMPLETED, isDone); //added isdone column to put into content value

        return db.update(Contract.TABLE_TODO.TABLE_NAME, cv, Contract.TABLE_TODO._ID + "=" + id, null);
    }

    @Override
    public void closeUpdateDialog(int year, int month, int day, String description, long id, String category, int isDone, boolean isCompleted) {


        if(isCompleted) //if checkbox is checked...
        {
            //go into the db and change the value of isDone to true for id = id
            isDone = 1;
            //this.rv.setBackgroundColor(isCompleted ? 0xFFAED581 : 0xFF0000);
            //currentListItem.setBackgroundColor(Color.RED);


        }
        else{
            isDone = 0;
            //this.rv.setBackgroundColor(isCompleted ? 0xFFAED581 : 0x000000);
            //currentListItem.setBackgroundColor(Color.TRANSPARENT);

        }

        updateToDo(db, year, month, day, description, id, category, isDone); //updates query
        adapter.swapCursor(getAllItems(db));
    }
}

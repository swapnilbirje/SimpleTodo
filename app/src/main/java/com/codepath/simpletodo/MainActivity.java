package com.codepath.simpletodo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.simpletodo.database.PostsDatabaseHelper;
import com.codepath.simpletodo.model.Item;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> items;
    ArrayAdapter<String> itemsAdapter;
    ListView lvItems;
    private final int REQUEST_CODE = 200;
    PostsDatabaseHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_simple_todo);
        setSupportActionBar(toolbar);
        // In any activity just pass the context and use the singleton method
        helper = PostsDatabaseHelper.getInstance(this);
        lvItems = (ListView) findViewById(R.id.lvItems);
        updateListView();

        //setup listener for long click - item removal
        setupListViewListener();
        //setup listener for onClick - Edit item
        setupOnItemClickListener();
    }

    private void updateListView() {
        items = (ArrayList<String>)helper.getAllItems();
        itemsAdapter =  new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, items);
        lvItems.setAdapter(itemsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onAddItem(View v){
        EditText etNewItem = (EditText) findViewById(R.id.etNewItem);
        Item newItem = new Item();
        newItem.setText(etNewItem.getText().toString());
        etNewItem.setText("");
        if(helper.addPost(newItem))
            Toast.makeText(this, getString(R.string.item_added), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, getString(R.string.enter_valid_item), Toast.LENGTH_SHORT).show();
        updateListView();

    }

    //Remove item from To-Do list
    private void setupListViewListener(){
        lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View item, int position, long id) {
                TextView txView = (TextView) item;
                if(helper.deleteItem(new String[]{txView.getText().toString()}))
                    Toast.makeText(getApplicationContext(), getString(R.string.item_deleted), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), getString(R.string.error_delete), Toast.LENGTH_SHORT).show();
                updateListView();
                return true;
            }
        });
    }

    //onClick listener - Edit screen
    private void setupOnItemClickListener(){
        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(MainActivity.this, EditItemActivity.class);
                i.putExtra("position", position);
                i.putExtra("item", items.get(position));
                startActivityForResult(i, REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // REQUEST_CODE is defined above
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            updateListView();
        }
    }

}

package com.codepath.simpletodo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.simpletodo.database.PostsDatabaseHelper;
import com.codepath.simpletodo.model.Item;

public class EditItemActivity extends AppCompatActivity {
    int position;
    String item;
    PostsDatabaseHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);
        // In any activity just pass the context and use the singleton method
        helper = PostsDatabaseHelper.getInstance(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_edit_card);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        item = getIntent().getStringExtra("item");
        position = getIntent().getIntExtra("position",0);

        EditText edText = (EditText) findViewById(R.id.editText);
        edText.setText(item);

    }

    public void onSaveItem(View v){
        Item etItem = new Item();
        EditText etText = (EditText) findViewById(R.id.editText);
        etItem.setText(etText.getText().toString());

        if(helper.addOrUpdateUser(etItem, item))
            Toast.makeText(getApplicationContext(), getString(R.string.item_edited), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(), getString(R.string.error_edit), Toast.LENGTH_SHORT).show();

        Intent data = new Intent();
        // Pass relevant data back as a result
        data.putExtra("code", 200); // ints work too
        setResult(RESULT_OK, data); // set result code and bundle data for response
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

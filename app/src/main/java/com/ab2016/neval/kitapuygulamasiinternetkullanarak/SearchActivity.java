package com.ab2016.neval.kitapuygulamasiinternetkullanarak;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.widget.AdapterView.OnItemClickListener;

public class SearchActivity extends Activity implements OnItemClickListener {

	private IntentFilter mIntentFilter;

	private ArrayList<String> resultBooks;
	private ListView mListView;
	private EditText et;
	private DBAdapter db;

	Cursor c = null;
	Bitmap mBitmap = null;
	AlertDialog.Builder myDialogBox = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_search);

		mListView = (ListView) findViewById(R.id.lvSearchResult);
		mListView.setOnItemClickListener(this);

		et = (EditText) findViewById(R.id.key);
		db = new DBAdapter(this);

		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction("IMAGE_DOWNLOADED_ACTION");
		// Register the receiver
		registerReceiver(mIntentReceiver, mIntentFilter);

	}

	public void onClick(View view) {
		String key = et.getText().toString();
		resultBooks = new ArrayList();
		db.open();
		Cursor c = db.getBooks(key);
		if (c.moveToFirst()) {
			do {
				resultBooks.add(c.getString(1));
			} while (c.moveToNext());
		}
		db.close();
		ArrayAdapter<String> mListAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, resultBooks);
		mListView.setAdapter(mListAdapter);
	}

	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {
		String rname = ((TextView) view).getText().toString();

		db = new DBAdapter(this);

		db.open();
		c = db.getBooks(rname);
		c.moveToFirst();
		db.close();

		Log.d("IMAGE NAME", c.getString(4));
		Intent intent = new Intent(getBaseContext(), MyIntentService.class);
		intent.putExtra("imgname", c.getString(4));
		startService(intent);

		/********************/
	}

	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Storing the received data into a Bundle
			Bundle mBundle = intent.getExtras();
			byte[] mByte = mBundle.getByteArray("image");

			myDialogBox = new AlertDialog.Builder(SearchActivity.this);

			Drawable drawable;
			// Converting the byte array into a Bitmap
			mBitmap = BitmapFactory.decodeByteArray(mByte, 0, mByte.length);
			drawable = new BitmapDrawable(context.getResources(), mBitmap);
			myDialogBox.setIcon(drawable);
			// Log.d("dialog image", "dialog imageset edildi");

			// set message, title, and icon
			myDialogBox.setTitle(c.getString(1));
			myDialogBox.setMessage("Author :" + c.getString(2)
					+ "\n\nPublishYear: " + c.getString(3));

			// Set three option buttons
			myDialogBox.setPositiveButton("Close",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// whatever should be done when answering "YES" goes
							// here

						}
					});

			myDialogBox.create();
			myDialogBox.show();

		}
	};

}

package com.ab2016.neval.kitapuygulamasiinternetkullanarak;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

public class MyIntentService extends IntentService {
	// One needs to implement a constructor for the class and call its
	// superclass
	// with the name of the intent service (setting it with a string):
	public MyIntentService() {
		super("MyIntentService");
	}

	// The onHandleIntent() method is where you place the code that needs to be
	// executed on
	// a separate thread, such as downloading a file from a server. When the
	// code has
	// finished executing, the thread is terminated and the service is stopped
	// automatically.
	protected void onHandleIntent(Intent intent) {

		String imgname = intent.getStringExtra("imgname");

		imgname = "http://www.ctis.bilkent.edu.tr/ctis487/jsonBook/" + imgname;
		
		Log.d("Serviceimgname", imgname);
		
		//imgname = "http://www.ctis.bilkent.edu.tr/ctis_foto/5402.jpg";
		Log.d("IntentService", "Service Started");
		
		Log.d("Serviceimgname", imgname);
		
		try {
			// Downloading a Bitmap
			Bitmap mBitmap = DownloadImage(new URL(imgname));

			Log.d("IntentService", "Bitmap is downloaded");

			// Converting the Bitmap to byte array
			ByteArrayOutputStream mByteArrayOutputStream = new ByteArrayOutputStream();
			mBitmap.compress(Bitmap.CompressFormat.JPEG, 100,
					mByteArrayOutputStream);
			byte[] mByte = mByteArrayOutputStream.toByteArray();

			// Send the Bitmap using a broadcast to inform the activity about
			// the data
			Intent broadcastIntent = new Intent();
			broadcastIntent.putExtra("image", mByte);

			broadcastIntent.setAction("IMAGE_DOWNLOADED_ACTION");
			getBaseContext().sendBroadcast(broadcastIntent);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		Log.d("IntentService", "Service will be terminated");
	}

	// Or method
	private Bitmap DownloadImage(URL url) {
		Bitmap bitmap = null;
		InputStream in = null;
		try {
			// Custom Method
			in = OpenHttpConnection(url);

			// Creating a Bitmap from InputStream
			bitmap = BitmapFactory.decodeStream(in);
			in.close();
		} catch (IOException e1) {
			Toast.makeText(this, e1.getLocalizedMessage(), Toast.LENGTH_LONG)
					.show();
			e1.printStackTrace();
		}
		return bitmap;
	}

	private InputStream OpenHttpConnection(URL url) throws IOException {
		InputStream in = null;
		int response = -1;

		URL mURL = url;
		URLConnection conn = mURL.openConnection();

		if (!(conn instanceof HttpURLConnection))
			throw new IOException("Not an HTTP connection");
		try {
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			// Sets allowUserInteraction. Unused by Android.
			httpConn.setAllowUserInteraction(false);

			// Sets whether this connection follows redirects
			httpConn.setInstanceFollowRedirects(true);

			// Sets the request command which will be sent to the remote HTTP
			// server.
			// This method can only be called before the connection is made.
			httpConn.setRequestMethod("GET");

			// Opens a connection to the resource.
			httpConn.connect();

			// Obtaining the response code returned by the remote HTTP server.
			// -1 corresponds to an invalid response code
			response = httpConn.getResponseCode();

			if (response == HttpURLConnection.HTTP_OK) {
				in = httpConn.getInputStream();
			}
		} catch (Exception ex) {
			throw new IOException("Error connecting");
		}
		return in;
	}
}

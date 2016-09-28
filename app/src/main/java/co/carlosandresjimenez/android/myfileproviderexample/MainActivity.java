package co.carlosandresjimenez.android.myfileproviderexample;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int PICK_IMAGE_REQUEST = 0;

    private static final String FILE_PROVIDER_AUTHORITY = "co.carlosandresjimenez.android.myfileprovider";

    private ImageView mImageView;
    private TextView mTextView;

    private Uri mUri;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTextView = (TextView) findViewById(R.id.image_uri);
        mImageView = (ImageView) findViewById(R.id.image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_email_opt1) {
            sedEmailOpt1();
            return true;
        }
        if (id == R.id.action_send_email_opt2) {
            sedEmailOpt2();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openImageSelector(View view) {
        Intent intent;
        Log.e(LOG_TAG, "While is set and the ifs are worked through.");

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        // Show only images, no videos or anything else
        Log.e(LOG_TAG, "Check write to external permissions");

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Log.i(LOG_TAG, "Received an \"Activity Result\"");
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mUri.toString());

                mTextView.setText(mUri.toString());
                mBitmap = getBitmapFromUri(mUri);
                mImageView.setImageBitmap(mBitmap);
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error closing ParcelFile Descriptor");
            }
        }
    }

    // Recommended option found here: https://github.com/crlsndrsjmnz/MyShareImageExample
    private void sedEmailOpt1() {
        if (mUri != null) {
            String filename = getFilePath();
            saveBitmapToFile(getCacheDir(), filename, mBitmap, Bitmap.CompressFormat.JPEG, 100);
            File imagePath = new File(getCacheDir(), filename);

            Uri uriToImage = FileProvider.getUriForFile(
                    this, FILE_PROVIDER_AUTHORITY, imagePath);

            String subject = "URI Example";
            String stream = "Hello! \n"
                    + "Uri example" + ".\n"
                    + "Uri: " + uriToImage.toString() + "\n";

            Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                    .setStream(uriToImage)
                    .setSubject(subject)
                    .setText(stream)
                    .getIntent();

            // Provide read access
            shareIntent.setData(uriToImage);
            shareIntent.setType("message/rfc822");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share with"));

        } else {
            Snackbar.make(mTextView, "Image not selected", Snackbar.LENGTH_LONG)
                    .setAction("Select", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openImageSelector(view);
                        }
                    }).show();
        }
    }

    // Recommended option found here: https://github.com/crlsndrsjmnz/MyShareImageExample
    private void sedEmailOpt2() {
        if (mUri != null) {
            String filename = getFilePath();
            saveBitmapToFile(getCacheDir(), filename, mBitmap, Bitmap.CompressFormat.JPEG, 100);
            File imagePath = new File(getCacheDir(), filename);

            Uri uriToImage = FileProvider.getUriForFile(
                    this, FILE_PROVIDER_AUTHORITY, imagePath);

            String subject = "URI Example";
            String stream = "Hello! \n"
                    + "Uri example" + ".\n"
                    + "Uri: " + uriToImage.toString() + "\n";

            Intent orderIntent = new Intent(Intent.ACTION_SENDTO);
            orderIntent.setData(Uri.parse("mailto:"));
            orderIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            orderIntent.putExtra(Intent.EXTRA_TEXT, stream);
            orderIntent.putExtra(Intent.EXTRA_STREAM, uriToImage);
            orderIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Solution taken from http://stackoverflow.com/a/18332000/3346625
            List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(orderIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                grantUriPermission(packageName, uriToImage, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            startActivity(Intent.createChooser(orderIntent, "Share with"));

        } else {
            Snackbar.make(mTextView, "Image not selected", Snackbar.LENGTH_LONG)
                    .setAction("Select", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openImageSelector(view);
                        }
                    }).show();
        }
    }

    public String getFilePath() {
        /*
         * Get the file's content URI from the incoming Intent,
         * then query the server app to get the file's display name
         * and size.
         */
        Cursor returnCursor =
                getContentResolver().query(mUri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);

        /*
         * Get the column indexes of the data in the Cursor,
         * move to the first row in the Cursor, get the data,
         * and display it.
         */
        returnCursor.moveToFirst();
        String fileName = returnCursor.getString(returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

        return fileName;
    }

    /*
    * Bitmap.CompressFormat can be PNG,JPEG or WEBP.
    *
    * quality goes from 1 to 100. (Percentage).
    *
    * dir you can get from many places like Environment.getExternalStorageDirectory() or mContext.getFilesDir()
    * depending on where you want to save the image.
    */
    public boolean saveBitmapToFile(File dir, String fileName, Bitmap bm,
                                    Bitmap.CompressFormat format, int quality) {
        File imageFile = new File(dir, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            bm.compress(format, quality, fos);
            fos.close();

            return true;
        } catch (IOException e) {
            Log.e("app", e.getMessage());
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        return false;
    }
}

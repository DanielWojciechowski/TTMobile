package com.github.danielwojciechowski.ttmobile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

public class UploadToServer extends AsyncTask<Void, Long, Boolean> {

    private DropboxAPI<?> mApi;
    private DropboxAPI.UploadRequest mRequest;
    private ProgressListener progressListener;
    private Context mContext;
    private final ProgressDialog mDialog;
    private final String picturePath = TTMainActivity.getPicturePath();
    private final String fileName = getLastPartOfUri(picturePath);
    private final File tempFile = new File(picturePath);
    private long mFileLen = tempFile.length();

    public UploadToServer(Context context, DropboxAPI<?> api) {
        mContext = context.getApplicationContext();
        progressListener = new ProgressListener() {
            @Override
            public void onProgress(long l, long l1) {
                int progress = (int)((l / (float) mFileLen) * 100);
                publishProgress((long) progress);
            }
        };
        mApi = api;

        mDialog = new ProgressDialog(context);
        mDialog.setMax(100);
        mDialog.setMessage("Wysyłanie zdjęcia " + fileName);
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setProgress(0);
        mDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Anuluj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRequest.abort();
            }
        });
        mDialog.show();
    }

    protected Boolean doInBackground(Void... params) {


        try(FileInputStream fileInputStream = new FileInputStream(tempFile)) {
            mRequest = mApi.putFileRequest(prepareTravelDirName() + fileName, fileInputStream, mFileLen, null, progressListener);

            if (mRequest != null) {
                mRequest.upload();
                publishProgress(100L);
                return true;
            }

        } catch (DropboxException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String prepareTravelDirName() {
        if(TextUtils.isEmpty(TTMainActivity.getTravelDirName())) {
            SecureRandom random = new SecureRandom();
            TTMainActivity.setTravelDirName(new BigInteger(130, random).toString(32) + "/");
        }
        return TTMainActivity.getTravelDirName();
    }

    private String getLastPartOfUri(String uri) {
        String[] tab = uri.split("/");
        return tab[tab.length-1];
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        mDialog.setProgress(progress[0].intValue());
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mDialog.dismiss();
        if (result) {
            showToast("Zdjęcie przesłane pomyślnie");
        } else {
            showToast("Błąd w trakcie przesyłania zdjęcia");
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }
}

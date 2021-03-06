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

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

public class DropBoxConnector extends AsyncTask<Void, Long, Boolean> {

    private DropboxAPI.UploadRequest mRequest;
    private ProgressListener progressListener;
    private Context mContext;
    private ProgressDialog mDialog;
    private String fileName;
    private File tempFile;
    private long mFileLen;
    private int req;

    public DropBoxConnector(Context context) {
        req = 1;

        String picturePath = TTMainActivity.getPicturePath();
        fileName = getLastPartOfUri(picturePath);
        tempFile = new File(picturePath);
        mFileLen = tempFile.length();

        mContext = context.getApplicationContext();
        progressListener = new ProgressListener() {
            @Override
            public void onProgress(long l, long l1) {
                int progress = (int)((l / (float) mFileLen) * 100);
                publishProgress((long) progress);
            }
        };

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

    public DropBoxConnector() {
        req = 2;
    }

    protected Boolean doInBackground(Void... params) {
        try {
            switch (req){
                case 1 : {
                    return uploadFile();
                }
                case 2 : {
                    TTMainActivity.setUserUID(Long.toString(TTMainActivity.getmApi().accountInfo().uid));
                    break;
                }
            }
        } catch (DropboxException e) {
            e.printStackTrace();
        }


        return null;
    }

    private Boolean uploadFile() {
        try (FileInputStream fileInputStream = new FileInputStream(tempFile)) {
            mRequest = TTMainActivity.getmApi().putFileRequest(prepareTravelDirName() + fileName, fileInputStream, mFileLen, null, progressListener);
            if (mRequest != null) {
                DropboxAPI.Entry entry = mRequest.upload();
                publishProgress(100L);
                TTMainActivity.getImages().get(TTMainActivity.getImages().size()-1).put("path", entry.path);
                return true;
            }
        } catch (DropboxException | IOException | JSONException e) {
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

        switch (req){
            case 1 : {
                mDialog.dismiss();
                if (result) {
                    showToast("Zdjęcie przesłane pomyślnie");
                } else {
                    showToast("Błąd w trakcie przesyłania zdjęcia");
                }
                break;
            }
            case 2 : {
                TTMainActivity.getImages().clear();
                break;
            }
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }
}

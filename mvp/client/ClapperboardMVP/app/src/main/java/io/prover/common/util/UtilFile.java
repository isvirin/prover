package io.prover.common.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.prover.clapperboardmvp.R;

/**
 * Created by babay on 07.11.2017.
 */

public class UtilFile {

    final File file;

    public UtilFile(File file) {
        this.file = file;
    }

    public static File addFileNameSuffix(File file, String suffix) {
        String name = file.getName();
        int ptIndex = name.lastIndexOf('.');
        if (ptIndex >= 0) {
            String ext = name.substring(ptIndex);
            name = name.substring(0, ptIndex);
            name = name + suffix + ext;
        } else {
            name = name + suffix;
        }
        return new File(file.getParent(), name);
    }

    public static String readFully(File file) {
        String result = null;
        try {
            InputStream stream = new FileInputStream(file);
            result = readFully(stream);
            stream.close();
            return result;
        } catch (IOException e) {
            Log.e("UtilFile", e.getMessage(), e);
        }
        return result;
    }

    public static String readFully(InputStream inputStream) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            return new String(baos.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    public void externalOpenFile(Context context, String mime) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);

        Uri uri;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri = Uri.fromFile(file);
        } else {
            uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
        }

        intent.setData(uri);
        if (mime == null) {
            ContentResolver cR = context.getContentResolver();
            mime = cR.getType(uri);
            if (mime == null || "application/octet-stream".equals(mime)) {
                String ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
                if (mime == null || "application/octet-stream".equals(mime)) {
                    mime = "*/*";
                }
            }
        }
        intent.setDataAndType(uri, mime);

        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> resolved = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolveInfo : resolved) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        if (resolved.size() == 0) {
            Toast.makeText(context, R.string.cantFindApp, Toast.LENGTH_SHORT).show();
        } else {
            context.startActivity(intent);
        }
    }

    public void sendShareIntent(Context context, String subject, String text) {
        Intent intentShareFile = new Intent(Intent.ACTION_SEND);

        if (file.exists()) {
            intentShareFile.setType("application/pdf");
            Uri uri = Uri.fromFile(file);
            intentShareFile.putExtra(Intent.EXTRA_STREAM, uri);

            intentShareFile.putExtra(Intent.EXTRA_SUBJECT, subject);
            intentShareFile.putExtra(Intent.EXTRA_TEXT, text);

            context.startActivity(Intent.createChooser(intentShareFile, "Share File"));
        }
    }
}

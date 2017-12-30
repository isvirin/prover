package io.prover.provermvp.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import io.prover.provermvp.R;

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
}

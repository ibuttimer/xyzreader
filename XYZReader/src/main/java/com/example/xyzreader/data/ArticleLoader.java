package com.example.xyzreader.data;

import android.content.Context;
import android.content.CursorLoader;
import android.net.Uri;

/**
 * Helper for loading a list of articles or a single article.
 */
public class ArticleLoader extends CursorLoader {
    public static ArticleLoader newAllArticlesInstance(Context context) {
        return new ArticleLoader(context, ItemsContract.Items.buildDirUri(), Query.FULL_PROJECTION);
    }

    public static ArticleLoader newAllArticleIdsInstance(Context context) {
        return new ArticleLoader(context, ItemsContract.Items.buildDirUri(), Query.ID_PROJECTION);
    }

    public static ArticleLoader newInstanceForItemId(Context context, long itemId) {
        return new ArticleLoader(context, ItemsContract.Items.buildItemUri(itemId), Query.FULL_PROJECTION);
    }

    private ArticleLoader(Context context, Uri uri, String[] projection) {
        super(context, uri, projection, null, null, ItemsContract.Items.DEFAULT_SORT);
    }

    public interface Query {
        String[] FULL_PROJECTION = {
                ItemsContract.Items._ID,
                ItemsContract.Items.TITLE,
                ItemsContract.Items.PUBLISHED_DATE,
                ItemsContract.Items.AUTHOR,
                ItemsContract.Items.THUMB_URL,
                ItemsContract.Items.PHOTO_URL,
                ItemsContract.Items.ASPECT_RATIO,
                ItemsContract.Items.BODY,
        };

        String[] ID_PROJECTION = {
                ItemsContract.Items._ID
        };

        int _ID = 0;
        int TITLE = 1;
        int PUBLISHED_DATE = 2;
        int AUTHOR = 3;
        int THUMB_URL = 4;
        int PHOTO_URL = 5;
        int ASPECT_RATIO = 6;
        int BODY = 7;
    }
}

package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.example.xyzreader.ui.ArticleListActivity.BACK_FROM_DETAIL_ARG;


/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, ArticleDetailFragment.IDetailActivity {

    private Cursor mCursor;
    private long mStartId;
    private int mSelectedPos;

    @BindColor(R.color.tab_seperator) int mTabSepColour;

    @BindView(R.id.pager) ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);

        ButterKnife.bind(this);

        mSelectedPos = -1;

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());

        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(mTabSepColour));

        mPager.setPageTransformer(true, new ZoomOutPageTransformer());

        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    if (mCursor.moveToPosition(position)) {
                        mSelectedPos = position;
                    }
                }
            }
        });

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null) {
                Uri uri = intent.getData();
                if (uri != null) {
                    mStartId = ItemsContract.Items.getItemId(uri);
                }
            }
        }

        getLoaderManager().initLoader(0, null, this);
    }


    @android.support.annotation.Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        Intent intent = super.getSupportParentActivityIntent();
        intent.putExtra(BACK_FROM_DETAIL_ARG, true);
        return intent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticleIdsInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            for (int i = 0, limit = mCursor.getCount(); i < limit; i++) {
                if (mCursor.moveToPosition(i)) {
                    if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                        mPager.setCurrentItem(i, false);
                        mSelectedPos = i;
                        break;
                    }
                }
            }
            mStartId = 0;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID), position);
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }


        @Override
        public void finishUpdate(ViewGroup container) {
            super.finishUpdate(container);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            Timber.i("setPrimaryItem " + position + " current " + getCurrentItem());

            if (object != null) {
                try {
                    ArticleDetailFragment fragment = (ArticleDetailFragment) object;
                    fragment.setToolBar();
                } catch (ClassCastException e) {
                    Timber.w("Primary item not ArticleDetailFragment");
                }
            }
        }
    }

    @Override
    public int getCurrentItem() {
        int current = -1;
        if (mPager != null) {
            current = mPager.getCurrentItem();
        }
        return current;
    }
}

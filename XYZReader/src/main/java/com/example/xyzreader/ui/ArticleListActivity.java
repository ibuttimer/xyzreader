package com.example.xyzreader.ui;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;
import com.example.xyzreader.util.Utils;
import com.fondesa.recyclerviewdivider.RecyclerViewDivider;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.MessageFormat;
import java.util.Date;

import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {


    public static final String BACK_FROM_DETAIL_ARG = "back_from_detail";

    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.item_list) RecyclerView mRecyclerView;
    @BindView(R.id.main_toolbar) Toolbar mToolBar;

    @BindColor(R.color.appColorPrimaryLight) int mDividerColour;
    @BindDimen(R.dimen.fab_margin) int mDividerInset;

    private Adapter mAdapter;
    private boolean mIsRefreshing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        ButterKnife.bind(this);

        mAdapter = new Adapter(null);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);

        setSupportActionBar(mToolBar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setLogo(R.drawable.logo);
        }

        getLoaderManager().initLoader(0, null, this);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!mIsRefreshing) {
                    refresh();
                }
            }
        });

        if (savedInstanceState == null) {
            Bundle bundle = getIntent().getExtras();
            if ((bundle != null) && !bundle.containsKey(BACK_FROM_DETAIL_ARG)) {
                refresh();
            }
            // else back from detail activity, so no need to refresh as data not changing & singleTop
        }
    }

    private void refresh() {
        clearData();
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void onMessageEvent(UpdaterService.UpdateServiceEvent event) {
        mIsRefreshing = event.isIsRefreshing();

        Timber.i("Refreshing " + mIsRefreshing);
        updateRefreshingUI();
    }

    private void updateRefreshingUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            refresh();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        mAdapter.setCursor(cursor);

        int columnCount = getResources().getInteger(R.integer.list_column_count);
        GridLayoutManager sglm = new GridLayoutManager(this, columnCount);
        mRecyclerView.setLayoutManager(sglm);

        RecyclerViewDivider.with(this)
                .color(mDividerColour)
                .inset(mDividerInset, mDividerInset)
                .hideLastDivider()
                .build()
                .addTo(mRecyclerView);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        clearData();
        mIsRefreshing = false;
        updateRefreshingUI();
    }


    private void clearData() {
        mAdapter.setCursor(null);
    }

    /**
     * Adapter class for article list
     */
    private class Adapter extends RecyclerView.Adapter<ViewHolder> {

        private Cursor mCursor;

        Adapter(Cursor cursor) {
            mCursor = cursor;
        }

        public void setCursor(Cursor cursor) {
            this.mCursor = cursor;
            notifyDataSetChanged();
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(ArticleLoader.Query._ID);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final ViewHolder vh = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(ArticleListActivity.this, ArticleDetailActivity.class);
                    intent.setData(ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition())));

                    startActivity(intent);
                }
            });
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));

            Date publishedDate = Utils.parseDate(mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE));
            String dateStr = Utils.getDateString(publishedDate);
            String subTitleHtml = MessageFormat.format(
                    getResources().getString(R.string.article_list_subtitle),
                    dateStr,
                    mCursor.getString(ArticleLoader.Query.AUTHOR));

            holder.subtitleView.setText(Html.fromHtml(subTitleHtml));

            holder.thumbnailView.setImageUrl(
                    mCursor.getString(ArticleLoader.Query.THUMB_URL),
                    ImageLoaderHelper.getInstance(ArticleListActivity.this).getImageLoader());
            holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
        }

        @Override
        public int getItemCount() {
            int count = 0;
            if (mCursor != null) {
                count = mCursor.getCount();
            }
            return count;
        }
    }

    /**
     * Article item view holder class
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.thumbnail) DynamicHeightNetworkImageView thumbnailView;
        @BindView(R.id.article_title) TextView titleView;
        @BindView(R.id.article_subtitle) TextView subtitleView;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }


}

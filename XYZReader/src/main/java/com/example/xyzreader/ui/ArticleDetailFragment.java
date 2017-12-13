package com.example.xyzreader.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;

import java.text.MessageFormat;
import java.util.Date;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.util.Dialog;
import com.example.xyzreader.util.Utils;

import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.example.xyzreader.util.Utils.OPAQUE;
import static com.example.xyzreader.util.Utils.TRANSPARENT;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    @BindColor(R.color.author_text_colour) int mAuthorTextColour;
    @BindColor(R.color.detail_metabar_container_background) int mMetaBarContainerBgColour;
    @BindColor(R.color.detail_metabar_background_default) int mDfltMetaBarBgColour;
    @BindColor(R.color.detail_metabar_text_default) int mDfltMetaBarTextColour;
    @BindColor(R.color.detail_statusbar_default) int mDfltStatusBarColour;
    @BindColor(android.R.color.transparent) int mTransparentColour;
    @BindInt(R.integer.max_palette_colour_count) int mMaxColourCount;
    @BindInt(android.R.integer.config_mediumAnimTime) int mAnimationDuration;
    @BindDimen(R.dimen.init_scroll_up_distance) int mScrollUpDistance;
    @BindInt(R.integer.tablet_land_breakpoint) int mTabletLandBreak;

    public static final String ARG_ITEM_ID = "item_id";

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mPosition;
    private boolean mIsToolBar;     // flag representing if this object's toolbar is used from the activity's action bar

    @BindView(R.id.detail_layout) View mLayoutView;
    @BindView(R.id.detail_toolbar) Toolbar mToolBar;
    @BindView(R.id.scrollview) NestedScrollView mScrollView;
    @BindView(R.id.meta_bar_container) View mMetaBarContainerView;

//    @BindView(R.id.action_up) View mUpButton;

    @BindView(R.id.photo) DynamicHeightImageView mPhotoView;
    @BindView(R.id.meta_bar) View mMetaBarView;
    @BindView(R.id.article_title) TextView mTitleView;
    @BindView(R.id.article_byline) TextView mBylineView;
    @BindView(R.id.article_body) TextView mBodyView;
    @BindView(R.id.detail_appbar) AppBarLayout mAppBar;
    @Nullable @BindView(R.id.detail_collapsing) CollapsingToolbarLayout mCollapsingBar;

    private IDetailActivity mActivity;

    private enum ScreenMode { PORTAIT, LANDSCAPE, TABLET_LANDSCAPE };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId, int position) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.mPosition = position;
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }
        mIsToolBar = false;

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        ButterKnife.bind(this, mRootView);

        bindViews();

        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        attach(context);
    }

    @Override
    public void onAttach(Activity activity) {
        /* this method is deprecated but if using native fragments with min sdk < 23
            still need to implement it as onAttach(Context) is not called
          */
        super.onAttach(activity);
        attach(activity);
    }


    private void attach(Object container) {
        try {
            mActivity = (IDetailActivity) container;
            setToolBar();
        } catch (ClassCastException e) {
            Timber.w("Activity must implement interface IDetailActivity");
        }
    }


    @OnClick(R.id.share_fab)
    public void share(View view) {
        if (mCursor != null) {
            String title = mCursor.getString(ArticleLoader.Query.TITLE);
            String author = mCursor.getString(ArticleLoader.Query.AUTHOR);
            String shareText = MessageFormat.format(
                    getResources().getString(R.string.article_share_text),
                    title,
                    author);
            startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                    .setType("text/plain")
                    .setText(shareText)
                    .getIntent(), getString(R.string.action_share)));
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        setToolBar();

        mBylineView.setMovementMethod(new LinkMovementMethod());

        mBodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), getString(R.string.article_font_path)));

        if (mCursor != null) {
            String title = mCursor.getString(ArticleLoader.Query.TITLE);
            String author = mCursor.getString(ArticleLoader.Query.AUTHOR);
            String published = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            String body = mCursor.getString(ArticleLoader.Query.BODY);
            String photoUrl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
            final Float aspect = mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO);

            /* portrait & tablet landscape - 1 stage animation; fade in screen
                normal landscape - 2 stage animation; fade in screen followed by body test
             */
            boolean twoStageFadeIn = (getScreenMode() == ScreenMode.LANDSCAPE);

            AnimatorListenerAdapter listenerAdapter;
            mScrollView.setAlpha(twoStageFadeIn ? TRANSPARENT : OPAQUE);
            if (twoStageFadeIn) {
                listenerAdapter = new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        animateAlpha(mScrollView, new YAnimatorListenerAdapter(mScrollView));
                    }
                };
            } else {
                listenerAdapter = new YAnimatorListenerAdapter(mScrollView);
            }
            animateAlpha(mRootView, listenerAdapter);

            if (mCollapsingBar != null) {
                mCollapsingBar.setTitle(title);

                mAppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                    @Override
                    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                        boolean enabled;
                        // enable title if collapsed
                        if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                            // collapsed
                            enabled = true;
                        } else {
                            // verticalOffset == 0 => expanded
                            // else somewhere in between
                            enabled = false;
                        }
                        mCollapsingBar.setTitleEnabled(enabled);
                    }
                });
            }

            mTitleView.setText(title);

            Date publishedDate = Utils.parseDate(published);
            String dateStr = Utils.getDateString(publishedDate);
            String subTitleHtml = MessageFormat.format(
                    getResources().getString(R.string.article_detail_subtitle),
                    dateStr,
                    author);

            mBylineView.setText(Html.fromHtml(subTitleHtml));

            mBodyView.setText(Html.fromHtml(body));

            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(photoUrl,
                            new ImageLoader.ImageListener() {
                                @Override
                                public void onResponse(ImageLoader.ImageContainer imageContainer, boolean isImmediate) {
                                    Bitmap bitmap = imageContainer.getBitmap();
                                    if (bitmap != null) {
                                        mPhotoView.setAspectRatio(aspect);
                                        mPhotoView.setImageBitmap(bitmap);

                                        Palette p = new Palette.Builder(bitmap)
                                                .maximumColorCount(mMaxColourCount)
                                                .generate();

                                        @ColorInt int mutedColor = p.getDarkMutedColor(mDfltMetaBarBgColour);
                                        @ColorInt int vibrantColor = p.getLightVibrantColor(mDfltMetaBarTextColour);
                                        @ColorInt int furthestColor = Utils.getFurthestColour(mMetaBarContainerBgColour, p);

                                        // set metabar background colour
//                                        if (getScreenMode() == ScreenMode.TABLET_LANDSCAPE) {
//                                            mToolBar.setBackgroundColor(mTransparentColour);
//                                        } else {
//                                            mToolBar.setBackgroundColor(mutedColor);
//                                        }
                                        mMetaBarView.setBackgroundColor(mutedColor);
                                        mLayoutView.setBackgroundColor(mutedColor);
                                        // set metabar text colour
                                        for (TextView textView : new TextView[]{mTitleView, mBylineView}) {
                                            textView.setTextColor(vibrantColor);
                                        }
                                        // set metabar body text colour
                                        mBodyView.setTextColor(furthestColor);
                                    }
                                }

                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    Dialog.showAlertDialog(getActivity(), volleyError.getLocalizedMessage());
                                }
                            });
        } else {
            mRootView.setVisibility(View.INVISIBLE);
            for (TextView textView : new TextView[] { mTitleView, mBylineView, mBodyView }) {
                textView.setText(R.string.not_available);
            }
        }
    }

    private ScreenMode getScreenMode() {
        Activity activity = getActivity();
        ScreenMode mode = ScreenMode.PORTAIT;
        if (!Utils.isPotraitScreen(activity)) {
            if (Utils.getScreenDpWidth(activity) < mTabletLandBreak) {
                mode = ScreenMode.LANDSCAPE;
            } else {
                mode = ScreenMode.TABLET_LANDSCAPE;
            }
        }
        return mode;
    }

    public void animateY(View view) {
        ObjectAnimator objAnimator = ObjectAnimator.ofInt(mScrollView, "scrollY", mScrollUpDistance);
        objAnimator.setRepeatCount(1);
        objAnimator.setRepeatMode(ValueAnimator.REVERSE);
        objAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                animator.setTarget(null);
            }
        });
        objAnimator.setDuration(mAnimationDuration)
                .start();
    }


    public void animateAlpha(View view, AnimatorListenerAdapter listenerAdapter) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(TRANSPARENT);
        ViewPropertyAnimator propertyAnimator = view.animate();
        propertyAnimator.setListener(listenerAdapter)
                .setDuration(mAnimationDuration)
                .alpha(OPAQUE);
    }

    public void setToolBar() {
        /* setPrimaryItem() in the adapter is called multiple times, and if this is called from there
            and sets the action bar it results in endless calls, hence the mIsToolBar flag
         */
        boolean isToolBar = false;
        if (mActivity != null) {
            int current = mActivity.getCurrentItem();
            if (mPosition == current) {
                try {
                    AppCompatActivity activity = (AppCompatActivity) getActivity();
                    if (!mIsToolBar) {
                        activity.setSupportActionBar(mToolBar);
                        ActionBar actionBar = activity.getSupportActionBar();
                        if (actionBar != null) {
                            actionBar.setDisplayHomeAsUpEnabled(true);
                            actionBar.setDisplayShowTitleEnabled(false);
                            isToolBar = true;
                        }
                    }
                } catch (ClassCastException e) {
                    Timber.w("Activity not AppCompatActivity");
                }
            }
        }
        mIsToolBar = isToolBar;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Timber.e("Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }



    interface IDetailActivity {
        int getCurrentItem();
    }


    class YAnimatorListenerAdapter extends AnimatorListenerAdapter {

        View view;

        public YAnimatorListenerAdapter(View view) {
            this.view = view;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            animateY(view);
        }

    }

}

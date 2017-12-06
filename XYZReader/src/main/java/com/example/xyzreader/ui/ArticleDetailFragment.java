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

import org.jetbrains.annotations.Nullable;

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
    @BindInt(R.integer.max_palette_colour_count) int mMaxColourCount;
    @BindInt(android.R.integer.config_mediumAnimTime) int mAnimationDuration;
    @BindDimen(R.dimen.init_scroll_up_distance) int mScrollUpDistance;
    @BindInt(R.integer.tablet_land_breakpoint) int mTabletLandBreak;

    public static final String ARG_ITEM_ID = "item_id";

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    @ColorInt private int mMutedColor;
    @ColorInt private int mVibrantColor;

    @BindView(R.id.detail_layout) View mLayoutView;
    @BindView(R.id.scrollview) NestedScrollView mScrollView;
    @BindView(R.id.meta_bar_container) MaxWidthLinearLayout mMetaBarContainerView;

    @BindView(R.id.action_up) View mUpButton;

    @BindView(R.id.photo) DynamicHeightImageView mPhotoView;
    @BindView(R.id.meta_bar) View mMetaBarView;
    @BindView(R.id.article_title) TextView mTitleView;
    @BindView(R.id.article_byline) TextView mBylineView;
    @BindView(R.id.article_body) TextView mBodyView;
    @BindView(R.id.detail_appbar) AppBarLayout mAppBar;
    @Nullable @BindView(R.id.detail_collapsing) CollapsingToolbarLayout mCollapsingBar;

    private IDetailActivity mActivity;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

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

    @OnClick(R.id.action_up)
    public void onUpClick(View view) {
        mActivity.upClick();
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        mBylineView.setMovementMethod(new LinkMovementMethod());

        mBodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), getString(R.string.article_font_path)));

        if (mCursor != null) {
            String title = mCursor.getString(ArticleLoader.Query.TITLE);
            String author = mCursor.getString(ArticleLoader.Query.AUTHOR);
            String published = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            String body = mCursor.getString(ArticleLoader.Query.BODY);
            String photoUrl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
            final Float aspect = mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO);

            Activity activity = getActivity();
            boolean twoStageFadeIn = false;  // 1 stage animation; fade in screen
            if (!Utils.isPotraitScreen(activity)) {
                if (Utils.getScreenDpWidth(activity) < mTabletLandBreak) {
                    // normal landscape, so use 2 stage animation; fade in screen followed by body test
                    twoStageFadeIn = true;
                }
            }

            final AnimatorListenerAdapter listenerAdapterY = new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    animateY();
                }
            };
            AnimatorListenerAdapter listenerAdapter;
            mScrollView.setAlpha(twoStageFadeIn ? TRANSPARENT : OPAQUE);
            if (twoStageFadeIn) {
                listenerAdapter = new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        animateAlpha(mScrollView, listenerAdapterY);
                    }
                };
            } else {
                listenerAdapter = listenerAdapterY;
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
                                        // set metabar background colour
                                        mMutedColor = p.getDarkMutedColor(mDfltMetaBarBgColour);
                                        mMetaBarView.setBackgroundColor(mMutedColor);
                                        mLayoutView.setBackgroundColor(mMutedColor);
                                        // set metabar text colour
                                        mVibrantColor = p.getLightVibrantColor(mDfltMetaBarTextColour);
                                        for (TextView textView : new TextView[]{mTitleView, mBylineView}) {
                                            textView.setTextColor(mVibrantColor);
                                        }

                                        // set metabar body text colour
                                        mBodyView.setTextColor(Utils.getFurthestColour(mMetaBarContainerBgColour, p));
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

    public void animateY() {
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


    interface IDetailActivity {
        void upClick();
    }




}

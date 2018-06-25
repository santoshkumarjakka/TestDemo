package com.demo.test;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.demo.test.models.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Suleiman on 19/10/16.
 */

public class PaginationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final int ITEM = 0;
  private static final int LOADING = 1;
  private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w150";

  private List<Page> movieResults;
  private Context context;

  private boolean isLoadingAdded = false;

  public PaginationAdapter(Context context,List<Page> movieResults) {
    this.context = context;
    this.movieResults=movieResults;
  }

  public List<Page> getMovies() {
    return movieResults;
  }

  public void setMovies(List<Page> movieResults) {
    this.movieResults = movieResults;
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    RecyclerView.ViewHolder viewHolder = null;
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());

    switch (viewType) {
      case ITEM:
        viewHolder = getViewHolder(parent, inflater);
        break;
      case LOADING:
        View v2 = inflater.inflate(R.layout.item_progress, parent, false);
        viewHolder = new LoadingVH(v2);
        break;
    }
    return viewHolder;
  }

  @NonNull
  private RecyclerView.ViewHolder getViewHolder(ViewGroup parent, LayoutInflater inflater) {
    RecyclerView.ViewHolder viewHolder;
    View v1 = inflater.inflate(R.layout.item_list, parent, false);
    viewHolder = new MovieVH(v1);
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    Page result = movieResults.get(position); // Movie

    switch (getItemViewType(position)) {
      case ITEM:
        final MovieVH movieVH = (MovieVH) holder;

        movieVH.mMovieTitle.setText(result.getTitle());
        if (result.getTerms() != null) {
          for (String des : result.getTerms().getDescription()) {
            movieVH.mMovieDesc.setText(des);
          }
        }
//

        /**
         * Using Glide to handle image loading.
         * Learn more about Glide here:
         * <a href="http://blog.grafixartist.com/image-gallery-app-android-studio-1-4-glide/" />
         */
        String values;
        if (result.getThumbnail() == null) {
          values = "text";
        } else {
          values = result.getThumbnail().getSource();
        }
        Glide
          .with(context)
          .load(values)
          .listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
              // TODO: 08/11/16 handle failure
              movieVH.mProgress.setVisibility(View.GONE);
              return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
              // image ready, hide progress now
              movieVH.mProgress.setVisibility(View.GONE);
              return false;   // return false if you want Glide to handle everything else.
            }
          })
////                        .diskCacheStrategy(DiskCacheStrategy.ALL)   // cache both original & resized image
          .fitCenter()
          .crossFade()
          .into(movieVH.mPosterImg);

        break;

      case LOADING:
//                Do nothing
        break;
    }

  }

  @Override
  public int getItemCount() {
    return movieResults == null ? 0 : movieResults.size();
  }

  @Override
  public int getItemViewType(int position) {
    return (position == movieResults.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
  }


    /*
   Helpers
   _________________________________________________________________________________________________
    */

  public void add(Page r) {
    movieResults.add(r);
    notifyItemInserted(movieResults.size() - 1);
  }

  public void addAll(List<Page> moveResults) {
    for (Page result : moveResults) {
      add(result);
    }
  }

  public void remove(Page r) {
    int position = movieResults.indexOf(r);
    if (position > -1) {
      movieResults.remove(position);
      notifyItemRemoved(position);
    }
  }

  public void clear() {
    isLoadingAdded = false;
    while (getItemCount() > 0) {
      remove(getItem(0));
    }
  }

  public boolean isEmpty() {
    return getItemCount() == 0;
  }


  public void addLoadingFooter() {
    isLoadingAdded = true;
    add(new Page());
  }

  public void removeLoadingFooter() {
    isLoadingAdded = false;

    int position = movieResults.size() - 1;
    Page result = getItem(position);

    if (result != null) {
      movieResults.remove(position);
      notifyItemRemoved(position);
    }
  }

  public Page getItem(int position) {
    return movieResults.get(position);
  }


   /*
   View Holders
   _________________________________________________________________________________________________
    */

  /**
   * Main list's content ViewHolder
   */
  protected class MovieVH extends RecyclerView.ViewHolder {
    private TextView mMovieTitle;
    private TextView mMovieDesc;
    private ImageView mPosterImg;
    private ProgressBar mProgress;

    public MovieVH(View itemView) {
      super(itemView);

      mMovieTitle = (TextView) itemView.findViewById(R.id.movie_title);
      mMovieDesc = (TextView) itemView.findViewById(R.id.movie_desc);
      mPosterImg = (ImageView) itemView.findViewById(R.id.movie_poster);
      mProgress = (ProgressBar) itemView.findViewById(R.id.movie_progress);
    }
  }


  protected class LoadingVH extends RecyclerView.ViewHolder {

    public LoadingVH(View itemView) {
      super(itemView);
    }
  }


}

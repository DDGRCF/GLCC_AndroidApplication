package com.glcc.client;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ClickUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.glcc.client.manager.VideoRecorderModel;

import java.util.List;


public class VideoRecyclerViewAdapter extends RecyclerView.Adapter<VideoRecyclerViewAdapter.ItemViewHolder> {

    public static final int TYPE_GROUP = 1;
    public static final int TYPE_CHILD = 2;
    private static final String TAG = VideoRecyclerViewAdapter.class.getName();

    private List<ViewItem> mItems;
    private OnItemClickListener mClickListener;
    private ClickUtils.OnDebouncingClickListener mImageViewClickListener;

    public VideoRecyclerViewAdapter(List<ViewItem> mItems) {
        this.mItems = mItems;
    }

    public List<ViewItem> getAllDataItem() {
        return mItems;
    }

    public void addDataItem(int position, ViewItem item) {
        mItems.add(position, item);
        notifyItemInserted(position);
    }

    public void removeDataItem(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    public ViewItem getSingleItem(int position) {
        return mItems.get(position);
    }

    public void setDataItem(List<ViewItem> mItems) {
        this.mItems = mItems;
//        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(VideoRecyclerViewAdapter.OnItemClickListener mChickListener) {
        this.mClickListener = mChickListener;
    }

    public void setOnImageViewClickListener(ClickUtils.OnDebouncingClickListener mClickListener) {
        this.mImageViewClickListener = mClickListener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
        View view;
        ItemViewHolder recyclerItemViewHolder = null;
        switch (viewType) {
            case TYPE_GROUP: {
                view = LayoutInflater.from(
                        parent.getContext()).
                        inflate(R.layout.item_title_video_recycler_view_adapter,
                                parent, false);
                recyclerItemViewHolder = new GroupViewHolder(view);
                break;
            }
            case TYPE_CHILD: {
                view = LayoutInflater.from(
                                parent.getContext()).
                        inflate(R.layout.item_video_recycler_view_adapter,
                                parent, false);
                recyclerItemViewHolder = new ChildViewHolder(view);
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + viewType);
        }
        return recyclerItemViewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ViewItem item = mItems.get(position);
        switch (getItemViewType(position)) {
            case TYPE_GROUP: {
                Log.d(TAG, "Parent: " + item.toString());
                ViewGroup group = (ViewGroup) item;
                GroupViewHolder groupVH = (GroupViewHolder) holder;
                groupVH.getVideoTitle().setText(group.getTitle());
                groupVH.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mClickListener != null) {
                            Log.d(TAG, "Click: " + position);
                            mClickListener.onItemClick(position);
                        }
                    }
                });
                break;
            }
            case TYPE_CHILD: {
                Log.d(TAG, "Child: " + item.toString());
                ViewChild child = (ViewChild) item;
                ChildViewHolder childVH = (ChildViewHolder) holder;
                VideoRecorderModel videoRecorderModel = child.getVideoRecorderModel();
                if (ObjectUtils.isEmpty(videoRecorderModel)) {
                    break;
                }

                if (!ObjectUtils.isEmpty(child.getVideoRecorderModel().getBitMap())) {
                    childVH.getVideoImage().setImageBitmap(child.getVideoRecorderModel().getBitMap());
                } else {
                    childVH.getVideoImage().setImageResource(R.drawable.image_break);
                }

                if (!ObjectUtils.isEmpty(child.getGroupName())) {
                    childVH.getVideoTitle().setText(child.getGroupName());
                } else {
                    childVH.getVideoTitle().setText("Error");
                }

                if (!ObjectUtils.isEmpty(child.getVideoRecorderModel().getVideoUrl())) {
                    childVH.getVideoUrl().setText(child.getVideoRecorderModel().getVideoUrl());
                } else {
                    childVH.getVideoUrl().setText("Error");
                }


                if (!ObjectUtils.isEmpty(child.getVideoRecorderModel().getStartTime())) {
                    childVH.getVideoStartTime().setText(child.getVideoRecorderModel().getStartTime());
                } else {
                    childVH.getVideoStartTime().setText("Null");
                }

                if (!ObjectUtils.isEmpty(child.getVideoRecorderModel().getEndTime())) {
                    childVH.getVideoEndTime().setText(child.getVideoRecorderModel().getEndTime());
                } else {
                    childVH.getVideoEndTime().setText("Null");
                }

                if (!ObjectUtils.isEmpty(child.getVideoRecorderModel().getBitMap())) {
                    childVH.getVideoImage().setImageBitmap(child.getVideoRecorderModel().getBitMap());
                    if (!ObjectUtils.isEmpty(child.getVideoRecorderModel().getVideoUrl())) {
                        String description = child.getGroupName() + "," + child.getVideoRecorderModel().getVideoUrl();
                        childVH.getVideoImage().setContentDescription(description);
                    }
                } else {
                    childVH.getVideoImage().setImageResource(R.drawable.image_break);
                    childVH.getVideoImage().setContentDescription("Null");
                }

                if (!ObjectUtils.isEmpty(child.getVideoRecorderModel().getMessage())) {
                    childVH.getMessage().setText(child.getVideoRecorderModel().getMessage());
                } else {
                    childVH.getMessage().setText("Null");
                }

                childVH.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        Log.d(TAG, v.getId() + ": 获得聚焦");
                        if (hasFocus) {
                            childVH.getVideoStartTime().setSelected(true);
                            childVH.getVideoStartTime().setSelected(true);
                            childVH.getVideoEndTime().setSelected(true);
                        } else {
                            childVH.getVideoStartTime().setSelected(false);
                            childVH.getVideoStartTime().setSelected(false);
                            childVH.getVideoEndTime().setSelected(false);
                        }
                    }
                });
                childVH.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mClickListener != null) {
                            Log.d(TAG, "Click: " + position);
                            mClickListener.onItemClick(position);
                        }
                    }
                });

                if (!ObjectUtils.isEmpty(mImageViewClickListener)) {
                    childVH.getVideoImage().setOnClickListener(mImageViewClickListener);
                }
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + getItemViewType(position));
        }
    }

    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    abstract static class ViewItem {
        private int position;

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public abstract int getType();
    }

    static class ViewGroup extends ViewItem {
        public String title;
        public final int type = TYPE_GROUP;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public int getType() {
            return type;
        }

        @NonNull
        @Override
        public String toString() {
            return "ViewGroup{" +
                    "position=" + getPosition() +
                    ", title='" + title + '\'' +
                    '}';
        }
    }

    static class ViewChild extends ViewItem {
        private int groupPos;
        private String groupName;
        private VideoRecorderModel videoRecorderModel;
        public final int type = TYPE_CHILD;

        public int getGroupPos() {
            return groupPos;
        }

        public void setGroupPos(int groupPos) {
            this.groupPos = groupPos;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public VideoRecorderModel getVideoRecorderModel() {
            return videoRecorderModel;
        }

        public void setVideoRecorderModel(VideoRecorderModel videoRecorderModel) {
            this.videoRecorderModel = videoRecorderModel;
        }

        @Override
        public int getType() {
            return type;
        }

        @NonNull
        @Override
        public String toString() {
            return "ViewChild{" +
                    "position=" + getPosition() +
                    ", groupPos=" + groupPos +
                    ", groupName='" + groupName + '\'' +
                    ", videoRecorderModel=" + videoRecorderModel +
                    '}';
        }
    }

    abstract static class ItemViewHolder extends RecyclerView.ViewHolder {
        public ItemViewHolder(View itemView) {
            super(itemView);
        }
        public abstract int getType();
    }


    static class ChildViewHolder extends ItemViewHolder {
        private ImageView videoImage;
        private TextView videoTitle;
        private TextView videoUrl;
        private TextView videoStartTime;
        private TextView videoEndTime;
        private TextView message;
        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            this.videoImage = itemView.findViewById(R.id.recorder_video_recycler_image);
            this.videoTitle = itemView.findViewById(R.id.recorder_video_recycler_title);
            this.videoUrl = itemView.findViewById(R.id.recorder_video_recycler_video_url);
            this.videoStartTime = itemView.findViewById(R.id.recorder_video_recycler_start_time);
            this.videoEndTime = itemView.findViewById(R.id.recorder_video_recycler_end_time);
            this.message = itemView.findViewById(R.id.recorder_video_recycler_message);
            videoUrl.setSelected(true);
            videoTitle.setSelected(true);
            videoStartTime.setSelected(true);
            videoEndTime.setSelected(true);
            message.setSelected(true);
        }

        public ImageView getVideoImage() {
            return videoImage;
        }

        public void setVideoImage(ImageView videoImage) {
            this.videoImage = videoImage;
        }

        public TextView getVideoTitle() {
            return videoTitle;
        }

        public void setVideoTitle(TextView videoTitle) {
            this.videoTitle = videoTitle;
        }

        public TextView getVideoUrl() {
            return videoUrl;
        }

        public void setVideoUrl(TextView videoUrl) {
            this.videoUrl = videoUrl;
        }

        public TextView getVideoStartTime() {
            return videoStartTime;
        }

        public void setVideoStartTime(TextView videoStartTime) {
            this.videoStartTime = videoStartTime;
        }

        public TextView getVideoEndTime() {
            return videoEndTime;
        }

        public void setVideoEndTime(TextView videoEndTime) {
            this.videoEndTime = videoEndTime;
        }

        public TextView getMessage() {
            return message;
        }

        public void setMessage(TextView message) {
            this.message = message;
        }

        @Override
        public int getType() {
            return VideoRecyclerViewAdapter.TYPE_CHILD;
        }
    }

    static class GroupViewHolder extends ItemViewHolder {
        private TextView videoTitle;
        public GroupViewHolder(View itemView) {
            super(itemView);
            this.videoTitle = itemView.findViewById(R.id.recorder_video_recycler_title);
        }

        public TextView getVideoTitle() {
            return videoTitle;
        }

        public void setVideoTitle(TextView videoTitle) {
            this.videoTitle = videoTitle;
        }

        @Override
        public int getType() {
            return VideoRecyclerViewAdapter.TYPE_GROUP;
        }


    }
}

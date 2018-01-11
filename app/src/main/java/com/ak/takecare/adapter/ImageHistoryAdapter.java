package com.ak.takecare.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.ak.takecare.ImageEditActivity;
import com.ak.takecare.R;
import com.ak.takecare.model.ImageData;
import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * Created by dg hdghfd on 12-04-2017.
 */

public class ImageHistoryAdapter extends RecyclerView.Adapter<ImageHistoryAdapter.MyViewHolder> {

    private List<ImageData> imageList;
    private Context context;

    Realm realm;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        //@BindView(R.id.tvDate)
        //TextView tvDate;
        @BindView(R.id.ivImg)
        ImageView ivImg;
        @BindView(R.id.ivImgEdited)
        ImageView ivImgEdit;
        @BindView(R.id.ivShare)
        ImageView ivShare;
        @BindView(R.id.ivDelete)
        ImageView ivDelete;


        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, ImageEditActivity.class);
                    i.putExtra("imgid",imageList.get(getPosition()).getId());
                    i.putExtra("imgurl", imageList.get(getPosition()).getEditedPath());
                    context.startActivity(i);
                }
            });

            ivShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageList.get(getPosition()).getEditedPath());
                    shareIntent.setType("image/jpeg");
                    context.startActivity(Intent.createChooser(shareIntent, "send to"));
                }
            });

            ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(final Realm realm) {


                            AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                            builder1.setMessage("Delete record?");
                            builder1.setCancelable(true);

                            builder1.setPositiveButton(
                                    "Yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                            realm.executeTransaction(new Realm.Transaction() {
                                                @Override
                                                public void execute(Realm realm) {
                                                    ImageData imageData = imageList.get(getPosition());
                                                    imageData.deleteFromRealm();

                                                    imageList.remove(getPosition());
                                                    notifyItemRemoved(getPosition());

                                                }
                                            });

                                        }
                                    });

                            builder1.setNegativeButton(
                                    "No",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                            AlertDialog alert11 = builder1.create();
                            alert11.show();

                        }
                    });
                }
            });


        }
    }


    public ImageHistoryAdapter(Context context, List<ImageData> imageList) {
        this.imageList = imageList;
        this.context = context;
        realm = Realm.getDefaultInstance();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Glide.with(context).load(imageList.get(position).getPrevPath()).into(holder.ivImg);
        Glide.with(context).load(imageList.get(position).getEditedPath()).into(holder.ivImgEdit);


    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }


}
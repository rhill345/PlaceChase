package com.android.placechase.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.placechase.R;

/**
 * Created by randy on 10/22/2016.
 */
public class ContactViewHolder extends RecyclerView.ViewHolder {

    private ImageView image;
    private TextView  name;
    private View      container;

    public ContactViewHolder(View itemView) {
        super(itemView);
        container = itemView.findViewById(R.id.content_holder);
        image     = (ImageView) itemView.findViewById(R.id.contact_image);
        name      = (TextView) itemView.findViewById(R.id.contact_name);
    }

    public ImageView getImage() {
        return image;
    }

    public TextView getName() {
        return name;
    }

    public View getContainer() {
        return container;
    }
}

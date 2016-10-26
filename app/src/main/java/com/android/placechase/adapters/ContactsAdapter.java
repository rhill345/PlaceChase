package com.android.placechase.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.placechase.R;
import com.android.placechase.fragments.IContactFragmentChangeListener;
import com.android.placechase.model.ContactShareModel;
import com.android.placechase.utils.GeneralUtils;
import com.android.placechase.utils.thirdparty.ColorGenerator;
import com.android.placechase.utils.ShareManager;
import com.android.placechase.utils.thirdparty.TextDrawable;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by randy on 10/22/2016.
 */
public class ContactsAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /*The RecyclerView view handles handles 3 types of inncomming data.
    * Each data type's onclick event gets handled differently
    * */
    public final static int TYPE_SHARE_LIST             = 0;
    public final static int TYPE_CONTACT_HISTORY_LIST   = 1;
    public final static int TYPE_PLACE_HISTORY_LIST     = 2;

    private List<ContactShareModel> mDataList = new LinkedList<>();
    private TextDrawable.IBuilder mDrawableBuilder;
    private Context mContext;
    private String mPlaceIdToShare;
    private String mPlaceNameToShare;
    private int mListType;

    /* Recycler View's handle click events within the adapter or holder
       This listener to send the calling  fragment/ activity about onclick
       events*/
    private IContactFragmentChangeListener mListener;


    public ContactsAdapter(Context c, List<ContactShareModel> dataList) {
        mDataList        = dataList;
        mContext         = c;

        /* The drawable builder is used to give the material circle color effect
        * */
        mDrawableBuilder = TextDrawable.builder()
                .beginConfig()
                .endConfig()
                .round();

    }

    public void setFragmentChangeListener(IContactFragmentChangeListener listener) {
        mListener = listener;
    }

    public ContactsAdapter(Context c, List<ContactShareModel> dataList,String placeId,String placeName) {
        this(c,dataList);
        mPlaceIdToShare    = placeId;
        mPlaceNameToShare  = placeName;
        mListType          = TYPE_SHARE_LIST;
    }

    public ContactsAdapter(Context c, List<ContactShareModel> dataList, int type) {
        this(c, dataList);
        mListType = type;
    }

    @Override
    public long getItemId(int position){
        return mDataList.get(position).hashCode();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_contact, parent,false);
        return new ContactViewHolder(listItemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindContactViewHolder((ContactViewHolder) holder, position);
    }

    private void bindContactViewHolder(ContactViewHolder holder, int position) {
        final ContactShareModel model = mDataList.get(position);
        TextDrawable drawable         = null;

        switch (mListType) {
             /* Handle contact data from the contact share list.
             *  This data opens the email/ sms client
             *  */
            case TYPE_SHARE_LIST :
                holder.getName().setText(model.getName());

                drawable = mDrawableBuilder.build(String.valueOf(model.getName().charAt(0)).toUpperCase(),
                        ColorGenerator.MATERIAL.getColor(model.getName()));
                holder.getImage().setImageDrawable(drawable);

                holder.getContainer().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShareManager.ShareBuilder shareBuilder =
                                new ShareManager.ShareBuilder(mContext,
                                        mContext.getString(R.string.share_message_title),
                                        mPlaceIdToShare,
                                        model.getName());
                        if(!model.getEmail().isEmpty())
                            shareBuilder.email(model.getEmail());
                        if(!model.getPhone().isEmpty())
                            shareBuilder.phone(model.getPhone());
                        shareBuilder.placeName(mPlaceNameToShare).build().shareMessage(mContext);
                    }
                });
                break;
            /*  Handle contact data from the contact history list.
             *  This data opens a view pager to drill into the
             *  Place history
             *  */
            case TYPE_CONTACT_HISTORY_LIST:
                holder.getName().setText(model.getName());
                drawable = mDrawableBuilder.build(String.valueOf(model.getName().charAt(0)).toUpperCase(),
                        ColorGenerator.MATERIAL.getColor(model.getName()));
                holder.getImage().setImageDrawable(drawable);
                holder.getContainer().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mListener == null)
                            return;
                        mListener.OnSetPlaceHistoryForUser(model.getName());
                    }
                });
                break;
            /*  Handle place data from the contact place list.
             *  This data opens an intent to view the selected
             *  place
             *  */
            case TYPE_PLACE_HISTORY_LIST:
                holder.getName().setText(model.getPlaceName());
                drawable = mDrawableBuilder.build(String.valueOf(model.getPlaceName().charAt(0)).toUpperCase(),
                        ColorGenerator.MATERIAL.getColor(model.getPlaceName()));
                holder.getImage().setImageDrawable(drawable);
                holder.getContainer().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mListener == null)
                            return;
                        String url = ShareManager.getPlaceIntentUrlNoShare(model.getPlaceId(),model.getName());
                        GeneralUtils.openHomeActivity(mContext,Uri.parse( url ));
                        mListener.OnFinishActivity();
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

}

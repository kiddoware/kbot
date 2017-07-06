package com.kiddoware.kbot;

/**
 * Created by VMac on 17/11/16.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kiddoware.kbot.models.ActionMessage;

import java.util.ArrayList;


public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private int SELF = 100;
    private ArrayList<ActionMessage> actionMessageArrayList;


    public ChatAdapter(ArrayList<ActionMessage> actionMessageArrayList) {
        this.actionMessageArrayList = actionMessageArrayList;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;

        // view type is to identify where to render the chat message
        // left or right
        if (viewType == SELF) {
            // self message
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(com.kiddoware.kbot.R.layout.chat_item_self, parent, false);
        } else {
            // WatBot message
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(com.kiddoware.kbot.R.layout.chat_item_watson, parent, false);
        }


        return new ViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        ActionMessage actionMessage = actionMessageArrayList.get(position);
        if (actionMessage.getId().equals("1")) {
            return SELF;
        }

        return position;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ActionMessage actionMessage = actionMessageArrayList.get(position);
        actionMessage.setMessage(actionMessage.getMessage());
        ((ViewHolder) holder).message.setText(actionMessage.getMessage());
        if(getItemViewType(position) != SELF) {
            //for bot response display source if present in response
            if(actionMessage.getSource() != null ) {
                ((ViewHolder) holder).source.setVisibility(View.VISIBLE);
                ((ViewHolder) holder).source.setText(actionMessage.getSource());
            }
            else {
                ((ViewHolder) holder).source.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public int getItemCount() {
            return actionMessageArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView message, source;

        public ViewHolder(View view) {
            super(view);
            message = (TextView) itemView.findViewById(com.kiddoware.kbot.R.id.message);
            source = (TextView) itemView.findViewById(com.kiddoware.kbot.R.id.source);

            //TODO: Uncomment this if you want to use a custom Font
            /*String customFont = "Montserrat-Regular.ttf";
            Typeface typeface = Typeface.createFromAsset(itemView.getContext().getAssets(), customFont);
            message.setTypeface(typeface);*/

        }
    }


}
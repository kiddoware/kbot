package com.kiddoware.kbot.views;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kiddoware.kbot.R;
import com.kiddoware.kbot.actions.OpenAppActionExecutor;
import com.kiddoware.kbot.models.ActionMessage;
import com.kiddoware.kbot.models.ActionSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shardul on 26/05/17.
 */

public class ActionMessagesAdapter extends RecyclerView.Adapter<ActionMessagesAdapter.MessagesViewHolder> {

    private List<ActionSection> sections;
    private Context context;
    private LayoutInflater inflater;

    public ActionMessagesAdapter(Context context) {
        this.sections = new ArrayList<>();
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public MessagesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        View view = inflater.inflate(R.layout.message_section, parent, false);


        return new MessagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MessagesViewHolder holder, int position) {
        ActionSection section = sections.get(position);

        holder.titleTextView.setText(section.getTitle());

        final int size = section.getMessages().size();

        holder.messagesGroup.removeAllViews();

        for (int i = 0; i < size; i++) {

            ActionMessage message = section.getMessages().get(i);
            final ActionMessage.Type type = message.getType();


            switch (type) {
                case Options:

                    final View messageView = inflater.inflate(R.layout.message_item_one_line, holder.messagesGroup, false);
                    TextView textView = (TextView) messageView.findViewById(android.R.id.text1);
                    textView.setText(message.getMessage());
                    holder.messagesGroup.addView(messageView);

                    final int optionsSize = message.getOptions().length;

                    for (int j = 0; j < optionsSize; j++) {

                        final Object object = message.getOptions()[j];
                        // need to refactor this if cast check
                        if (object instanceof OpenAppActionExecutor.AppData) {
                            final OpenAppActionExecutor.AppData data = (OpenAppActionExecutor.AppData) object;

                            View appView = inflater.inflate(R.layout.message_item_app_line, holder.messagesGroup, false);
                            TextView appTitle = (TextView) appView.findViewById(android.R.id.text1);

                            appView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    data.executor.handleOption(data);

                                    holder.messagesGroup.removeAllViews();

                                    holder.messagesGroup.addView(messageView);
                                    holder.messagesGroup.addView(v);
                                }
                            });


                            appTitle.setText(data.name);

                            appTitle.setCompoundDrawablePadding((int) context.
                                    getResources().getDimension(R.dimen.activity_horizontal_margin));

                            try {
                                appTitle.setCompoundDrawablesWithIntrinsicBounds(
                                        context.getPackageManager().getApplicationIcon(data.pacakge)
                                        , null, null, null);
                            } catch (PackageManager.NameNotFoundException e) {
                                appTitle.setCompoundDrawablesWithIntrinsicBounds(
                                        data.fallbackDrawable, 0, 0, 0);
                            }

                            holder.messagesGroup.addView(appView);

                            if (j < optionsSize - 1) {
                                View divider = inflater.inflate(R.layout.divider, holder.messagesGroup, false);
                                holder.messagesGroup.addView(divider);
                            }
                        }
                    }

                    break;
                case Suggestions:

                    final int suggestions = message.getOptions().length;

                    if (suggestions >= 2) {
                        View suggestionView = inflater.inflate(R.layout.message_item_two_line, holder.messagesGroup, false);

                        TextView titleView = (TextView) suggestionView.findViewById(android.R.id.text1);
                        TextView descView = (TextView) suggestionView.findViewById(android.R.id.text2);

                        titleView.setText((String) message.getOptions()[0]);
                        descView.setText((String) message.getOptions()[1]);

                        holder.messagesGroup.addView(suggestionView);
                    }

                    break;
                case Message:
                    final View defaultView = inflater.inflate(R.layout.message_item_one_line, holder.messagesGroup, false);
                    TextView text = (TextView) defaultView.findViewById(android.R.id.text1);
                    text.setText(message.getMessage());
                    holder.messagesGroup.addView(defaultView);
                    break;

                case MediaMessage:
                    final View defaultMediaView = inflater.inflate(R.layout.media_message_item_one_line, holder.messagesGroup, false);
                    TextView mediaText = (TextView) defaultMediaView.findViewById(android.R.id.text1);
                    mediaText.setText(message.getMessage());
                    holder.messagesGroup.addView(defaultMediaView);
                    break;

                default:
                    break;
            }

            if (i < size - 1) {
                View divider = inflater.inflate(R.layout.divider, holder.messagesGroup, false);
                holder.messagesGroup.addView(divider);
            }


        }
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    public void addSection(ActionSection section) {
        if (sections.size() == 1) { // force suggestions to clear
            ActionSection suggestionSection = sections.get(0);
            List<ActionMessage> messages = suggestionSection.getMessages();

            if (!messages.isEmpty() && messages.get(0).getType() == ActionMessage.Type.Suggestions) {
                clear();
            }
        }

        sections.add(section);
        notifyDataSetChanged();
    }

    public void clear() {
        sections.clear();
        notifyDataSetChanged();
    }

    class MessagesViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        ViewGroup messagesGroup;

        public MessagesViewHolder(View itemView) {
            super(itemView);

            titleTextView = (TextView) itemView.findViewById(R.id.section_title);
            messagesGroup = (ViewGroup) itemView.findViewById(R.id.root);
        }
    }
}

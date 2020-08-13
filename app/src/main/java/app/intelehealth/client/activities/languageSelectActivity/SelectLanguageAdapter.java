package app.intelehealth.client.activities.languageSelectActivity;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.intelehealth.client.R;
import app.intelehealth.client.models.LanguagesModel;

public class SelectLanguageAdapter extends RecyclerView.Adapter<SelectLanguageAdapter.Myholder> {
    List<LanguagesModel> languages;
    Context context;
    LanguageSelectActivity languageSelectActivity;
    public SelectLanguageAdapter(List<LanguagesModel> patients, Context context,LanguageSelectActivity languageSelectActivity) {
        this.languages = patients;
        this.context = context;
        this.languageSelectActivity = languageSelectActivity;
    }
    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.list_item_select_language, parent, false);
        return new Myholder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectLanguageAdapter.Myholder holder, int position) {
        final LanguagesModel language = languages.get(position);
        if (language != null) {
            holder.language_name.setText(language.getLanguageName());

            if (language.getLanguageSelected())
            {
                holder.select_langauge_card.setCardBackgroundColor(Color.parseColor("#d9d5f6"));
                holder.select_language_tick.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.select_langauge_card.setCardBackgroundColor(Color.parseColor("#ffffff"));
                holder.select_language_tick.setVisibility(View.GONE);
            }

        }
        holder.languageItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                languageSelectActivity.refreshLanguages(language.getLanguageName(),language.getLanguageSpec(),true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    class Myholder extends RecyclerView.ViewHolder {
        LinearLayout languageItem;
        private TextView language_name;
        private CardView select_langauge_card;
        private ImageView select_language_tick;
        public Myholder(View itemView) {
            super(itemView);
            language_name = itemView.findViewById(R.id.language_name);
            languageItem = itemView.findViewById(R.id.languageItem);

            select_langauge_card = itemView.findViewById(R.id.select_langauge_card);
            select_language_tick = itemView.findViewById(R.id.select_language_tick);
        }
    }

}

package nicolasrocchi.com.topquiz.controller;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nicolasrocchi.com.topquiz.R;
import nicolasrocchi.com.topquiz.model.User;

public class RankingActivity extends AppCompatActivity {

    private TextView mRankTextView;
    private Button mRankButtonScore;
    private Button mRankButtonName;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        mRankTextView = (TextView) findViewById(R.id.activity_ranking_textview);
        mRankButtonScore = (Button) findViewById(R.id.activity_ranking_sort_by_score);
        mRankButtonName = (Button) findViewById(R.id.activity_ranking_sort_by_name);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (mPreferences.contains("PREF_KEY_LEADERBOARD")) {
            // On créé un objet Gson afin de convertir la nouvelle liste en json pour la sauvegarder dans les shared preferences
            Gson gson = new Gson();

            // On récupère la liste Leaderboard courante en json depuis les sharedPref et on la convertit en List
            String json = mPreferences.getString("PREF_KEY_LEADERBOARD", null);
            Type type = new TypeToken<List<User>>(){}.getType();
            final List<User> Leaderboard = gson.fromJson(json, type);

            // On trie la liste par score
            sortByScore(Leaderboard);

            // On affiche dynamiquement la liste avec StringBuilder
            displayLeaderboard(Leaderboard);

            mRankButtonScore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // On trie par score puis on affiche dynamiquement la liste
                    sortByScore(Leaderboard);
                    displayLeaderboard(Leaderboard);
                }

            });

            mRankButtonName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // On trie par nom puis on affiche dynamiquement la liste
                    sortByName(Leaderboard);
                    displayLeaderboard(Leaderboard);
                }
            });
        }
    }

    private void displayLeaderboard (List<User> Leaderboard) {
        // On utilise StringBuilder pour générer dynamiquement la textview selon la liste obtenue
        StringBuilder builder = new StringBuilder();

        for(int i=0; i<Leaderboard.size(); i++){

            builder.append((i + 1) + ". " + Leaderboard.get(i).getFirstName() + " (" + Leaderboard.get(i).getScore() + ")");
            if (i < Leaderboard.size() - 1){ builder.append("\n\n"); }
        }

        mRankTextView.setText(builder.toString());
    }

    private void sortByScore (List<User> Leaderboard) {
        // on trie la liste par score décroissant puis par ordre alphabétique croissant afin de placer en 1er
        // le plus grand score et si égalité le 1er nom par ordre alphabétique
        Collections.sort(Leaderboard, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                if (user2.getScore() != user1.getScore()) {
                    return user2.getScore() - user1.getScore();
                }
                else {
                    return user1.getFirstName().compareTo(user2.getFirstName());
                }
            }
        });
    }

    private void sortByName (List<User> Leaderboard) {
        // on trie la liste par ordre alphabétique croissant puis par score décroissant afin de placer en 1er
        // le 1er nom par ordre alphabétique et si égalité le plus grand score
        Collections.sort(Leaderboard, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                if (user2.getFirstName() != user1.getFirstName()) {
                    return user1.getFirstName().compareTo(user2.getFirstName());
                }
                else {
                    return user2.getScore() - user1.getScore();
                }
            }
        });
    }
}

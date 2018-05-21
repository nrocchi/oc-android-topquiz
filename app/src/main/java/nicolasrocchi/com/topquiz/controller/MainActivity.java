package nicolasrocchi.com.topquiz.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nicolasrocchi.com.topquiz.R;
import nicolasrocchi.com.topquiz.model.User;

public class MainActivity extends AppCompatActivity {

    private TextView mGreetingText;
    private EditText mNameInput;
    private Button mPlayButton;
    private Button mRankingButton;
    private User mUser;
    private SharedPreferences mPreferences;
    private ArrayList<User> Leaderboard;

    private static final int GAME_ACTIVITY_REQUEST_CODE = 42;
    private static final int RANKING_ACTIVITY_REQUEST_CODE = 7;

    public static final String PREF_KEY_FIRSTNAME = "PREF_KEY_FIRSTNAME";
    public static final String PREF_KEY_SCORE = "PREF_KEY_SCORE";
    public static final String PREF_KEY_LEADERBOARD = "PREF_KEY_LEADERBOARD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.out.println("MainActivity::onCreate()");

        mUser = new User();
        Leaderboard = new ArrayList<>();

        mGreetingText = (TextView) findViewById(R.id.activity_main_greeting_txt);
        mNameInput = (EditText) findViewById(R.id.activity_main_name_input);
        mPlayButton = (Button) findViewById(R.id.activity_main_play_btn);
        mRankingButton = (Button) findViewById(R.id.activity_main_ranking_btn);

        mPlayButton.setEnabled(false);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (!mPreferences.contains("PREF_KEY_LEADERBOARD")) {
            mRankingButton.setVisibility(View.GONE);
        }

        greetUser();

        mNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mPlayButton.setEnabled(s.toString().length() != 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String FirstName = mNameInput.getText().toString();
                mUser.setFirstName(FirstName);

                mPreferences.edit().putString(PREF_KEY_FIRSTNAME, mUser.getFirstName()).apply();

                Intent gameActivity = new Intent(MainActivity.this, GameActivity.class);
                startActivityForResult(gameActivity, GAME_ACTIVITY_REQUEST_CODE);
            }
        });

        mRankingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent rankingActivity = new Intent(MainActivity.this, RankingActivity.class);
                startActivityForResult(rankingActivity, RANKING_ACTIVITY_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (GAME_ACTIVITY_REQUEST_CODE == requestCode && RESULT_OK == resultCode) {
            // Fetch the score from the Intent
            int score = data.getIntExtra(GameActivity.BUNDLE_EXTRA_SCORE, 0);

            mPreferences.edit().putInt(PREF_KEY_SCORE, score).apply();

            // On prépare l'objet User à insérer dans la liste Leaderboard
            mUser.setScore(score);
            Character first = Character.toUpperCase(mUser.getFirstName().charAt(0));
            mUser.setFirstName(first + mUser.getFirstName().substring(1, mUser.getFirstName().length()));

            // On créé un objet Gson afin de convertir la nouvelle liste en json pour la sauvegarder dans les shared preferences
            Gson gson = new Gson();

            // On récupère la liste Leaderboard courante en json depuis les sharedPref et on la convertit en List
            if (mPreferences.contains("PREF_KEY_LEADERBOARD")) {
                String json = mPreferences.getString("PREF_KEY_LEADERBOARD", null);
                Type type = new TypeToken<List<User>>(){}.getType();
                Leaderboard = gson.fromJson(json, type);

                //On trie la liste afin de comparer le dernier score avec le + petit score de la liste
                sortByScoreAsc(Leaderboard);
            }

            // Si la liste est vide on ajoute automatiquement
            if (Leaderboard.isEmpty()) {
                Leaderboard.add(new User(mUser.getFirstName(), mUser.getScore()));
            }
            //Sinon si le score est + grand ou égal au plus petit score de la liste (le 1er car la liste est triée)
            // On ajoute l'user à la liste
            else if (score >= Leaderboard.get(0).getScore() || Leaderboard.size() <= 6) {
                Leaderboard.add(new User(mUser.getFirstName(), mUser.getScore()));

                // Si la liste est égale à 6 on enlève le 1er score de la liste car la liste est triée
                if (Leaderboard.size() == 6) {
                    Leaderboard.remove(0);
                }
            }

            // On convertit la liste en json
            String jsonLeaderboard = gson.toJson(Leaderboard);

            // on ajoute le json aux shared preferences
            mPreferences.edit().putString(PREF_KEY_LEADERBOARD, jsonLeaderboard).apply();

            // on affiche le bouton des meilleurs scores
            mRankingButton.setVisibility(View.VISIBLE);

            greetUser();
        }
    }

    private void sortByScoreAsc (List<User> Leaderboard) {
        // on trie la liste par score croissant puis par ordre alphabétique décroissant afin de retirer
        // le plus petit score et si égalité le dernier nom par ordre alphabétique
        Collections.sort(Leaderboard, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                if (user2.getScore() != user1.getScore()) {
                    return user1.getScore() - user2.getScore();
                }
                else {
                    return user2.getFirstName().compareTo(user1.getFirstName());
                }
            }
        });
    }

    private void greetUser() {
        String firstname = mPreferences.getString(PREF_KEY_FIRSTNAME, null);

        if (firstname != null) {
            int score = mPreferences.getInt(PREF_KEY_SCORE, 0);

            String fulltext = "Bonjour " + firstname
                    + " !\n\nVotre dernier score est : " + score
                    + "\n\nPouvez-vous faire mieux cette fois-ci ?";
            mGreetingText.setText(fulltext);
            mNameInput.setText(firstname);
            mNameInput.setSelection(firstname.length());
            mPlayButton.setEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        System.out.println("MainActivity::onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();

        System.out.println("MainActivity::onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();

        System.out.println("MainActivity::onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();

        System.out.println("MainActivity::onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        System.out.println("MainActivity::onDestroy()");
    }
}

// BattleActivity.java
package com.example.pokedex_mobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.pokedex_mobile.data.PokemonRepository;
import com.example.pokedex_mobile.dataRemote.RetrofitProvider;
import com.example.pokedex_mobile.domain.PokemonUi;
import com.example.pokedex_mobile.ui.TypeColors;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

public class BattleActivity extends AppCompatActivity {

    public static final String EXTRA_PLAYER = "player_ui";

    private View root;
    private ImageView ivPlayer, ivOpponent, ivTypePlayer, ivTypeOpponent;
    private TextView tvPlayerName, tvOpponentName, tvPlayerHp, tvOpponentHp;
    private TextView tvTypePlayer1, tvTypePlayer2, tvTypeOpponent1, tvTypeOpponent2;
    private ProgressBar pbPlayer, pbOpponent;

    // Contador/Luta
    private TextView tvCountdown;
    private ImageView ivClash;

    private PokemonUi player, opponent;
    private int maxHpPlayer, maxHpOpponent;
    private int curHpPlayer, curHpOpponent;

    private final PokemonRepository repo =
            new PokemonRepository(RetrofitProvider.getApi());
    private final Random rnd = new Random();
    private final Handler handler = new Handler();

    // ====== LOOP DE CLASH / TIMEOUT ======
    private static final int TICK_MS = 120;          // intervalo de atualização do HP
    private static final long CLASH_MAX_MS = 5000L;  // 5s: empate se ninguém cair a 0
    private boolean isClashing = false;
    private boolean resultShown = false;
    private int startHpPlayerSnapshot = 0;
    private int startHpOpponentSnapshot = 0;

    private final Runnable clashTick = new Runnable() {
        @Override public void run() {
            if (!isClashing || resultShown) return;

            // dano por segundo conforme regra Ataque-Defesa
            final int dpsPlayerToOpp = Math.max(0, player.attack   - opponent.defense);
            final int dpsOppToPlayer = Math.max(0, opponent.attack - player.defense);

            // dano por tick (float -> arredonda suavemente)
            float tickOpp = dpsPlayerToOpp * (TICK_MS / 1000f);
            float tickPlr = dpsOppToPlayer * (TICK_MS / 1000f);

            int nextOpp = Math.max(0, Math.round(curHpOpponent - tickOpp));
            int nextPlr = Math.max(0, Math.round(curHpPlayer   - tickPlr));

            curHpOpponent = nextOpp;
            curHpPlayer   = nextPlr;

            refreshHp();

            // encerrar por KO
            if (curHpOpponent == 0 && curHpPlayer == 0) {
                resultShown = true;
                endClashAndShow(Outcome.DRAW);
                return;
            }
            if (curHpOpponent == 0) {
                resultShown = true;
                endClashAndShow(Outcome.PLAYER_WIN);
                return;
            }
            if (curHpPlayer == 0) {
                resultShown = true;
                endClashAndShow(Outcome.OPPONENT_WIN);
                return;
            }

            // continua drenando
            handler.postDelayed(this, TICK_MS);
        }
    };

    private final Runnable clashTimeout = new Runnable() {
        @Override public void run() {
            if (resultShown) return; // já resolveu por KO
            // Se em 5s ninguém caiu, consideramos EMPATE
            // (Se preferir “quem tiver mais HP”, troque a linha abaixo pelo bloco comentado)
            resultShown = true;
            endClashAndShow(Outcome.DRAW);

            /*
            // Alternativa: decidir pelo maior HP após 5s
            Outcome out;
            if (curHpPlayer == curHpOpponent) out = Outcome.DRAW;
            else out = (curHpPlayer > curHpOpponent) ? Outcome.PLAYER_WIN : Outcome.OPPONENT_WIN;
            resultShown = true;
            endClashAndShow(out);
            */
        }
    };

    private enum Outcome { PLAYER_WIN, OPPONENT_WIN, DRAW }

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        // refs
        root            = findViewById(R.id.rootBattle);
        ivPlayer        = findViewById(R.id.ivPlayer);
        ivOpponent      = findViewById(R.id.ivOpponent);
        ivTypePlayer    = findViewById(R.id.ivTypePlayer);
        ivTypeOpponent  = findViewById(R.id.ivTypeOpponent);
        tvPlayerName    = findViewById(R.id.tvPlayerName);
        tvOpponentName  = findViewById(R.id.tvOpponentName);
        tvPlayerHp      = findViewById(R.id.tvPlayerHp);
        tvOpponentHp    = findViewById(R.id.tvOpponentHp);
        pbPlayer        = findViewById(R.id.pbPlayer);
        pbOpponent      = findViewById(R.id.pbOpponent);

        tvTypePlayer1   = findViewById(R.id.tvTypePlayer1);
        tvTypePlayer2   = findViewById(R.id.tvTypePlayer2);
        tvTypeOpponent1 = findViewById(R.id.tvTypeOpponent1);
        tvTypeOpponent2 = findViewById(R.id.tvTypeOpponent2);

        // Contador/Clash (sem fundo)
        tvCountdown     = findViewById(R.id.tvCountdown);
        tvCountdown.setBackground(null);
        ivClash         = findViewById(R.id.ivClash);

        Object extra = getIntent().getSerializableExtra(EXTRA_PLAYER);
        if (!(extra instanceof PokemonUi)) { finish(); return; }
        player = (PokemonUi) extra;

        // Carrega adversário aleatório (de API) diferente do seu
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<PokemonUi> list = repo.loadDetail(); // 50 primeiros com stats/tipos/imagem
                PokemonUi pick = null;
                if (list != null && !list.isEmpty()) {
                    do { pick = list.get(rnd.nextInt(list.size())); } while (pick.id == player.id);
                }
                PokemonUi finalOpp = pick;
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (finalOpp == null) { finish(); return; }
                    opponent = finalOpp;
                    setupUi();
                    startCountdownThenBattle();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(this::finish);
            }
        });
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        // limpa callbacks para evitar vazamento/duplo disparo
        handler.removeCallbacks(clashTick);
        handler.removeCallbacks(clashTimeout);
    }

    private void setupUi() {
        // Nomes e sprites (GIFs animate via Glide)
        tvPlayerName.setText(player.name);
        tvOpponentName.setText(opponent.name);
        Glide.with(this).load(player.imageUrl).into(ivPlayer);
        Glide.with(this).load(opponent.imageUrl).into(ivOpponent);

        // HP inicial vindo da API (PokemonUi.hp)
        maxHpPlayer   = Math.max(1, player.hp);
        maxHpOpponent = Math.max(1, opponent.hp);
        curHpPlayer   = maxHpPlayer;
        curHpOpponent = maxHpOpponent;
        initBar(pbPlayer, maxHpPlayer);
        initBar(pbOpponent, maxHpOpponent);
        refreshHp();

        // Tipos
        fillTypes(player, ivTypePlayer, tvTypePlayer1, tvTypePlayer2);
        fillTypes(opponent, ivTypeOpponent, tvTypeOpponent1, tvTypeOpponent2);
    }

    private void startCountdownThenBattle() {
        tvCountdown.setVisibility(View.VISIBLE);

        final String[] seq = {"3", "2", "1", "LUTEM!"};
        final long step = 800; // ms por etapa

        tvCountdown.setScaleX(0.8f);
        tvCountdown.setScaleY(0.8f);
        tvCountdown.setAlpha(0f);

        for (int i = 0; i < seq.length; i++) {
            final int idx = i;
            tvCountdown.postDelayed(() -> {
                tvCountdown.setText(seq[idx]);

                tvCountdown.animate()
                        .alpha(1f).scaleX(1f).scaleY(1f)
                        .setDuration(220).withEndAction(() ->
                                tvCountdown.animate()
                                        .alpha(idx == seq.length - 1 ? 1f : 0f)
                                        .setDuration(idx == seq.length - 1 ? 0 : 220)
                                        .start()
                        ).start();

                if (idx == seq.length - 1) {
                    // último frame: iniciar aproximação
                    tvCountdown.postDelayed(() -> {
                        tvCountdown.setVisibility(View.GONE);
                        approachAndClash();
                    }, 500);
                }
            }, i * step);
        }
    }

    /** Anima os dois indo um ao encontro do outro e aciona o “clash” com drenagem/timeout */
    private void approachAndClash() {
        float playerTargetDx = -dp(90); // player avança para a esquerda
        float oppTargetDx    =  dp(90); // oponente avança para a direita

        ObjectAnimator pGo = ObjectAnimator.ofFloat(ivPlayer, View.TRANSLATION_X,
                ivPlayer.getTranslationX(), ivPlayer.getTranslationX() + playerTargetDx);
        ObjectAnimator oGo = ObjectAnimator.ofFloat(ivOpponent, View.TRANSLATION_X,
                ivOpponent.getTranslationX(), ivOpponent.getTranslationX() + oppTargetDx);

        pGo.setDuration(550);
        oGo.setDuration(550);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(pGo, oGo);
        set.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                showClashAndStartLoop();
            }
        });
        set.start();
    }

    /** Mostra o GIF de clash e inicia o loop de drenagem até KO ou timeout de 5s */
    private void showClashAndStartLoop() {
        ivPlayer.setVisibility(View.INVISIBLE);
        ivOpponent.setVisibility(View.INVISIBLE);
        ivClash.setVisibility(View.VISIBLE);

        try {
            Glide.with(this).asGif().load(R.drawable.battle_clash).into(ivClash);
        } catch (Exception e) {
            ivClash.setImageResource(R.drawable.ic_pokeball);
        }

        // Inicializa flags/snapshot e inicia loop + timeout
        resultShown = false;
        startHpPlayerSnapshot   = curHpPlayer;
        startHpOpponentSnapshot = curHpOpponent;

        isClashing = true;
        handler.post(clashTick);
        handler.postDelayed(clashTimeout, CLASH_MAX_MS);
    }

    /** Finaliza o clash, esconde o GIF e abre o resultado */
    private void endClashAndShow(@NonNull Outcome outcome) {
        isClashing = false;
        handler.removeCallbacks(clashTick);
        handler.removeCallbacks(clashTimeout);
        if (isFinishing() || isDestroyed()) return;
        ivClash.setVisibility(View.GONE);
        showResultDialog(outcome);
    }

    private Outcome simulateOutcome() {
        int dmgPO = Math.max(1, player.attack   - opponent.defense); // player -> opp
        int dmgOP = Math.max(1, opponent.attack - player.defense);   // opp -> player

        int ticksToKoOpp = (int) Math.ceil(maxHpOpponent / (float) dmgPO);
        int ticksToKoPlr = (int) Math.ceil(maxHpPlayer   / (float) dmgOP);

        if (ticksToKoOpp < ticksToKoPlr) return Outcome.PLAYER_WIN;
        if (ticksToKoPlr < ticksToKoOpp) return Outcome.OPPONENT_WIN;
        return Outcome.DRAW;
    }

    private void showResultDialog(@NonNull Outcome outcome) {
        if (isFinishing() || isDestroyed()) return;

        Dialog d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_battle_result);
        d.setCanceledOnTouchOutside(false);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            d.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams lp = d.getWindow().getAttributes();
            lp.dimAmount = 0.5f;
            d.getWindow().setAttributes(lp);
            d.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        TextView tvTitle = d.findViewById(R.id.tvTitle);
        TextView tvMsg   = d.findViewById(R.id.tvMessage);
        View btnHome     = d.findViewById(R.id.btnHome);

        String msg;
        switch (outcome) {
            case PLAYER_WIN:
                msg = "O seu pokémon escolhido " + player.name +
                        " ganhou a batalha contra o pokémon do boot " + opponent.name + ".";
                break;
            case OPPONENT_WIN:
                msg = "O seu pokémon escolhido " + player.name +
                        " perdeu a batalha contra o pokémon do boot " + opponent.name + ".";
                break;
            default:
                msg = "O seu pokémon escolhido " + player.name +
                        " empatou a batalha contra o pokémon do boot " + opponent.name + ".";
                break;
        }
        tvMsg.setText(msg);

        btnHome.setOnClickListener(v -> {
            d.dismiss();
            finish(); // volta para a tela anterior (início)
        });

        d.show();
    }

    // ===== helpers visuais existentes =====

    private void fillTypes(@NonNull PokemonUi p,
                           @NonNull ImageView typeDot,
                           @NonNull TextView chip1,
                           @NonNull TextView chip2) {
        chip1.setVisibility(View.GONE);
        chip2.setVisibility(View.GONE);

        if (p.typesPt == null || p.typesPt.isEmpty()) {
            setTypeDot(typeDot, Color.parseColor("#9E9E9E"));
            return;
        }

        String t1 = p.typesPt.get(0);
        int color1 = TypeColors.bgFor(Collections.singletonList(t1), this);
        setTypeDot(typeDot, color1);
        styleChip(chip1, t1, color1);
        chip1.setVisibility(View.VISIBLE);

        if (p.typesPt.size() > 1) {
            String t2 = p.typesPt.get(1);
            int color2 = TypeColors.bgFor(Collections.singletonList(t2), this);
            styleChip(chip2, t2, color2);
            chip2.setVisibility(View.VISIBLE);
        }
    }

    private void styleChip(TextView tv, String text, @ColorInt int bg) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(bg);
        g.setCornerRadius(dp(10));
        tv.setBackground(g);
        tv.setText(text);
        tv.setTextColor(Color.WHITE);
    }

    private void setTypeDot(ImageView iv, @ColorInt int color) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setSize(dp(14), dp(14));
        g.setColor(color);
        iv.setImageDrawable(g);
    }

    private void refreshHp() {
        pbPlayer.setProgress(curHpPlayer);
        pbOpponent.setProgress(curHpOpponent);

        tvPlayerHp.setText(curHpPlayer + "/" + maxHpPlayer);
        tvOpponentHp.setText(curHpOpponent + "/" + maxHpOpponent);

        int cPlayer   = colorForHp(curHpPlayer, maxHpPlayer);
        int cOpponent = colorForHp(curHpOpponent, maxHpOpponent);
        pbPlayer.setProgressTintList(ColorStateList.valueOf(cPlayer));
        pbOpponent.setProgressTintList(ColorStateList.valueOf(cOpponent));
        pbPlayer.setProgressBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#22000000")));
        pbOpponent.setProgressBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#22000000")));
    }

    private void initBar(ProgressBar pb, int max) {
        pb.setMax(max);
        pb.setProgress(max);
    }

    @ColorInt
    private int colorForHp(int cur, int max) {
        float pct = (max <= 0) ? 0f : (cur * 1f / max);
        if (pct > 0.60f) return Color.parseColor("#2ecc71");  // verde
        if (pct > 0.40f) return Color.parseColor("#7ed957");  // verde claro
        if (pct > 0.25f) return Color.parseColor("#f1c40f");  // amarelo
        if (pct > 0.10f) return Color.parseColor("#e67e22");  // laranja
        return Color.parseColor("#e74c3c");                    // vermelho
    }

    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return (int) (v * d);
    }
}

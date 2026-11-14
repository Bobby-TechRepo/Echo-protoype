package io.bsoft.echo.ui;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import io.bsoft.echo.level.Challenge;
import io.bsoft.echo.level.LevelResult;
import io.bsoft.echo.rendering.Assets;
import io.bsoft.echo.rendering.Palette;
import io.bsoft.echo.save.LevelProgress;

/** Level complete summary (spec §45) with challenge results and best comparisons. */
public final class ResultsOverlay {

    public static final String FEEDBACK_URL = "https://docs.google.com/forms/d/e/1FAIpQLScP68-JNdRYUPp1i48kFkOim1yDjSR1hZfcT36TTVpKA3gkKA/viewform?usp=header";

    private final Assets assets;
    private final UiCanvas canvas;
    private LevelResult result;
    private Array<Challenge> challenges;
    private LevelProgress progress;
    private boolean hasNext;
    private boolean showFeedbackPrompt;
    private float appear;

    public ResultsOverlay(Assets assets, UiCanvas canvas) {
        this.assets = assets;
        this.canvas = canvas;
    }

    public void show(LevelResult result, Array<Challenge> challenges, LevelProgress progress, boolean hasNext,
                     boolean showFeedbackPrompt) {
        this.result = result;
        this.challenges = challenges;
        this.progress = progress;
        this.hasNext = hasNext;
        this.showFeedbackPrompt = showFeedbackPrompt;
        this.appear = 0f;
    }

    public boolean shouldShowFeedbackPrompt() {
        return showFeedbackPrompt;
    }

    public boolean handleFeedbackClick(float x, float y) {
        if (!showFeedbackPrompt) {
            return false;
        }
        float bx = UiCanvas.WIDTH / 2f - 260f;
        float by = 100f;
        float bw = 520f;
        float bh = 120f;
        if (x >= bx && x <= bx + bw && y >= by && y <= by + bh) {
            openFeedbackLink();
            return true;
        }
        return false;
    }

    public void openFeedbackLink() {
        Gdx.net.openURI(FEEDBACK_URL);
    }

    public void update(float delta) {
        appear = Math.min(1f, appear + delta * 2.5f);
    }

    public void render() {
        if (result == null) {
            return;
        }
        canvas.begin();
        canvas.rect(0f, 0f, UiCanvas.WIDTH, UiCanvas.HEIGHT, Color.BLACK, 0.65f * appear);
        float cx = UiCanvas.WIDTH / 2f;
        float y = UiCanvas.HEIGHT - 110f - (1f - appear) * 30f;
        canvas.text(assets.fontTitle, "LEVEL COMPLETE", cx, y, Align.center, Palette.EXIT, appear);
        y -= 90f;

        float left = cx - 200f;
        float right = cx + 200f;
        y = statRow(y, left, right, "Time", String.format("%.1fs", result.completionTime()),
                progress != null && progress.bestTime() > 0f && result.completionTime() <= progress.bestTime());
        y = statRow(y, left, right, "Echoes", Integer.toString(result.echoesUsed()),
                progress != null && progress.bestEchoes() >= 0 && result.echoesUsed() <= progress.bestEchoes());
        y = statRow(y, left, right, "Rewinds", Integer.toString(result.rewindCount()), false);
        y = statRow(y, left, right, "Deaths", Integer.toString(result.deaths()), false);
        y = statRow(y, left, right, "Recording", String.format("%.1fs", result.recordingTime()), false);
        if (result.paradoxes() > 0) {
            y = statRow(y, left, right, "Paradoxes", Integer.toString(result.paradoxes()), false);
        }
        y -= 30f;

        if (challenges != null) {
            for (Challenge c : challenges) {
                boolean done = c.isSatisfied(result);
                String mark = done ? "[x]" : "[ ]";
                Color color = done ? Palette.PLATE_ACTIVE : Palette.UI_DIM;
                canvas.text(assets.fontLarge, mark + "  " + c.description(), left, y, Align.left, color, appear);
                y -= 36f;
            }
        }
        y -= 30f;
        boolean isAndroid = Gdx.app.getType() == Application.ApplicationType.Android;
        String prompt;
        if (showFeedbackPrompt) {
            if (isAndroid) {
                prompt = hasNext ? "Next level     Retry     Level select     Feedback" : "Retry     Level select     Feedback";
            } else {
                prompt = hasNext ? "[ENTER] Next level     [R] Retry     [L] Level select     [F] Feedback"
                        : "[R] Retry     [L] Level select     [F] Feedback";
            }
        } else if (isAndroid) {
            prompt = hasNext ? "Next level     Retry     Level select" : "Retry     Level select";
        } else {
            prompt = hasNext ? "[ENTER] Next level     [R] Retry     [L] Level select"
                    : "[R] Retry     [L] Level select";
        }
        canvas.text(assets.font, prompt, cx, Math.max(y, 60f), Align.center, Palette.UI_TEXT, appear);

        if (showFeedbackPrompt) {
            float boxX = cx - 260f;
            float boxY = 74f;
            float boxW = 520f;
            float boxH = 120f;
            canvas.rect(boxX, boxY, boxW, boxH, Color.BLACK, 0.78f * appear);
            canvas.rect(boxX + 10f, boxY + 10f, boxW - 20f, boxH - 20f, Palette.PLATE_ACTIVE, 0.18f * appear);
            canvas.text(assets.fontLarge, "Thanks for playing!", cx, boxY + boxH - 34f, Align.center, Palette.UI_TEXT, appear);
            canvas.textWrapped(assets.font, "Please share your gameplay feedback. Tap or press F to open the survey.",
                    cx, boxY + 58f, boxW - 32f, Palette.UI_TEXT, appear);
            canvas.text(assets.font, "Open feedback form", cx, boxY + 24f, Align.center, Palette.EXIT, appear);
        }
        canvas.end();
    }

    private float statRow(float y, float left, float right, String label, String value, boolean best) {
        canvas.text(assets.fontLarge, label, left, y, Align.left, Palette.UI_DIM, appear);
        canvas.text(assets.fontLarge, value, right, y, Align.right, Palette.UI_TEXT, appear);
        if (best) {
            canvas.text(assets.font, "BEST", right + 16f, y - 4f, Align.left, Palette.PLATE_ACTIVE, appear);
        }
        return y - 40f;
    }
}
